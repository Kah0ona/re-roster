(defproject re-roster "0.1.0"
  :description "A clojurescript re-frame component that renders an interactive week roster."
  :url "https://github.com/Kah0ona/re-datagrid.git"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.5"]
                 [re-pressed "0.2.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [re-frame-utils "0.1.0-SNAPSHOT"]
                 [breaking-point "0.1.2"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [day8.re-frame/re-frame-10x "0.3.3"]
                   [day8.re-frame/tracing "0.5.1"]
                   [figwheel-sidecar "0.5.16"]
                   [cider/piggieback "0.3.5"]]

    :plugins [[lein-figwheel "0.5.16"]
              [lein-doo "0.1.8"]]}
   :prod { :dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "re-roster.core/mount-root"}
     :compiler     {:main                 re-roster.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame-10x.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_"        true
                                           "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            re-roster.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          re-roster.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}
    ]}

  )
