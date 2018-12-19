(ns cashflow-app.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [cashflow-app.event-handlers :as events]
            [cashflow-app.subscriptions :as subscriptions]
            [cashflow-app.views :as views]))

(defn ^:export init []
  (rf/dispatch-sync [:initialise-db])
  (rf/dispatch-sync [:request-data])
  (reagent/render [views/main] (js/document.getElementById "app")))
