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
        (.get "/transactions/starling"
              (get-in initialized-routes [:transactions :starling]))
        (.get "/transactions/past/adjustment"
              (get-in initialized-routes [:transactions :past :adjustment]))
        (.get "/transactions/past/amex"
              (get-in initialized-routes [:transactions :past :amex]))
        (.get "/transactions/past/starling"
              (get-in initialized-routes [:transactions :past :starling]))
        (.get "/transactions/future/starling"
              (get-in initialized-routes [:transactions :future :starling]))
        (.get "/transactions/future/recurring"
              (get-in initialized-routes [:transactions :future :recurring]))
        (.use error-handler))))
