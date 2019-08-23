(ns node-clj.core
  (:require [cljs.nodejs :as nodejs]
            [clojure.string :as string]))

(nodejs/enable-util-print!)

(defonce express (nodejs/require "express"))
(defonce http (nodejs/require "http"))
(defonce server-port 3000)

;; app gets redefined on reload
(def app (express))

;; req.params[key]
(defn req-params [req key]
  (aget req "params" key))

(defn factorial [n]
  (loop [current n fact 1]
    (if (= current 1)
      fact
      (recur (dec current) (* fact current)))))

(. app (get "/hello"
            (fn [_ res] (. res (send "Hello world")))))

(. app (get "/user/:name"
            (fn [req res] (. res (send (req-params req "name"))))))

(. app (get "/fact/:number"
            (fn [req res]
              (let [number (int (req-params req "number"))]
                (. res (send (string/join ["Factorial(" number ") = " (factorial number)])))))))
(def -main
  (.listen app server-port (fn [] (println "Server running on" server-port))))

(set! *main-cli-fn* -main)
