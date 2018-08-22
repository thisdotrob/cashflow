(ns cashflow-app.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]
            [cashflow-app.events :as events]))

(def routes ["/" {""                      :home
                  "about"                 :about
                  "transactions/" {"amex" :amex-transactions}}])

(defn- parse-url [url]
  (bidi/match-route routes url))

(defn- dispatch-route [matched-route]
  (let [data-name (keyword (str (name (:handler matched-route)) "-data"))
        panel-name (keyword (str (name (:handler matched-route)) "-panel"))]
    (re-frame/dispatch [::events/set-data data-name])
    (re-frame/dispatch [::events/set-active-panel panel-name])))

(defn app-routes[]
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def url-for (partial bidi/path-for routes))
