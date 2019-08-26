(ns node-clj.util
  (:require [cljs.nodejs :as nodejs]))

(defonce pg (nodejs/require "pg"))

(def Pool (aget pg "Pool"))
(def pool (Pool. (js-obj "connectionString" "postgresql://odoo:odoo@localhost:5432/odoo12")))

(defn factorial [n]
  (loop [current n fact 1]
    (if (= current 1)
      fact
      (recur (dec current) (* fact current)))))

(defn connect-postgres [q cb]
  (. pool (query q (fn [_ result] (cb (aget result "rows"))))))
