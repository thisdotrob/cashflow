(ns cashflow-app.event-handlers
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(rf/reg-event-db
 :initialise-db
 (fn-traced [_ _]
   {:amex-transaction-inline-start-date "2018-08-19"
    :adjustment-transactions []
    :amex-repayment-inline-end-date "2018-08-02"
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

(def adjustment-transactions-data-http-opts {:method          :get
                                            :uri             "http://localhost:3000/transactions/adjustment"
                                            :response-format (ajax/json-response-format {:keywords? true})
                                            :on-success      [:http-fetch-success :adjustment-transactions]
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
     :adjustment-transactions-data             {:db db
                                                :http-xhrio adjustment-transactions-data-http-opts}
     :cashflow-data                            {:db db
                                                :http-xhrio [adjustment-transactions-data-http-opts
                                                             amex-transactions-data-http-opts
                                                             starling-transactions-and-balances-data-http-opts
                                                             recurring-transactions-data-http-opts]}
     {:db db})))

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
