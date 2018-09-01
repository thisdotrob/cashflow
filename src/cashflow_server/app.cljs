(ns cashflow-server.app
  (:require [cashflow-server.routes :as routes]
            ["express" :as express]))

(defn cors-handler [req res next]
  (-> res
      (.header "Access-Control-Allow-Origin"
               "*")
      (.header "Access-Control-Allow-Headers"
               "Origin, X-Requested-With, Content-Type, Accept"))
  (next))

(defn error-handler [err, req, res, next]
  (-> res
      (.status 500)
      (.send err.message)))

(defn create-app [env-vars]
  (let [initialized-routes (routes/initialize env-vars)]
    (-> (express)
        (.use cors-handler)
        (.get "/balances/starling"
              (get-in initialized-routes [:balances :starling]))
        (.get "/transactions/starling"
              (get-in initialized-routes [:transactions :starling]))
        (.get "/transactions/amex"
              (get-in initialized-routes [:transactions :amex]))
        (.get "/transactions/recurring"
              (get-in initialized-routes [:transactions :recurring]))
        (.use error-handler))))
