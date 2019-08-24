(ns node-clj.core
  (:require [cljs.nodejs :as nodejs]
            [goog.string :as gstring]
            goog.string.format))

(nodejs/enable-util-print!)

(defonce express (nodejs/require "express"))
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

(defn divide [req res]
  (let [p (aget req "params")
        number1 (aget p "number1")
        number2 (aget p "number2")]
    (if (zero? (int number2)) (. res (send "Divide by zero!"))
        (. res (send (str (/ (int number1) (int number2))))))))

(. app (get "/hello"
            (fn [_ res] (. res (send "Hello world")))))

(. app (get "/user/:name"
            (fn [req res] (. res (send (req-params req "name"))))))

(. app (get "/fact/:number"
            (fn [req res]
              (let [number (int (req-params req "number"))
                    fact (factorial number)]
                (. res (send (gstring/format "Factorial(%d) = %d" number fact)))))))

(. app (get "/divide/:number1/:number2" divide))

(def -main
  (.listen app server-port (fn [] (println (gstring/format "Server is running on port %d" server-port)))))

(set! *main-cli-fn* -main)
