(ns cashflow-server.recurring
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!]]
            [clojure.string :as string]
            ["fs" :as fs]
            [cashflow-server.date :as date]
            [cashflow-server.utils :as utils]))

(defn future-transactions [transaction today]
  (map (fn [months-to-add]
         {:date      (-> today
                         (date/add-months months-to-add)
                         (date/set-day (:day transaction)))
          :narrative (:narrative transaction)
          :amount    (:amount transaction)})
       (range (if (< (:day transaction) (date/get-day today))
                1 0)
              (if (< (:day transaction) (date/get-day today))
                11 10))))

(defn transactions [{:keys [RECURRING_TRANSACTIONS_FILENAME]}]
  (go
    (let [json-str (<! (utils/js-invoke-async fs
                                              "readFile"
                                              RECURRING_TRANSACTIONS_FILENAME
                                              "utf8"))
          parsed (js->clj (js-invoke js/JSON "parse" json-str)
                          :keywordize-keys
                          true)]
      (map future-transactions parsed))))
