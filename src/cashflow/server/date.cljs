(ns cashflow.server.date
  (:require ["date-fns" :as date]))

(defn date->str [d]
  (date/format d "YYYY-MM-DD"))

(defn add-months [date amount]
  (-> date
      (date/addMonths amount)
      date->str))

(defn add-weeks [date amount]
  (-> date
      (date/addWeeks amount)
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

(defn next-day-of-week [day-of-week]
  (let [today (js/Date.)
        day-today (date/getDay today)
        day-diff (- day-of-week day-today)
        abs-day-diff (max day-diff (- day-diff))
        days-to-add (cond
                      (= day-diff 0) 7
                      (< day-diff 0) (- 7 abs-day-diff)
                      :else day-diff)]
    (-> today
        (date/addDays days-to-add)
        date->str)))

(defn in-future? [d]
  (date/isAfter d (js/Date.)))
