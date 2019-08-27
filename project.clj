(defproject node-clj "0.1.0-SNAPSHOT"
  :description "Nodejs ClojureScript"
  :url "https://github.com/hoangpq/node-clojurescript-app"

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.13"]
            [lein-cljfmt "0.6.4"]]

  :source-paths ["src"]

  :clean-targets ["server.js"
                  "target"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :figwheel true
              :compiler {
                :main node-clj.core
                :asset-path "target/js/compiled/dev"
                :output-to "target/js/compiled/node_clj.js"
                :output-dir "target/js/compiled/dev"
                :target :nodejs
                :optimizations :none
                :source-map true}}
             {:id "prod"
              :source-paths ["src"]
              :compiler {
                :output-to "server.js"
                :output-dir "target/js/compiled/prod"
                :target :nodejs
                :optimizations :simple
                :fn-invoke-direct true
                :optimize-constants true
                :hashbang false
                :parallel-build true
                :source-map true
                :pretty-print  false}}]}

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.13"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
