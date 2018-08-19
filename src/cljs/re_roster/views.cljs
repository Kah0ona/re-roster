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
  [e]
  [:div.roster-entry (str e)])

(defn time-slot
  [{:keys [id data-subscription] :as opts} day hour index lookup-table]
  (let [data (get lookup-table [day hour index])]
    [:div
     [:div {:style {:height "64px"}} day " " hour " " index]
     (when (not (empty? data))
       (map-indexed
        (fn [i d]
          ^{:key i}
          [data-entry])
        data))]))

(defn hour-cell
  [opts day hour data]
  [:td.roster-cell {:class (str "hour-" hour " day-" (name day))}
   ;;divide into 4 quarters
   [:div.roster-timeslots
    (map
     (fn [i]
       ^{:key i}
       [:div.roster-timeslot
        {:on-click #(rf/dispatch [::events/handle-cell-click (:id opts) day hour i])}
        [time-slot opts day hour i data]])
     (range 0 4))]])

(defn hour-row
  [opts hour data]
  [:tr.roster-row
   ^{:key :label}
   [:td.roster-cell.hour-label hour]
   (map
    (fn [d]
      ^{:key d}
      [hour-cell opts d hour data])
    weekdays)])

(defn roster
  [{:keys [data-subscription id] :as opts}]
  ;;we need this wrapper subscribe function, to have a known internal API.
  (let [data (rf/subscribe [::subs/lookup-table data-subscription])]
    (fn [{:keys [data-subscription id] :as opts}]
      [:div.roster
       [:p (str @data)]
       [:table.roster-table
        [thead opts]
        [:tbody
         (doall
          (map
           (fn [h]
             ^{:key h}
             [hour-row opts h @data])
           hours))]]])))


;;TODO move to different example/demo namespace
(def opts
  {:id                :my-roster
   :data-subscription [::subs/my-get-data]

   })

(defn main-panel []
  (let [name (rf/subscribe [::subs/name])
        data (rf/subscribe [::subs/my-get-data])]
    (fn []
      [:div
       [:button {:on-click #(rf/dispatch [::events/add-data {:day      :monday
                                                             :hour     3
                                                             :timeslot 2}])} "add data"]
       [:p (str @data)]
       [roster opts]
       ])))



;; --------------------------------------------- todo remove -------------------------

(comment
  #_[:div
   [:h3 (str "screen-width: " @(rf/subscribe [::bp/screen-width]))]
   [:h3 (str "screen: " @(rf/subscribe [::bp/screen]))]]

  (defn display-re-pressed-example []
    (let [re-pressed-example (rf/subscribe [::subs/re-pressed-example])]
      [:div

       [:p
        "Re-pressed is listening for keydown events. However, re-pressed
      won't trigger any events until you set some keydown rules."]

       [:div
        [:button
         {:on-click dispatch-keydown-rules}
         "set keydown rules"]]

       [:p
        [:span
         "After clicking the button, you will have defined a rule that
       will display a message when you type "]
        [:strong [:code "hello"]]
        [:span ". So go ahead, try it out!"]]

       (when-let [rpe @re-pressed-example]
         [:div
          {:style {:padding          "16px"
                   :background-color "lightgrey"
                   :border           "solid 1px grey"
                   :border-radius    "4px"
                   :margin-top       "16px"
                   }}
          rpe])]))

  (defn dispatch-keydown-rules []
    (rf/dispatch
     [::rp/set-keydown-rules
      {:event-keys [[[::events/set-re-pressed-example "Hello, world!"]
                     [{:which 72} ;; h
                      {:which 69} ;; e
                      {:which 76} ;; l
                      {:which 76} ;; l
                      {:which 79} ;; o
                      ]]]

       :clear-keys
       [[{:which 27} ;; escape
         ]]}]))

  )
