(ns cashflow-server.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!] :as a]
            ["https" :as https]))

(defn js-invoke-async [js-obj f-name & args]
  (let [async-ch (a/chan)
        f (partial js-invoke js-obj f-name)
        cb (fn [err result]
             (if (nil? err)
               (go (>! async-ch result))
               (throw err)))]
    (apply f (conj (vec args) cb))
    async-ch))

(defn https-get-async [{:keys [hostname path headers]}]
  (let [ch (a/chan)
        opts {:hostname hostname
              :path path
              :headers headers}]
    (.get https
          (clj->js opts)
          (fn [res] 
            (.setEncoding res "utf8")
            (if (not= 200 (.-statusCode res))
              (throw (str "Non 200 received from: " hostname path)))
            (.on res "data" #(go (>! ch %)))
            (.on res "end" #(go (a/close! ch)))))
    (a/reduce #(str %1 %2) "" ch)))
