(ns cashflow-app.views
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [cashflow-app.subscriptions :as subscriptions]
            [cashflow-app.routes :as routes]))

(defn home-panel []
  [:div (str "This is the Home Page.")
   [:div [:a {:href (routes/url-for :about)} "go to About Page"]]])

(defn about-panel []
  [:div "This is the About Page."
   [:div [:a {:href (routes/url-for :home)} "go to Home Page"]]])

(defn transaction-row [{:as data :keys [id date narrative amount]}]
  [:tr
   [:td date]
   [:td narrative]
   [:td amount]])

(defn transactions-table [transactions]
  [:table {:style {:width "100%"}}
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
     [transactions-table transactions]]))

(defn starling-transactions-panel []
  (let [transactions @(rf/subscribe [::subscriptions/starling-transactions])]
    [:div "This is the Starling Transactions Page."
     [transactions-table transactions]]))

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    :amex-transactions-panel [amex-transactions-panel]
    :starling-transactions-panel [starling-transactions-panel]
    [:div]))

(defn main-panel []
  (let [active-panel (rf/subscribe [::subscriptions/active-panel])]
    [panels @active-panel]))
