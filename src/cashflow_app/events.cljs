(ns cashflow-app.events
  (:require [re-frame.core :as re-frame]
            [cashflow-app.stub-data :as stub-data]))

(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
  ::set-data
  (fn [db [_ data-name]]
    (println data-name)
    (case data-name
      :amex-transactions-data
      (assoc db :amex-transactions stub-data/amex-transactions)

      db)))
