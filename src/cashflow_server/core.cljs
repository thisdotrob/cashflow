(ns cashflow-server.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cashflow-server.starling :as starling]
            [cashflow-server.amex :as amex]
            [cashflow-server.recurring :as recurring]
            [cashflow-server.adjustment :as adjustment]
            [cljs.core.async :refer [<!]]
            ["express" :as express]
            [cashflow-server.env :as env]))

(defn route [env-vars f]
  (fn [req res] (go (.send res (clj->js (<! (f env-vars)))))))

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

(defn start-server [env-vars]
  (-> (express)
      (.use cors-handler)
      (.get "/transactions/adjustment" (route env-vars adjustment/transactions))
      (.get "/transactions/amex" (route env-vars amex/transactions))
      (.get "/transactions/recurring" (route env-vars recurring/transactions))
      (.get "/transactions/starling" (route env-vars starling/transactions))
      (.use error-handler)
      (.listen 3000 #(println "cashflow-server listening on port 3000"))))

(defonce server (atom nil))

(def env-keys [:STARLING_TOKEN])

(defn start! []
  (->> env-keys
       env/validate
       start-server
       (reset! server)))

(defn stop! []
  (.close @server)
  (reset! server nil))
