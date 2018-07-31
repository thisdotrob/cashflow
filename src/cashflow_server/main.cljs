(ns cashflow-server.main
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >!] :as a]
            ["https" :as https]
            ["fs" :as fs]
            ["express" :as express]))

(defn error-handler [err, req, res, next]
  (-> res
      (.status 500)
      (.send err.message)))

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

(defn fetch-amex-transactions []
  (let [ch (a/chan)]
    (.readFile fs "amex.csv"
                  "utf8"
                  (fn [err data]
                    (if (nil? err)
                      (go (>! ch data))
                      (throw "Error reading file"))))
    ch))

(defn starling-transactions [req res]
  (go
    (let [starling-transactions (<! (fetch-starling-transactions))]
      (.json res (.parse js/JSON starling-transactions)))))

(defn amex-transactions [req res]
  (go 
    (let [amex-transactions (<! (fetch-amex-transactions))]
      (.send res amex-transactions))))

(def app
  (-> (express)
      (.get "/starling-transactions" starling-transactions)
      (.get "/amex-transactions" amex-transactions)
      (.use error-handler)))

(defn start-server []
  (println "Starting server")
  (.listen app 3000 #(println "Cashflow server listening on port 3000")))

(defonce server (atom nil))

(defn start! []
  (reset! server (start-server)))

(defn stop! []
  (.close @server)
  (reset! server nil))
