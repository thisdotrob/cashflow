(ns cashflow-app.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [cashflow-app.routes :as routes]))

;; -- Domino 1 Event Dispatch

;; -- Domino 2 - Event Handlers

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:amex-transactions []}))

;; -- Domino 4 - Query

(rf/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(rf/reg-sub
 ::amex-transactions
 (fn [db _]
   (:amex-transactions db)))


;; -- Domino 5 - View Functions

;; home

(defn home-panel []
  [:div (str "This is the Home Page.")
   [:div [:a {:href (routes/url-for :about)} "go to About Page"]]])

;; about

(defn about-panel []
  [:div "This is the About Page."
   [:div [:a {:href (routes/url-for :home)} "go to Home Page"]]])

(defn transaction-row [{:as data :keys [id date narrative amount]}]
  [:li
   [:p date]
   [:p narrative]
   [:p amount]])

(defn amex-transactions-panel []
  [:div "This is the Amex Transactions Page."
   [:div
    [:ul
     (for [transaction @(rf/subscribe [::amex-transactions])]
       ^{:key (:id transaction)} [transaction-row transaction])]]])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    :amex-transactions-panel [amex-transactions-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (rf/subscribe [::active-panel])]
    [show-panel @active-panel]))

(defn ui []
  [:div
   [main-panel]])

(defn render []
  (reagent/render [ui]
                  (js/document.getElementById "app")))

(defn ^:export init []
  (js/console.log "init")
  (routes/app-routes)
  (rf/dispatch-sync [:initialize])
  (render))
