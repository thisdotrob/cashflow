(ns cashflow.client.event-handlers
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(rf/reg-event-db
 :initialise-db
 (fn [_ _]
   {:adjustment-transactions []
    :amex-transactions []
    :starling-transactions-and-balances []
    :recurring-transactions []}))

(def amex-transactions-data-http-opts
  {:method          :get
   :uri             "http://localhost:3000/transactions/amex"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:http-fetch-success :amex-transactions]
   :on-failure      [:http-fetch-fail]})

(def starling-transactions-and-balances-data-http-opts
  {:method          :get
   :uri             "http://localhost:3000/transactions/starling"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:http-fetch-success :starling-transactions-and-balances]
   :on-failure      [:http-fetch-fail]})

(def recurring-transactions-data-http-opts
  {:method          :get
   :uri             "http://localhost:3000/transactions/recurring"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:http-fetch-success :recurring-transactions]
   :on-failure      [:http-fetch-fail]})

(def adjustment-transactions-data-http-opts
  {:method          :get
   :uri             "http://localhost:3000/transactions/adjustment"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:http-fetch-success :adjustment-transactions]
   :on-failure      [:http-fetch-fail]})

(rf/reg-event-fx
 :request-data
 (fn [{:keys [db]} [_ data-name]]
   {:db db
    :http-xhrio [adjustment-transactions-data-http-opts
                 amex-transactions-data-http-opts
                 starling-transactions-and-balances-data-http-opts
                 recurring-transactions-data-http-opts]}))

(rf/reg-event-db
  :http-fetch-success
  (fn [db [_ db-key data]]
    (assoc db db-key data)))

(rf/reg-event-db
  :http-fetch-fail
  (fn [db [_ error-map]]
    (assoc db :http-error error-map)))
