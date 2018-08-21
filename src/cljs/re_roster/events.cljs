(ns re-roster.events
  (:require
   [re-frame.core :as rf]
   [re-roster.db :as db]
   [re-roster.subs :as subs]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [taoensso.timbre :refer [debug warn error]]
   [vimsical.re-frame.cofx.inject :as inject]))

(.addEventListener js/document.body "mouseup" #(rf/dispatch [::mouse-up]))
(.addEventListener js/document.body "mousedown" #(rf/dispatch [::mouse-down]))
(.addEventListener js/document.body "mousemove" #(rf/dispatch [::mouse-move
                                                                     (+ (.-clientX %)
                                                                        (.-scrollX js/window))
                                                                     (+
                                                                      (.-clientY %)
                                                                      (.-scrollY js/window))]))

(def slots-per-hour 4)
(rf/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(rf/reg-event-db
 ::initialize
 (fn [db [_ {:keys [id] :as opts}]]
   (assoc-in db [:roster/data id :options] opts)))

(rf/reg-event-db
 ::set-re-pressed-example
 (fn [db [_ value]]
   (assoc db :re-pressed-example value)))

(rf/reg-event-db
 ::mouse-move
 (fn [db [_ x y]]
   (-> db
       (assoc-in [:roster/data :mouse :x] x)
       (assoc-in [:roster/data :mouse :y] y)
       (assoc-in [:roster/data :mouse :moved?] true))))

(rf/reg-event-db
 ::mouse-up
 (fn [db _]
   (-> db
       (assoc-in [:roster/data :mouse :down] false)
       (assoc-in [:roster/data :mouse :moved?] false))))

(rf/reg-event-db
 ::mouse-down
 (fn [db _]
   (-> db
       (assoc-in [:roster/data :mouse :moved?] false)
       (assoc-in [:roster/data :mouse :down] true))))

(rf/reg-event-db
 ::start-drag
 (fn [db [_ id [day hour slot] direction record-id]]
   (assoc-in db [:roster/data id :drag-state] {:k         [day hour slot]
                                               :record-id record-id
                                               :direction direction})))

(rf/reg-event-db
 ::stop-drag
 (fn [db [_ id]]
   (assoc-in db [:roster/data id :drag-state] nil)))

(defn resize-top
  [r [day hour slot :as k]]
  (let [old-pos    (+ (* (:hour r) slots-per-hour ) (:timeslot r))
        new-pos    (+ (* hour slots-per-hour) slot)
        delta      (if (> old-pos new-pos)
                     (- old-pos new-pos)
                     (- new-pos old-pos))
        new-length (if (> old-pos new-pos)
                     (+ (:length r) delta)
                     (- (:length r) delta))]
    (assoc r
           :day day
           :hour hour
           :timeslot slot
           :length new-length)))

(defn resize-bottom
  [r [day hour slot :as k]]
  (let [old-pos (+ (* (:hour r) slots-per-hour) (:timeslot r))
        new-pos (+ (* hour slots-per-hour) slot)
        delta   (if (> old-pos new-pos)
                  (- old-pos new-pos)
                  (- new-pos old-pos))]
    (assoc r
           :day day
           :length (inc delta))))
(defn resize-move
  [r [day hour slot :as k]]
  (assoc r
         :day day
         :hour hour
         :timeslot slot))

(defn update-record
  [r dir [day hour slot :as k]]
  (case dir
    :move   (resize-move r k)
    :top    (resize-top r k)
    :bottom (resize-bottom r k)))

(rf/reg-event-fx
 ::hover-over
 [(rf/inject-cofx ::inject/sub [::subs/mouse])
  (rf/inject-cofx ::inject/sub
                  (fn [[_ id data-sub]]
                    [::subs/lookup-table data-sub]))
  (rf/inject-cofx ::inject/sub
                  (fn [[_ id]]
                    [::subs/collision id]))]
 (fn [{db           :db
       mouse        ::subs/mouse
       lookup-table ::subs/lookup-table
       collision    ::subs/collision}
      [_ id data-sub]]
   (let [dragging?       (:down mouse)
         update-dispatch (get-in db [:roster/data id :options :update-dispatch])
         {:keys [direction record-id]
          :as   ds}      (get-in db [:roster/data id :drag-state]) ;;:top, :move, or :bottom
         old-k           (get-in db [:roster/data id :drag-state :k])
         old-record      (when dragging?
                           (->> (get lookup-table old-k)
                                (filter (fn [r]
                                          (= (:record-id r)
                                             record-id)))
                                first))
         updated-record  (when (and collision dragging?)
                           (update-record old-record direction collision))]
     (cond-> {:db db}
       (and dragging?
            collision)           (assoc-in [:db :roster/data id :drag-state :k]
                                           [(:day updated-record)
                                            (:hour updated-record)
                                            (:timeslot updated-record)])
       (and dragging?            ;;perf tweak
            collision)           (assoc-in [:db :roster/data :mouse :last-collision]
                                           collision)
       (and dragging? collision) (assoc :dispatch (vec (concat update-dispatch
                                                               [id collision updated-record])))))))

;;; ---- todo move to some demo namespace

(rf/reg-event-db
 ::add-data
 (fn [db [_ d]]
   (println (:data db))
   (update db :data conj d)))

(rf/reg-event-db
 ::my-update-dispatch
 (fn [db [_ id k new-record]]
   (update db :data (fn [rs]
                      (map
                       (fn [r]
                         (if (= (:record-id r) (:record-id new-record))
                           new-record
                           r))
                       rs)))))
