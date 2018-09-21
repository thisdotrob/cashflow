(ns cashflow-app.views
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [cashflow-app.subscriptions :as subscriptions]
            [cashflow-app.routes :as routes]))

(defn nav-section []
  [:div
   [:ul
    [:li [:a {:href (routes/url-for :starling-transactions-and-balances)} "starling transactions and balances"]]
    [:li [:a {:href (routes/url-for :amex-transactions)} "amex transactions"]]
    [:li [:a {:href (routes/url-for :starling-transactions)} "starling transactions"]]
    [:li [:a {:href (routes/url-for :recurring-transactions)} "recurring transactions"]]
    [:li [:a {:href (routes/url-for :cashflow)} "cashflow"]]]])

(defn home-panel []
  [:div (str "This is the Home Page.")
   [nav-section]])

(def transactions-header
  [:thead
   [:tr
    [:td "Source"]
    [:td "Date"]
    [:td "Desc"]
    [:td "Amount"]]])

(defn transactions-row [{:as data :keys [source id date narrative amount]}]
  [:tr
   [:td source]
   [:td date]
   [:td narrative]
   [:td amount]])

(defn transactions-table [transactions]
  [:table {:style {:width "50%"}}
   transactions-header
   [:tbody
    (for [transaction transactions]
      ^{:key (:id transaction)} [transactions-row transaction])]])

(def transactions-and-balances-header
  [:thead
   [:tr
    [:td "Source"]
    [:td "Date"]
    [:td "Desc"]
    [:td "Amount"]
    [:td "Balance"]
    [:td "Start Transaction?"]]])

(defn display-date [iso-string-date]
  (-> iso-string-date
      (string/replace #"T" " ")
      (string/replace #"Z" " ")
      (subs 0 16)))

(defn transactions-and-balances-row [{:as data :keys [source id date narrative amount balance]}]
  [:tr
   [:td source]
   [:td (display-date date)]
   [:td narrative]
   [:td amount]
   [:td balance]
   [:td {:on-click #(rf/dispatch [:set-computed-balance-start-id id])} "X"]])

(defn transactions-and-balances-table [transactions-and-balances]
  [:table {:style {:width "50%"}}
   transactions-and-balances-header
   [:tbody
    (for [transaction-and-balance transactions-and-balances]
      ^{:key (:id transaction-and-balance)} [transactions-and-balances-row transaction-and-balance])]])

(defn starling-transactions-panel []
  (let [transactions @(rf/subscribe [::subscriptions/starling-transactions])]
    [:div "This is the Starling Transactions Page."
     [transactions-table transactions]
     [nav-section]]))

(defn amex-transactions-panel []
  (let [transactions @(rf/subscribe [::subscriptions/amex-transactions])]
    [:div "This is the Amex Transactions Page."
     [transactions-table transactions]
     [nav-section]]))

(defn starling-transactions-and-balances-panel []
  (let [transactions-and-balances @(rf/subscribe [::subscriptions/starling-transactions-and-balances])]
    [:div "This is the Starling Transactions and Balances Page."
     [transactions-and-balances-table transactions-and-balances]
     [nav-section]]))

(defn recurring-transactions-panel []
  (let [transactions @(rf/subscribe [::subscriptions/recurring-transactions])]
    [:div "This is the Recurring Transactions Page."
     [transactions-table transactions]
     [nav-section]]))

(defn cashflow-panel []
  (let [transactions-and-balances @(rf/subscribe [::subscriptions/cashflow-transactions-and-balances])]
    [:div "This is the Cashflow Page."
     [transactions-and-balances-table transactions-and-balances]
     [nav-section]]))

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :cashflow-panel [cashflow-panel]
    :amex-transactions-panel [amex-transactions-panel]
    :starling-transactions-panel [starling-transactions-panel]
    :starling-transactions-and-balances-panel [starling-transactions-and-balances-panel]
    :recurring-transactions-panel [recurring-transactions-panel]
    [:div]))

(defn main-panel []
  (let [active-panel (rf/subscribe [::subscriptions/active-panel])]
    [panels @active-panel]))
