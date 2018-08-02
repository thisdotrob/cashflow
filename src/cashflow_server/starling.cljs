(ns cashflow-server.starling
  (:require [cashflow-server.utils :as utils]
            ["fs" :as fs]))

(defn transactions [{:keys [STARLING_HOST STARLING_TOKEN]}]
  (utils/https-get-async {:hostname STARLING_HOST
                          :path "/api/v1/transactions"
                          :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}))
