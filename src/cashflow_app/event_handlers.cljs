(ns cashflow-app.event-handlers
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [cashflow-app.stub-data :as stub-data]))

(rf/reg-event-db
 :initialise-db
 (fn-traced [_ _]
   {:computed-balance-start-id "10ef32c6-302a-4551-b514-1c78ea2af25d"
    :amex-transactions []
    :starling-transactions-and-balances []
    :recurring-transactions []}))

(rf/reg-event-db
 :set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(def amex-transactions-data-http-opts {:method          :get
                                       :uri             "http://localhost:3000/transactions/amex"
                                       :response-format (ajax/json-response-format {:keywords? true})
                                       :on-success      [:http-fetch-success :amex-transactions]
                                       :on-failure      [:http-fetch-fail]})

(def starling-transactions-and-balances-data-http-opts {:method          :get
                                                        :uri             "http://localhost:3000/transactions-and-balances/starling"
                                                        :response-format (ajax/json-response-format {:keywords? true})
                                                        :on-success      [:http-fetch-success :starling-transactions-and-balances]
                                                        :on-failure      [:http-fetch-fail]})

(def recurring-transactions-data-http-opts {:method          :get
                                            :uri             "http://localhost:3000/transactions/recurring"
                                            :response-format (ajax/json-response-format {:keywords? true})
                                            :on-success      [:http-fetch-success :recurring-transactions]
                                            :on-failure      [:http-fetch-fail]})

(rf/reg-event-fx
 :request-active-data
 (fn-traced [{:keys [db]} [_ data-name]]
   (case data-name
     :amex-transactions-data                   {:db db
                                                :http-xhrio amex-transactions-data-http-opts}
     :starling-transactions-data               {:db db
                                                :http-xhrio starling-transactions-and-balances-data-http-opts}
     :starling-transactions-and-balances-data  {:db db
                                                :http-xhrio starling-transactions-and-balances-data-http-opts}
     :recurring-transactions-data              {:db db
                                                :http-xhrio recurring-transactions-data-http-opts}
     :cashflow-data                            {:db db
                                                :http-xhrio [amex-transactions-data-http-opts
                                                             starling-transactions-and-balances-data-http-opts
                                                             recurring-transactions-data-http-opts]}
     db)))

(rf/reg-event-db
  :http-fetch-success
  (fn-traced [db [_ db-key data]]
    (assoc db db-key data)))

(rf/reg-event-db
  :http-fetch-fail
  (fn-traced [db [_ error-map]]
    (assoc db :http-error error-map)))

(rf/reg-event-db
  :set-computed-balance-start-id
  (fn-traced [db [_ id]]
       (assoc db :computed-balance-start-id id)))
