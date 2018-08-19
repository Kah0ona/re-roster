(ns re-roster.events
  (:require
   [re-frame.core :as rf]
   [re-roster.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   ))

(rf/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(rf/reg-event-db
 ::set-re-pressed-example
 (fn [db [_ value]]
   (assoc db :re-pressed-example value)))



;;; ---- todo move to some demo namespace

(rf/reg-event-db
 ::add-data
 (fn [db [_ d]]
   (println (:data db))
   (update db :data conj d)))
