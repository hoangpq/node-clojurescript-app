(ns node-clj.core
  (:require [cljs.nodejs :as nodejs]
            [goog.string :as gstring]
            [node-clj.util :as util]
            goog.string.format))

(nodejs/enable-util-print!)

(defonce express (nodejs/require "express"))
(defonce server-port 3000)

(def app (express))

(defn sse [req res]
  (. res (set (js-obj "Cache-Control" "no-cache" "Content-Type" "text/event-stream")))
  (if-not (aget req "count") (aset req "count" 1))
  (let [time (. js/Date now)]
    (js/setTimeout (fn [] (doto res
                            (.write "event: ping\n")
                            (.write (gstring/format "data: {\"time\": %d}" time))
                            (.write "\n\n")
                            (.end))) 4000)))

(defn query [req res]
  (util/query (aget req "query" "q")
              (fn [err result] (if-not err (. res (json result)) (. res (json err))))))

(defn factorial [req res]
  (let [number (int (aget req "params" "number"))
        fact (util/factorial number)]
    (. res (send (gstring/format "Factorial(%d) = %d" number fact)))))

(defn -main []
  (. app (get "/hello" (fn [_ res] (js/setTimeout (fn [] (. res (send "ClojureScript!"))) 1000))))
  (. app (get "/query" query))
  (. app (get "/sse" sse))
  (. app (get "/fact/:number" factorial))
  (. app (listen server-port (fn [] (println (gstring/format "Server is running on port %d" server-port))))))

(set! *main-cli-fn* -main)
