(ns cashflow-server.routes
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cashflow-server.starling :as starling]
            [cashflow-server.amex :as amex]
            [cashflow-server.recurring :as recurring]
            [cashflow-server.adjustment :as adjustment]
            [cljs.core.async :refer [<!]]))

(defn route [env-vars f]
  (fn [req res] (go (.send res (clj->js (<! (f env-vars)))))))

(defn initialize [env-vars]
  {:starling-scheduled-payments (route env-vars starling/scheduled-payments)
   :transactions {:past {:adjustment (route env-vars adjustment/transactions)
                         :amex       (route env-vars amex/transactions)
                         :starling   (route env-vars starling/past-transactions)}
                  :future {:starling (route env-vars starling/future-transactions)
                           :recurring (route env-vars recurring/transactions)}}})
