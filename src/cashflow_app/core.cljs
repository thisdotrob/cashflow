(ns cashflow-app.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [cashflow-app.subscriptions :as subscriptions]
            [cashflow-app.views :as views]
            [cashflow-app.routes :as routes]))

(defn ui []
  (let [initialising? (rf/subscribe [:initialising?])]
    (if @initialising?
      [:div "Initialising..."]
      [:div [views/main-panel]])))

(defn render []
  (reagent/render [ui] (js/document.getElementById "app")))

(defn ^:export init []
  (routes/app-routes)
  (rf/dispatch-sync [:initialise-db])
  (render))
