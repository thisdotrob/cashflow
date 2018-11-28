(ns cashflow-app.event-handlers
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(rf/reg-event-db
 :initialise-db
 (fn [_ _]
   {:amex-repayment-inline-end-date "2018-10-16"
    :adjustment-transactions []
    :amex-transactions []
    :starling-transactions-and-balances []
    :recurring-transactions []}))

(rf/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(def amex-transactions-data-http-opts
  {:method          :get
   :uri             "http://localhost:3000/transactions/past/amex"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:http-fetch-success :amex-transactions]
   :on-failure      [:http-fetch-fail]})

(def starling-transactions-and-balances-data-http-opts
  {:method          :get
   :uri             "http://localhost:3000/transactions/past/starling"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:http-fetch-success :starling-transactions-and-balances]
   :on-failure      [:http-fetch-fail]})

(def recurring-transactions-data-http-opts
  {:method          :get
   :uri             "http://localhost:3000/transactions/future/recurring"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:http-fetch-success :recurring-transactions]
   :on-failure      [:http-fetch-fail]})

(def adjustment-transactions-data-http-opts
  {:method          :get
   :uri             "http://localhost:3000/transactions/past/adjustment"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:http-fetch-success :adjustment-transactions]
   :on-failure      [:http-fetch-fail]})

(rf/reg-event-fx
 :request-active-data
 (fn [{:keys [db]} [_ data-name]]
   (case data-name
     :amex-transactions-data
     {:db db
      :http-xhrio amex-transactions-data-http-opts}

     :starling-transactions-data
     {:db db
      :http-xhrio starling-transactions-and-balances-data-http-opts}

     :recurring-transactions-data
     {:db db
      :http-xhrio recurring-transactions-data-http-opts}

     :adjustment-transactions-data
     {:db db
      :http-xhrio adjustment-transactions-data-http-opts}

     :cashflow-data
     {:db db
      :http-xhrio [adjustment-transactions-data-http-opts
                   amex-transactions-data-http-opts
                   starling-transactions-and-balances-data-http-opts
                   recurring-transactions-data-http-opts]}
     {:db db})))

(rf/reg-event-db
  :http-fetch-success
  (fn [db [_ db-key data]]
    (assoc db db-key data)))

(rf/reg-event-db
  :http-fetch-fail
  (fn [db [_ error-map]]
    (assoc db :http-error error-map)))
