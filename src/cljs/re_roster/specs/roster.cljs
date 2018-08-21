(ns re-roster.specs.roster
   (:require [clojure.spec.alpha :as s]))

(s/def ::day #{:monday :tuesday :wednesday :thursday :friday :saturday :sunday})
(s/def ::id keyword?)
(s/def ::record-id #(not (nil? %)))
(s/def ::hour #(and (int? %) (>= % 0) (< % 24)))
(s/def ::timeslot #(and (int? %) (>= % 0) (< % 4)))
(s/def ::length int?) ;; length of the block (in number of timeslots)
(s/def ::data-entry-component fn?)
(s/def ::data-subscription vector?)
(s/def ::update-dispatch vector?)

(s/def ::calendar-entry
  (s/keys :req-un [::day ::hour ::timeslot ::length ::record-id]))

(s/def ::options
  (s/keys :req-un [::id
                   ::data-subscription
                   ::update-dispatch
                   ::data-entry-component]
          :opt-un []))
