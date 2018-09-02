(ns cashflow-app.views
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [cashflow-app.subscriptions :as subscriptions]
            [cashflow-app.routes :as routes]))

(defn nav-section []
  [:div
   [:ul
    [:li [:a {:href (routes/url-for :starling-transactions)} "starling transactions"]]
    [:li [:a {:href (routes/url-for :amex-transactions)} "amex transactions"]]
    [:li [:a {:href (routes/url-for :recurring-transactions)} "recurring transactions"]]]])

(defn home-panel []
  [:div (str "This is the Home Page.")
   [nav-section]])

(defn transaction-row [{:as data :keys [id date narrative amount]}]
  [:tr
   [:td date]
   [:td narrative]
   [:td amount]])

(defn transactions-table [transactions]
  [:table {:style {:width "75%"}}
   [:thead
    [:tr
     [:td "Date"]
     [:td "Desc"]
     [:td "Amount"]]]
   [:tbody
    (for [transaction transactions]
      ^{:key (:id transaction)} [transaction-row transaction])]])

(defn amex-transactions-panel []
  (let [transactions @(rf/subscribe [::subscriptions/amex-transactions])]
    [:div "This is the Amex Transactions Page."
     [transactions-table transactions]
     [nav-section]]))

(defn starling-transactions-panel []
  (let [transactions @(rf/subscribe [::subscriptions/starling-transactions])]
    [:div "This is the Starling Transactions Page."
     [transactions-table transactions]
     [nav-section]]))

(defn recurring-transactions-panel []
  (let [transactions @(rf/subscribe [::subscriptions/recurring-transactions])]
    [:div "This is the Recurring Transactions Page."
     [transactions-table transactions]
     [nav-section]]))

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :amex-transactions-panel [amex-transactions-panel]
    :starling-transactions-panel [starling-transactions-panel]
    :recurring-transactions-panel [recurring-transactions-panel]
    [:div]))

(defn main-panel []
  (let [active-panel (rf/subscribe [::subscriptions/active-panel])]
    [panels @active-panel]))
