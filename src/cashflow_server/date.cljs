(ns cashflow-server.date
  (:require ["date-fns" :as date]))

(defn date->str [d]
  (date/format d "YYYY-MM-DD"))

(defn add-months [date amount]
  (-> date
      (date/addMonths amount)
      date->str))

(defn set-day [date day]
  (-> date
      (date/setDate day)
      date->str))

(defn get-day [date]
  (date/getDate date))

(defn today []
  (-> (js/Date.)
      date->str))
