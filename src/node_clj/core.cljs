(ns node-clj.core
  (:require [cljs.nodejs :as nodejs]
            [node-clj.util :as util]
            [node-clj.sse-client :as sse-client]
            [goog.string :as string]
            goog.string.format))

(nodejs/enable-util-print!)

(defonce path (nodejs/require "path"))
(defonce express (nodejs/require "express"))
(defonce server-port 9000)

(def resource-path (. path (join js/__dirname "resources/public")))
(def app (express))

(defn query [req res]
  (util/query (aget req "query" "q")
              (fn [err result]
                (if-not err
                  (. res (json result))
                  (. res (json err))))))

(defn index [_ res]
  (. res (sendFile (. path (join resource-path "index.html")))))

(defn sse [req res]
  (sse-client/initialize req res))

(defn div-test [req res]
  (let [params (aget req "params")
        x (int (aget params "x"))
        y (int (aget params "y"))]
    (try
      ;; try
      (->> (js-obj "data" (test-fn x y))
           (.json res))
      ;; catch
      (catch js/Error _
        (->> (js-obj "message" "Divide by zero!")
             (.json res))))))

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

    (.use app "/static" (.static express resource-path))

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
