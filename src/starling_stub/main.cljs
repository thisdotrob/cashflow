(ns starling-stub.main
  (:require ["http" :as http]
            ["fs" :as fs]))

(def transactions
  (.readFileSync fs "starling-transactions-stub.json"))

(defonce server (atom nil))

(defn request-handler [req res]
  (.end res transactions))

(defn listening-cb [err]
  (if (nil? err)
    (println "Server listening on 4000")
    (println "Something bad happened." err)))

(defn start! []
  (println "start! called")
  (let [s (.createServer http request-handler)]
    (.listen s "4000" listening-cb)
    (reset! server s)))

(defn stop! []
  (println "stop! called")
  (.close @server)
  (reset! server nil))
