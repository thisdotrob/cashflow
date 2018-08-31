(ns cashflow-app.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [cashflow-app.views :as views]
            [cashflow-app.routes :as routes]))

(defn ui []
  [:div
   [views/main-panel]])

(defn render []
  (reagent/render [ui] (js/document.getElementById "app")))

(defn ^:export init []
  (js/console.log "init")
  (routes/app-routes)
  (rf/dispatch-sync [:initialize])
  (render))
