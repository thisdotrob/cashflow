(ns cashflow.client.date
  (:require ["date-fns" :as date]))

(defn is-before? [d1 d2]
  (date/isBefore d1 d2))
