(ns re-roster.specs.roster
   (:require [clojure.spec.alpha :as s]))

(s/def ::day #{:monday :tuesday :wednesday :thursday :friday :saturday :sunday})
(s/def ::display-fn fn?)
(s/def ::edit-fn fn?)
(s/def ::id keyword?)
(s/def ::hour int?)
(s/def ::hour int?)

(s/def ::calendar-entry
  (s/keys :req-un [::day ::hour ::timeslot]))

(s/def ::options
  (s/keys :req-un [::id ::data-subscription]
          :opt-un [::display-fn ::edit-fn])

  )
