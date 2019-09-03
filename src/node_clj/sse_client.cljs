(ns node-clj.sse-client
  (:require [cljs.nodejs :as nodejs]
            [goog.string :as string]
            goog.string.format))

(nodejs/enable-util-print!)

(defonce channels (atom #{}))
(declare close)

(defn initialize [req res]
  (let [channel {:request req :response res}]
    (swap! channels conj channel)
    (.setNoDelay req.socket true)
    (doto res
      (.status 200)
      (.set (js-obj "Cache-Control" "no-cache, no-transform"
                    "Content-Type" "text/event-stream"
                    "Connection" "keep-alive"))
      (.write ":ok\n\n"))))

(defn send [channel event message]
  (-> (channel :response)
      (.write (string/format "event: %s\ndata: %s\n\n" event message))))

(defn send-all [event message]
  (doseq [channel @channels]
    (send channel event message)))

(defn close [channel]
  (let [res (channel :response)]
    (.end res)
    (swap! channels disj channel)))

(defn alive? [channel]
  (-> channel
      (:response)
      (aget "connection" "destroyed")
      not))

(defn heartbeat [channel]
  {:pre [(alive? channel)]}
  (send channel "ping" "ping"))

(defn start-heartbeat []
  ;; heartbeat
  (js/setInterval
   (fn []
     (doseq [channel @channels]
       (try (heartbeat channel)
            (catch js/Error _
              (close channel))))) 5000))

(defn teardown []
  ;; teardown
  (doseq [channel @channels]
    (close channel)))
