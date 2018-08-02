(ns cashflow-server.amex
  (:require ["fs" :as fs]
            [cashflow-server.utils :as utils]))

(defn transactions [{:keys [AMEX_TRANSACTIONS_FILENAME]}]
  (utils/js-invoke-async fs "readFile" AMEX_TRANSACTIONS_FILENAME "utf8"))
