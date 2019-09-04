(ns node-clj.path
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(defonce path (nodejs/require "path"))

(defn resource [& args]
  (let [path-join (partial path.join js/__dirname "resources/public")]
    (apply path-join args)))
