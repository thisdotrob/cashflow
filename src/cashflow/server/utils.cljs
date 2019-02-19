(ns cashflow.server.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!] :as a]
            ["fs" :as fs]
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

(defn asyncify-prom [f]
  (fn [& args]
    (let [async-ch (a/chan)
          prom (apply f args)]
      (.then prom (fn [result] (go (>! async-ch result))))
      async-ch)))

(defn asyncify-cb [f]
  (fn [& args]
    (let [async-ch (a/chan)
          cb (fn [err result] (if (nil? err)
                                (go (>! async-ch result))
                                (throw err)))
          args-cb-at-tail (conj (vec args) cb)]
      (apply f args-cb-at-tail)
      async-ch)))

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

(defn json->clj [json]
  (js->clj (js-invoke js/JSON "parse" json) :keywordize-keys true))

(defn read-file-async [filename]
  (js-invoke-async fs "readFile" filename "utf8"))
