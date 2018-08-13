(ns cashflow-server.recurring
  (:require ["fs" :as fs]
            [cashflow-server.utils :as utils]))

(defn transactions [{:keys [RECURRING_TRANSACTIONS_FILENAME]}]
  (utils/js-invoke-async fs "readFile" RECURRING_TRANSACTIONS_FILENAME "utf8"))
