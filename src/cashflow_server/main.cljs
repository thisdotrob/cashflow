(ns cashflow-server.main
  (:require [cashflow-server.app :as app]
            [cashflow-server.env :as env]))

(defn listening-msg [port]
  (str "cashflow-server listening on port " port))

(defn start-server [{:keys [PORT] :as env-vars}]
  (-> env-vars
      (app/create-app)
      (.listen PORT (println (listening-msg PORT)))))

(defonce server (atom nil))

(def env-keys [:PORT
               :STARLING_HOST
               :STARLING_TOKEN
               :MONTHLY_AMOUNTS_FILENAME
               :AMEX_TRANSACTIONS_FILENAME])

(defn start! []
  (->> env-keys
       env/validate
       start-server
       (reset! server)))

(defn stop! []
  (.close @server)
  (reset! server nil))
