(ns cashflow-app.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]
            [cashflow-app.event-handlers :as events]))

(def routes ["/" {""                            :home
                  "cashflow"                    :cashflow
                  "transactions/" {"starling"   :starling-transactions
                                   "amex"       :amex-transactions
                                   "recurring"  :recurring-transactions
                                   "adjustment" :adjustment-transactions}}])

(defn- parse-url [url]
  (bidi/match-route routes url))

(defn- dispatch-route [matched-route]
  (let [data-name (keyword (str (name (:handler matched-route)) "-data"))
        panel-name (keyword (str (name (:handler matched-route)) "-panel"))]
    (re-frame/dispatch [:request-active-data data-name])
    (re-frame/dispatch [:set-active-panel panel-name])))

(defn app-routes []
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def url-for (partial bidi/path-for routes))
