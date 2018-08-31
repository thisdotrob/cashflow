(ns cashflow-app.event-handlers
  (:require [re-frame.core :as rf]
            [cashflow-app.stub-data :as stub-data]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:amex-transactions []}))

(rf/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(rf/reg-event-db
  ::set-data
  (fn [db [_ data-name]]
    (case data-name
      :amex-transactions-data (assoc db
                                     :amex-transactions
                                     stub-data/amex-transactions)
      db)))
