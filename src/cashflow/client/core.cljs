(ns cashflow.client.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [cashflow.client.event-handlers :as events]
            [cashflow.client.subscriptions :as subscriptions]
            [cashflow.client.views :as views]))

(defn ^:export init []
  (rf/dispatch-sync [:initialise-db])
  (rf/dispatch-sync [:request-data])
  (reagent/render [views/main] (js/document.getElementById "app")))
