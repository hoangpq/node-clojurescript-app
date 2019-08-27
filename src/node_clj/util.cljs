(ns node-clj.util
  (:require [cljs.nodejs :as nodejs]
            [clojure.string :as string]
            [cljs.core.async
             :refer [<! chan put! close! onto-chan to-chan]])
  (:require-macros
   [cljs.core.async.macros :as m :refer [go]]))

(defonce pg (nodejs/require "pg"))

(defn connect-db []
  (-> {"host"     "localhost"
       "port"     5432
       "database" "postgres"
       "ssl"      false}
      (clj->js)
      (pg.Pool.)))

(def pool (connect-db))

(defn async-query [q]
  (let [channel (chan)]
    (. pool (query q (fn [err result]
                       (if err
                         (put! channel (js-obj "error" err))
                         (put! channel result)))))
    channel))

(defn factorial [n]
  (loop [current n fact 1]
    (if (= current 1)
      fact
      (recur (dec current) (* fact current)))))

(defn format-error [e]
  (clj->js {:error 1 :message (aget e "message")}))

(defn format-result [result]
  (let [r (aget result "rows")]
    (clj->js {:data (or r [])})))

(defn query [q cb]
  (go
    (let [result (<! (async-query q)) e (aget result "error")]
      (if e
        (cb (format-error e) nil)
        (cb nil (format-result result))))))

(defn pg-client []
  (let [channel (chan)]
    (. pool (connect
             (fn [_ client _]
               (put! channel client))))
    channel))

(defn pg-listen [channel cb]
  (go (let [client (<! (pg-client))]
        (. client (on "notification" cb))
        (. client (query (string/join " " ["LISTEN" channel]) (fn [] {}))))))
