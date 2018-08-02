(ns cashflow-server.routes
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cashflow-server.starling :as starling]
            [cashflow-server.amex :as amex]
            [cashflow-server.monthly-amounts :as monthly-amounts]
            [cljs.core.async :refer [<!]]))

(defmacro defn-go [fname args & body]
  `(defn ~fname ~args (go ~@body)))

(defn route [env-vars f]
  (fn [req res] (go (.json res (<! (f env-vars))))))

(defn-go amex-transactions [req res]
  (.json res (<! (amex/transactions {:}))))

(defn initialize [env-vars]
  {:transactions    {:starling (route env-vars starling/transactions)
                     :amex     (route env-vars amex/transactions)}
   :monthly-amounts {:user     (route env-vars monthly-amounts/user)}})
