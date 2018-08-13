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
        (.get "/transactions/starling"
              (get-in initialized-routes [:transactions :starling]))
        (.get "/transactions/amex"
              (get-in initialized-routes [:transactions :amex]))
        (.get "/transactions/recurring"
              (get-in initialized-routes [:transactions :recurring]))
        (.use error-handler))))
