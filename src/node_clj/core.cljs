(ns node-clj.core
  (:require [cljs.nodejs :as nodejs]
            [node-clj.util :as util]
            [node-clj.sse-client :as sse-client]
            [node-clj.path :as path]
            [node-clj.config :as config]
            [goog.string :as string]
            goog.string.format))

(nodejs/enable-util-print!)

(defonce express (nodejs/require "express"))
(defonce server-port 9000)

(def app (express))

(defn db-query [q res]
  {:pre [(or
          (not (clojure.string/blank? q))
          (throw (js/Error. "Query string is required")))]}
  (util/query q (fn [error result]
                  (if-not error
                    (.json res result)
                    (.json res (util/format-error error))))))

(defn query [req res]
  (let [q (aget req "query" "q")]
    (try (db-query q res)
         (catch js/Error error
           (.json res (util/format-error error))))))

(defn index [_ res]
  (.sendFile res (path/resource "index.html")))

(defn sse [req res]
  (sse-client/initialize req res))

(defn message [req res]
  (let [params (aget req "params")
        channel-id (int (aget params "channel"))
        message (aget params "message")]
    (util/message_post channel-id message
                       (fn [] (.send res "Done!!")))))

(defn -main []
  (let [listener (util/create-pg-listener)]
    ;; listen postgres notification
    (.subscribe listener
                (fn [message]
                  (sse-client/send-all "imbus" message)))

    ;; express routing
    (.get app "/" index)
    (.get app "/sse" sse)
    (.get app "/query" query)
    (.get app "/message/:channel/:message" message)

    ;; static assets
    (.use app "/static" (.static express (path/resource)))

    ;; teardown tcp connection
    (util/handler-process-exit
     (fn [] (println "Teardown all TCP connections")
       (sse-client/teardown)))

    ;; binding to server port
    (.listen app server-port
             (fn []
               (println (string/format "Server is running on port %d" server-port))
               (sse-client/start-heartbeat)))))

(set! *main-cli-fn* -main)
