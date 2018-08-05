(ns cashflow-server.app
  (:require [cashflow-server.routes :as routes]
            ["express" :as express]))

(defn error-handler [err, req, res, next]
  (-> res
      (.status 500)
      (.send err.message)))

(defn create-app [env-vars]
  (let [initialized-routes (routes/initialize env-vars)]
    (-> (express)
        (.get "/starling/transactions"
              (get-in initialized-routes [:transactions :starling]))
        (.get "/amex/transactions"
              (get-in initialized-routes [:transactions :amex]))
        (.get "/user/monthly-amounts"
              (get-in initialized-routes [:monthly-amounts :user]))
        (.use error-handler))))
