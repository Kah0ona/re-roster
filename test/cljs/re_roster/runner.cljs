(ns re-roster.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [re-roster.core-test]))

(doo-tests 're-roster.core-test)
