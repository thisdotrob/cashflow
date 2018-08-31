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
