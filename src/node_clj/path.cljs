(ns node-clj.path
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(defonce path (nodejs/require "path"))

(defn resource [& args]
  (let [root (.join path js/__dirname "resources/public")]
    (apply path.join (conj args root))))
