(ns cashflow-server.monthly-amounts
  (:require ["fs" :as fs]
            [cashflow-server.utils :as utils]))

(defn user [{:keys [MONTHLY_AMOUNTS_FILENAME]}]
  (utils/js-invoke-async fs "readFile" MONTHLY_AMOUNTS_FILENAME "utf8"))
