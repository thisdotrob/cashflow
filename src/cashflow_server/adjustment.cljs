(ns cashflow-server.adjustment
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!]]
            ["fs" :as fs]
            [cashflow-server.utils :as utils]))

(defn json->clj [json]
  (js->clj (js-invoke js/JSON "parse" json) :keywordize-keys true))

(defn read-file-async [filename]
  (utils/js-invoke-async fs "readFile" filename "utf8"))

(defn assoc-id [{:as transaction :keys [narrative amount date]}]
  (assoc transaction :id (str narrative amount date)))

(defn assoc-source [transaction]
  (assoc transaction :source "Adjustment"))

(defn transactions [{:keys [ADJUSTMENT_TRANSACTIONS_FILENAME]}]
  (go (->> ADJUSTMENT_TRANSACTIONS_FILENAME
           read-file-async
           <!
           json->clj
           (map assoc-id)
           (map assoc-source))))

