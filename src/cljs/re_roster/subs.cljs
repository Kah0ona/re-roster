(ns re-roster.subs
  (:require
   [taoensso.timbre :refer [debug]]
   [re-frame.core :as rf]))

(rf/reg-sub
 ::name
 (fn [db]
   (:name db)))

(rf/reg-sub
 ::re-pressed-example
 (fn [db _]
   (:re-pressed-example db)))

(rf/reg-sub
 ::options
 (fn [db [_ id]]
   (get-in db [:roster/data id :options])))

(rf/reg-sub
 ::records
 (fn [[_ subscription-path] _]
   (assert subscription-path "make sure subscription-path is set")
   (rf/subscribe subscription-path))
 (fn [records _]
   records))

(rf/reg-sub
 ::drag-state
 (fn [db [_ id]]
   (get-in db [:roster/data id :drag-state])))

(rf/reg-sub
 ::mouse
 (fn [db _]
   (get-in db [:roster/data :mouse])))

(defn collides?
  [{x :x y :y :as mouse}
   [{r2 :right l2 :left t2 :top b2 :bottom :as rect} elt]]
  (if (nil? rect)
    false
    (if (not (or (< x l2) (> x r2) (< y t2) (> y b2)))
      [rect elt]
      false)))

(defn find-bounding-rects
  "Input should be a HTMLCollection, returns a list of tuples, of [rect elt]"
  [coll]
  (map
   (fn [c]
     (let [rect (.getBoundingClientRect c)
           pos  {:top    (.-top rect)
                 :left   (.-left rect)
                 :bottom (.-bottom rect)
                 :right  (.-right rect)}]
       [(-> pos
            (update :top    + (.-scrollY js/window))
            (update :right  + (.-scrollX js/window))
            (update :bottom + (.-scrollY js/window))
            (update :left + (.-scrollX js/window)))
        c]))
   (js->clj (.from js/Array coll))))

(defn find-collision
  [{:keys [x y] :as mouse} coll]
  (->> coll
       (some (partial collides? mouse))
       second ;;second tuple element is the html element
       ))

(defn find-timeslot-cells
  [hour-cell]
  (when hour-cell
    (js->clj (.from js/Array (.-childNodes hour-cell)))))

(defn extract-time-key-from-class
  [e]
  (let [classes (.-className e)
        res     (re-find #"roster-time-slot-([a-z]+)-([0-9]+)+-([0-9]+)" classes)]
    (->> res
         (drop 1)
         (map-indexed (fn [i e]
                        (if (= i 0)
                          (keyword e)
                          (js/parseInt e))))
         vec)))

(defn find-data-entry-by-key
  [[day hour slot :as x]]
  (when x
    (->
     (.from js/Array
            (.getElementsByClassName js/document (str "roster-time-slot-" (name day) "-" hour "-" slot)))
     js->clj
     find-bounding-rects
     first)))


(rf/reg-sub
 ::last-collision
 (fn [db _]
   (get-in db [:roster/data :mouse :last-collision])))


(rf/reg-sub
 ::collision
 (fn [[_ id]]
   [(rf/subscribe [::mouse])])
 (fn [[mouse ] [_ id]]
   ;;perf optimization
   (if-not (:down mouse)
     nil
     (if-let [c (collides? mouse (find-data-entry-by-key (:last-collision mouse)))]
       (:last-collision mouse)
       ;;else
       (let [hour-cells (find-bounding-rects (.getElementsByClassName js/document "roster-timeslots"))]
         (if-let [overlapping-hour-cell (find-collision mouse hour-cells)]
           ;;find out which timeslot it is
           (let [timeslot-cells     (find-timeslot-cells overlapping-hour-cell)
                 bounding-rects     (find-bounding-rects timeslot-cells)
                 colliding-timeslot (find-collision mouse bounding-rects)]
             (extract-time-key-from-class colliding-timeslot))
           ;;else
           nil))))))

(rf/reg-sub
 ::lookup-table
 (fn [[_ data-sub]]
   (rf/subscribe [::records data-sub]))
 (fn [records] ;; creates a datastructure to quickly find a record by
   (reduce
    (fn [acc {:keys [day hour timeslot] :as r}]
      (update acc [day hour timeslot] conj r))
    {}
    records)))

(rf/reg-sub
 ::data-for-slot
 (fn [[_ id data-sub day hour slot] _]
   [(rf/subscribe [::options id])
    (rf/subscribe [::lookup-table data-sub])])
 (fn db [_ id day hour slot]
   (fn [[options records] [_ id _ day hour timeslot]]
     (get records [day hour timeslot]))))

;; ===== DEMO subs ======

(rf/reg-sub
 ::my-get-data
 (fn [db _]
   (:data db)))
