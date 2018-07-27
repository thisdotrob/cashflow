(ns cashflow-server.main
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >!] :as a]
            ["https" :as https]
            ["express" :as express]))

(defn error-handler [err, req, res, next]
  (-> res
      (.status 500)
      (.send "Something broke!")))

(defn status [req res]
  (.json res (clj->js {:service "cashflow-server" :status "green"})))

(def token (aget (.-env js/process) "STARLING_TOKEN"))

(defn fetch-starling-transactions []
  (let [ch (a/chan)
        opts {:hostname "api.starlingbank.com"
              :path "/api/v1/transactions"
              :headers {:Authorization (str "Bearer " token)}}]
    (.get https
          (clj->js opts)
          (fn [res] 
            (.setEncoding res "utf8")
            (if (not= 200 (.-statusCode res)) (throw "Non 200 received from Starling"))
            (.on res "data" #(go (>! ch %)))
            (.on res "end" #(go (a/close! ch)))))
    (a/reduce #(str %1 %2) "" ch)))

(defn transactions [req res]
  (go
    (let [starling-transactions (<! (fetch-starling-transactions))]
      (.json res (.parse js/JSON starling-transactions)))))

(def app
  (-> (express)
      (.get "/transactions" transactions)
      (.use error-handler)))

(defn start-server []
  (println "Starting server")
  (.listen app 3000 #(println "Cashflow server listening on port 3000")))

;;(defn start! []
;;  (go (println (<! (fetch-starling-transactions)))))
;;
;;(defn stop! [])

(defonce server (atom nil))

(defn start! []
  (reset! server (start-server)))

(defn stop! []
  (.close @server)
  (reset! server nil))
