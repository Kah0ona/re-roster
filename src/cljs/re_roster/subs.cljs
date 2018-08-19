(ns re-roster.subs
  (:require
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
 ::lookup-table
 (fn [[_ data-sub]]
   (println data-sub)
   (rf/subscribe [::records data-sub]))
 (fn [records] ;; creates a datastructure to quickly find a record by
   (println records)
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
     (println options)
     (println records)
     (get records [day hour timeslot]))))

;;; ---todo move elsewhere

(rf/reg-sub
 ::my-get-data
 (fn [db _]
   (println "jo")
   (:data db)))
