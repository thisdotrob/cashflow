(ns cashflow-app.event-handlers
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [cashflow-app.stub-data :as stub-data]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:amex-transactions []}))

(rf/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(rf/reg-event-fx
 :request-active-data
 (fn [{:keys [db]} [_ data-name]]
   (case data-name
     :amex-transactions-data      {:db db
                                   :http-xhrio {:method          :get
                                                :uri             "http://localhost:3000/transactions/amex"
                                                :response-format (ajax/json-response-format {:keywords? true})
                                                :on-success      [:http-fetch-success :amex-transactions]
                                                :on-failure      [:http-fetch-fail]}}
     :starling-transactions-data  {:db db
                                   :http-xhrio {:method          :get
                                                :uri             "http://localhost:3000/transactions/starling"
                                                :response-format (ajax/json-response-format {:keywords? true})
                                                :on-success      [:http-fetch-success :starling-transactions]
                                                :on-failure      [:http-fetch-fail]}}
     :recurring-transactions-data {:db db
                                   :http-xhrio {:method          :get
                                                :uri             "http://localhost:3000/transactions/recurring"
                                                :response-format (ajax/json-response-format {:keywords? true})
                                                :on-success      [:http-fetch-success :recurring-transactions]
                                                :on-failure      [:http-fetch-fail]}}
     db)))

(rf/reg-event-db
  :http-fetch-success
  (fn [db [_ db-key data]]
    (assoc db db-key data)))

(rf/reg-event-db
  :http-fetch-fail
  (fn [db [_ error-map]]
    (assoc db :http-error error-map)))
