(ns cashflow-app.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(rf/reg-sub
 ::amex-transactions
 (fn [db _]
   (:amex-transactions db)))

(rf/reg-sub
 ::starling-transactions
 (fn [db _]
   (:starling-transactions db)))

(rf/reg-sub
 ::recurring-transactions
 (fn [db _]
   (:recurring-transactions db)))
