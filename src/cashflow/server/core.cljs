(ns cashflow.server.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cashflow.server.starling :as starling]
            [cashflow.server.amex :as amex]
            [cashflow.server.recurring :as recurring]
            [cashflow.server.adjustment :as adjustment]
            [cashflow.server.one-off :as one-off]
            [cljs.core.async :refer [<!]]
            ["express" :as express]
            [cashflow.server.env :as env]))

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

(defn init-server [env-vars]
  (-> (express)
      (.use cors-handler)
      (.get "/transactions/adjustment" (route env-vars adjustment/transactions))
      (.get "/transactions/one_off" (route env-vars one-off/transactions))
      (.get "/transactions/amex" (route env-vars amex/transactions))
      (.get "/transactions/recurring" (route env-vars recurring/transactions))
      (.get "/transactions/starling" (route env-vars starling/transactions))
      (.use error-handler)))

(defonce server-ref
  (volatile! nil))

(def env-keys [:STARLING_TOKEN])

(defn main [& args]
  (js/console.log "starting server")
  (let [server (->> env-keys
                    env/validate
                    init-server)]
    (.listen server 3000
      (fn [err]
        (if err
          (js/console.error "server start failed")
          (js/console.info "http server running"))))
    (vreset! server-ref server)))

(defn start
  "Hook to start. Also used as a hook for hot code reload."
  []
  (js/console.warn "start called")
  (main))

(defn stop
  "Hot code reload hook to shut down resources so hot code reload can work"
  [done]
  (js/console.warn "stop called")
  (when-some [server @server-ref]
    (.close server
      (fn [err]
        (js/console.log "stop completed" err)
        (done)))))

(js/console.log "__filename" js/__filename)
