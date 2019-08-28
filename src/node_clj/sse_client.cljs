(ns node-clj.sse-client
  (:require [cljs.nodejs :as nodejs]
            [goog.string :as gstring]
            goog.string.format))

(nodejs/enable-util-print!)

(defn initialize [req res]
  (. req.socket (setNoDelay true))
  (doto res
    (.status 200)
    (.set (js-obj "Cache-Control" "no-cache, no-transform"
                  "Content-Type" "text/event-stream"
                  "Connection" "keep-alive"))
    (.write ":ok\n\n"))
  {:request req :response res})

(defn send [channel message]
  (println message)
  (let [res (channel :response)]
    (doto res
      (.write "event: ping\n")
      (.write (gstring/format "data: {\"message\": \"%s\"}" message))
      (.write "\n\n"))))

(defn close [channel]
  (let [res (channel :response)]
    (. res (end))))
