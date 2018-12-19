(ns cashflow-app.views
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [cashflow-app.subscriptions :as subscriptions]))

(defn display-date [iso-string-date]
  (-> iso-string-date
      (string/replace #"T" " ")
      (string/replace #"Z" " ")
      (subs 0 16)))

(defn row [{:as data :keys [source id date narrative amount balance]}]
  [:tr
   [:td (display-date date)]
   [:td source]
   [:td narrative]
   [:td amount]
   [:td balance]])

(defn main []
  (let [transactions @(rf/subscribe [:cashflow-transactions-and-balances])]
    [:div
     [:table {:style {:width "100%"}}
      [:thead
       [:tr
        [:td "Date"]
        [:td "Source"]
        [:td "Desc"]
        [:td "Amount"]
        [:td "Balance"]]]
      [:tbody
       (for [t transactions]
         ^{:key (:id t)} [row t])]]]))
