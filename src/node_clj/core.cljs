(ns node-clj.core
  (:require [cljs.nodejs :as nodejs]
            [node-clj.util :as util]
            [node-clj.sse-client :as sse-client]
            [goog.string :as gstring]
            goog.string.format))

(nodejs/enable-util-print!)

(defonce path (nodejs/require "path"))
(defonce express (nodejs/require "express"))
(defonce server-port 3000)

(defonce channels (atom #{}))

(def resource-path (. path (join js/__dirname "resources/public")))
(def app (express))

(defn query [req res]
  (util/query (aget req "query" "q")
              (fn [err result]
                (if-not err
                  (. res (json result))
                  (. res (json err))))))

(defn factorial [req res]
  (let [number (int (aget req "params" "number"))
        fact (util/factorial number)]
    (. res
       (send (gstring/format "Factorial(%d) = %d" number fact)))))

(defn index [_ res]
  (. res (sendFile (. path (join resource-path "index.html")))))

(defn sse [req res]
  (let [channel (sse-client/initialize req res)]
    (swap! channels conj channel)))

(defn -main []
  (let [listener (util/create-pg-listener)]
    ;; teardown tcp connection
    (util/handler-process-exit
     (fn [] (println "Teardown all TCP connections")
       (doseq [channel @channels]
         (sse-client/close channel))))

    ;; listen postgres notification
    (. listener (subscribe
                 (fn [message]
                   (doseq [channel @channels] (sse-client/send channel message)))))

    ;; express routing
    (. app (get "/" index))
    (. app (get "/sse" sse))
    (. app (get "/query" query))
    (. app (get "/fact/:number" factorial))

    ;; Express server binding to `server port`
    (. app (listen server-port
                   (fn []
                     (println (gstring/format "Server is running on port %d" server-port)))))))

(set! *main-cli-fn* -main)