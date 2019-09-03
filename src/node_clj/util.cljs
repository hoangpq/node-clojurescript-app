(ns node-clj.util
  (:require [cljs.nodejs :as nodejs]
            [node-clj.config :as config]
            [clojure.string :as string]
            [cljs.core.async
             :refer [<! chan put! close! onto-chan to-chan]])
  (:require-macros
   [cljs.core.async.macros :as m :refer [go]]))

(defonce pg (nodejs/require "pg"))
(defonce operators (nodejs/require "rxjs/operators"))
(defonce rxjs (nodejs/require "rxjs"))
(defonce Odoo (nodejs/require "node-odoo"))

(defn get-config []
  (let [config (config/loadConfig)
        get (partial aget config)]
    (fn [& args] (apply get args))))

(defonce jsConfig (get-config))

(defn connect-db []
  (pg.Pool. (jsConfig "db")))

(defn create-odoo-instance []
  (Odoo. (jsConfig "rpc")))

(defn connect-odoo [odoo cb]
  (.connect odoo
            (fn [error]
              (if error (throw error) (cb odoo)))))

(defn connected? [instance]
  (true? (.isConnected instance)))

(defn connect-odoo-async []
  (let [odoo (create-odoo-instance)]
    (fn [cb]
      (if
       (connected? odoo)
        (cb odoo) (connect-odoo odoo cb)))))

(def pool (connect-db))
;; connect to Odoo RPC
(def odoo (connect-odoo-async))

(defn async-query [q]
  (let [channel (chan)]
    (.query pool q (fn [error result]
                     (if error
                       (put! channel (js-obj "error" error))
                       (put! channel result))))
    channel))

(defn format-error [e]
  (clj->js {:error 1 :message (aget e "message")}))

(defn format-result [result]
  (let [r (aget result "rows")]
    (clj->js {:data (or r [])})))

(defn query [q cb]
  (go
    (let [result (<! (async-query q))
          error (aget result "error")]
      (if error
        (cb (format-error error) nil)
        (cb nil (format-result result))))))

(defn message_post [channel-id message cb]
  (odoo
   (fn [instance]
     (.message_post instance channel-id message cb))))

(defn pg-client []
  (let [channel (chan)]
    (.connect pool (fn [_ client _]
                     (put! channel client)))
    channel))

(defn pg-listen [channel cb]
  (go (let [client (<! (pg-client))]
        (.on client "notification" cb)
        (.query client
                (string/join " " ["LISTEN" channel])
                (fn [] ())))))

(defn create-pg-listener []
  (let [subject (rxjs.Subject.)]
    (pg-listen "im_events"
               (fn [msg]
                 (.next subject (aget msg "payload"))))
    (-> (.asObservable subject)
        (.pipe (operators.share)))))

(defn handler-process-exit [cb]
  (. nodejs/process
     (on "SIGINT"
         (fn []
           (cb)
           (.exit nodejs/process 0)))))
