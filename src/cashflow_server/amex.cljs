(ns cashflow-server.amex
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require ["fs" :as fs]
            [goog.labs.format.csv :as csv]
            [cljs.core.async :refer [<!]]
            [cashflow-server.utils :as utils]))

(defn transactions [{:keys [AMEX_TRANSACTIONS_FILENAME]}]
  (go (vec (map vec (csv/parse (<! (utils/js-invoke-async fs "readFile" AMEX_TRANSACTIONS_FILENAME "utf8")))))))
