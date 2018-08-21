(ns re-roster.views
  (:require
   [re-frame.core :as re-frame]
   [re-frame.core :as rf]
   [breaking-point.core :as bp]
   [re-pressed.core :as rp]
   [re-roster.events :as events]
   [re-roster.subs :as subs]))

(def weekdays
  [:monday :tuesday :wednesday :thursday :friday :saturday :sunday])

(def weekday-titles
  ["Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday" "Sunday"])

(def day->name
  (zipmap weekdays weekday-titles))

(defn thead
  [opts]
  [:thead
   [:tr
    ^{:key -1}
    [:th]
    (map
     (fn [t]
       ^{:key t}
       [:th t])
     weekday-titles)]])

(def hours (range 0 24))

(defn data-entry
  [{:keys [id data-entry-component] :as opts}
   {:keys [length day hour timeslot record-id]
    :or   {length 1}
    :as   e}]
  [:div.roster-entry
   {:style {:height (str (* length (/ 51 events/slots-per-hour)) "px")}}
   [:div.roster-entry-top.roster-drag-handle
    {:on-mouse-down #(rf/dispatch [::events/start-drag id [day hour timeslot] :top record-id])}]
   [:div.roster-entry-body
    {:on-mouse-down #(rf/dispatch [::events/start-drag id [day hour timeslot] :move record-id])}
    [data-entry-component e]]
   [:div.roster-entry-bottom.roster-drag-handle
    {:on-mouse-down #(rf/dispatch [::events/start-drag id [day hour timeslot] :bottom record-id])}]])

(defn time-slot
  [{:keys [id data-subscription] :as opts} day hour index lookup-table]
  (let [data (get lookup-table [day hour index])]
    [:div.roster-timeslot
     {:class (str "roster-" id " roster-time-slot-" (name day) "-" hour "-" index)}
     (when (not (empty? data))
       (map-indexed
        (fn [i d]
          ^{:key i}
          [data-entry opts d])
        data))]))

(defn hour-cell
  [opts day hour data]
  [:td.roster-cell
   {:class (str "hour-" hour " day-" (name day))}
   ;;divide into 4 quarters
   [:div.roster-timeslots
    (map
     (fn [i]
       ^{:key i}
       [time-slot opts day hour i data])
     (range 0 4))]])

(defn format-hour
  [h]
  (str h ":00"))

(defn hour-row
  [opts hour data]
  [:tr.roster-row
   ^{:key :label}
   [:td.roster-cell.hour-label (format-hour hour)]
   (map
    (fn [d]
      ^{:key d}
      [hour-cell opts d hour data])
    weekdays)])

(defn drag-mask
  [{:keys [data-subscription id] :as options}]
  (let [f #(rf/dispatch [::events/hover-over id data-subscription])]
    [:div.roster-drag-mask
     {:on-mouse-move f
      :on-mouse-over f}]))

(defn roster
  [{:keys [data-subscription id] :as opts}]
  ;;we need this wrapper subscribe function, to have a known internal API.
  (let [data      (rf/subscribe [::subs/lookup-table data-subscription])
        options   (rf/subscribe [::subs/options id])
        collision (rf/subscribe [::subs/collision id])
        ds        (rf/subscribe [::subs/drag-state id])
        mouse     (rf/subscribe [::subs/mouse])]
    (fn [{:keys [data-subscription id] :as opts}]
      (if (not= opts @options)
        (do
          (rf/dispatch [::events/initialize opts])
          [:div "loading"])
        ;;else
        [:div.roster
         (when (and (:moved? @mouse) (:down @mouse))
           [drag-mask @options])
         [:table.roster-table
          [thead @options]
          [:tbody
           (doall
            (map
             (fn [h]
               ^{:key h}
               [hour-row opts h @data])
             hours))]]]))))

;; ===== DEMO code =====
(def demo-opts
  {:id                :my-roster
   :data-subscription [::subs/my-get-data]
   :update-dispatch   [::events/my-update-dispatch]
   :data-entry-component (fn [e]
                           [:div.my-entry "your code renders this"])})

(defn main-panel []
  (let [name  (rf/subscribe [::subs/name])
        data  (rf/subscribe [::subs/my-get-data])
        mouse (rf/subscribe [::subs/mouse])]
    (fn []
      [:div
       [:div (str @mouse)]
       [:button {:on-click #(rf/dispatch [::events/add-data {:day       :monday
                                                             :hour      3
                                                             :something :else
                                                             :length    4
                                                             :record-id (str (random-uuid))
                                                             :timeslot  2}])} "add data"]
       [:p (str @data)]
       [roster demo-opts]])))
