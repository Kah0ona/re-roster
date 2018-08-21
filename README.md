# re-roster

A [re-frame](https://github.com/Day8/re-frame) library that shows a week-roster, with an API to schedule things in.
It doesn't have any dates, so it's not a full blown calendar, rather it's a roster.

[See the demo](https://git)

It's pretty alpha, but it works, you can resize appointments, and move them, and add them through a dispatch

Instructions:

```
(ns my-ns
  (:require [re-roster.views :as roster]))

(defn my-component
  []
  (let [options {:id :my-roster-1
                 ;; this should return data in the correct data format (see below)
                 :data-subscription [:my-re-frame-subscription-yielding-data]
                 ;; this dispatch is called when an appointment has changed (moved or resized).
                 ;; it is up to you to update the underlying data, probably such that the data-subscription
                 ;; from above will yield an updated result.
                 ;; re-roster is agnostic as how your data is stored, and puts the responsibility to the user
                 ;; using the :update-dispatch configuration
                 ;; update-dispatch passes in as arguments:
                 ;; - :id of the roster (:my-roster-1 in this case)
                 ;; - a 3-tuple [day hour timeslot] that denotes at which point the drag-resize/drag-move action was released
                 ;;   (ie. it denotes the cell the cursor was on at that point)
                 ;; - the updated record with updated :length, :day, :hour, :timeslot values
                 :update-dispatch [:my-re-frame-dispatch]
                 ;; render something in the body, like this:
                 :data-entry-component (fn [data-entry]
                                        [my-component data-entry])}]
    [roster/roster options]))

;; optional: include the css styles in resources/public/css/style.css in your project to give it a decent rendering.


```
Things to note:
====

The options should adhere to the spec in `re-roster.specs.roster/options`.
Example options:
```
{:id                   :my-roster
 :data-subscription    [::subs/my-get-data]
 :update-dispatch      [::events/my-update-dispatch]
 :data-entry-component (fn [e]
                         [:div.my-entry (str e)])}
```

The data-entry format, ie. the data map for each block, should adhere to the spec in `re-roster.specs.roster/calendar-entry`.
Example data-entry:
```
{:day :monday,
 :hour 4,
 :something :else,
 :length 17,
 :record-id "a91f7a04-52b7-47dc-9dae-87bfed06f9b1",
 :timeslot 3}
```

## Development Mode

### Start Cider from Emacs:

Put this in your Emacs config file:

```
(setq cider-cljs-lein-repl
	"(do (require 'figwheel-sidecar.repl-api)
         (figwheel-sidecar.repl-api/start-figwheel!)
         (figwheel-sidecar.repl-api/cljs-repl))")
```

Navigate to a clojurescript file and start a figwheel REPL with `cider-jack-in-clojurescript` or (`C-c M-J`)

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

### Run tests:

Install karma and headless chrome

```
npm install -g karma-cli
npm install karma karma-cljs-test karma-chrome-launcher --save-dev
```

And then run your tests

```
lein clean
lein doo chrome-headless test once
```

Please note that [doo](https://github.com/bensu/doo) can be configured to run cljs.test in many JS environments (phantom, chrome, ie, safari, opera, slimer, node, rhino, or nashorn).

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
