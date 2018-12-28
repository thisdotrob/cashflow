(ns cashflow.client.views
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [cashflow.client.subscriptions :as subscriptions]))

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
  (let [transactions @(rf/subscribe [:cashflow-transactions-and-balances])
        filters @(rf/subscribe [:filters])]
    [:div
     [:div
      [:input {:type "checkbox"
               :on-change #(rf/dispatch [:toggle-filter :amex])
               :checked (:amex filters)}]
      [:label "Amex"]
      [:input {:type "checkbox"
               :on-change #(rf/dispatch [:toggle-filter :starling])
               :checked (:starling filters)}]
      [:label "Starling"]
      [:input {:type "checkbox"
               :on-change #(rf/dispatch [:toggle-filter :recurring])
               :checked (:recurring filters)}]
      [:label "Recurring"]
      [:input {:type "checkbox"
               :on-change #(rf/dispatch [:toggle-filter :adjustments])
               :checked (:adjustments filters)}]
      [:label "Adjustment"]]
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
          ^{:key (:id t)} [row t])]]]]))
