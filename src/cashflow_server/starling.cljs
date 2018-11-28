(ns cashflow-server.starling
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.string :as gstring]
            goog.string.format
            [cljs.core.async :as async :refer [<!]]
            [clojure.string :as string]
            ["fs" :as fs]
            [cashflow-server.date :as date]
            [cashflow-server.utils :as utils]))

(defn starling-transaction->transaction-and-balance [transaction]
  {:source "Starling"
   :id (get transaction "id")
   :date (get transaction "created")
   :narrative (get transaction "narrative")
   :amount (gstring/format "%.2f" (get transaction "amount"))
   :balance (gstring/format "%.2f" (get transaction "balance"))})

(defn starling-savings-goal->savings-goal [savings-goal]
  {:uid (get savings-goal "uid")
   :name (get savings-goal "name")
   :target (get-in savings-goal ["target" "minorUnits"])
   :total-saved (get-in savings-goal ["totalSaved" "minorUnits"])})

(defn starling-recurring-transfer->recurring-transfer [transfer]
  {:transfer-uid (get transfer "transferUid")
   :recurrence-rule {:start-date (get-in transfer
                                         ["recurrenceRule" "startDate"])
                     :frequency (get-in transfer
                                        ["recurrenceRule" "frequency"])
                     :interval (get-in transfer
                                        ["recurrenceRule" "interval"])
                     :count (get-in transfer
                                        ["recurrenceRule" "count"])
                     :until-date (get-in transfer
                                        ["recurrenceRule" "untilDate"])
                     :week-start (get-in transfer
                                        ["recurrenceRule" "weekStart"])
                     :days (get-in transfer
                                        ["recurrenceRule" "days"])
                     :month-day (get-in transfer
                                        ["recurrenceRule" "monthDay"])
                     :month-week (get-in transfer
                                        ["recurrenceRule" "monthWeek"])}
   :amount (get-in transfer ["currencyAndAmount" "minorUnits"])})

(defn assoc-recurring-transfer [token savings-goal]
  (go
    (if (= (:total-saved savings-goal) (:target savings-goal))
      savings-goal ;; return unmodified since making a request will 404
      (->> {:hostname "api.starlingbank.com"
           :path (str "/api/v1/savings-goals/"
                      (:uid savings-goal)
                      "/recurring-transfer")
           :headers {:Authorization (str "Bearer " token)}}
          utils/https-get-async
          <!
          (.parse js/JSON)
          js->clj
          starling-recurring-transfer->recurring-transfer
          (assoc savings-goal :recurring-transfer)))))

(defn recurrence-rule->payment-dates
  [{:keys [count frequency interval start-date]}]
  (let [add-fn (if (= "MONTHLY" frequency)
                 date/add-months
                 date/add-weeks)
        intervals (map #(* interval %) (range 0 count))]
    (map #(str (add-fn start-date %))
         intervals)))

(defn transaction [narrative amount date]
  {:source "Starling"
   :narrative narrative
   :amount amount
   :date (str date "T23:59:59.999Z")
   :id (str narrative
            amount
            date)})

(defn savings-goal->future-transactions
  [{:keys [recurring-transfer name target]}]
  (let [instalment-amount (/ (/ target
                                (get-in recurring-transfer
                                        [:recurrence-rule
                                         :count]))
                             -100)]
    (->> recurring-transfer
         :recurrence-rule
         recurrence-rule->payment-dates
         (map (partial transaction name instalment-amount)))))

(defn savings-goals [{:keys [STARLING_TOKEN]}]
  (go
    (->> {:hostname "api.starlingbank.com"
          :path "/api/v1/savings-goals"
          :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}
         utils/https-get-async
         <!
         (.parse js/JSON)
         js->clj
         (#(get % "savingsGoalList"))
         (map starling-savings-goal->savings-goal)
         (map (partial assoc-recurring-transfer STARLING_TOKEN))
         async/merge
         (async/into [])
         <!)))

(defn future-transactions [env-vars]
  (go (->> (savings-goals env-vars)
           <!
           (mapcat savings-goal->future-transactions))))

(defn past-transactions [{:keys [STARLING_TOKEN]}]
  (go
    (->> {:hostname "api.starlingbank.com"
          :path "/api/v1/transactions"
          :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}
         utils/https-get-async
         <!
         (.parse js/JSON)
         js->clj
         (#(get-in % ["_embedded" "transactions"]))
         (map starling-transaction->transaction-and-balance))))

(defn starling-scheduled-payment->scheduled-payment [payment]
  {:next-date (get payment "nextDate")
   :amount (get payment "amount")
   :reference (get payment "reference")
   :payment-type (get payment "paymentType")
   :recurrence-rule {:start-date (get-in payment ["recurrenceRule" "startDate"])
                     :interval (get-in payment ["recurrenceRule" "interval"])
                     :count (get-in payment ["recurrenceRule" "count"])
                     :frequency (get-in payment ["recurrenceRule" "frequency"])}})


(defn scheduled-payment->future-transactions
  [{:keys [amount reference recurrence-rule] :as x}]
  (if (nil? (:count recurrence-rule))
    (->> recurrence-rule
          recurrence-rule->payment-dates
          (map (partial transaction (str reference "one-off") amount)))
    (if (= 1 (:count recurrence-rule))
     [(transaction reference amount (:start-date recurrence-rule))]
     (->> recurrence-rule
          recurrence-rule->payment-dates
          (map (partial transaction reference amount))))))

(defn scheduled-payments [{:keys [STARLING_TOKEN nilkey]}]
  (go
    (->> {:hostname "api.starlingbank.com"
          :path "/api/v1/payments/scheduled"
          :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}
         utils/https-get-async
         <!
         (.parse js/JSON)
         js->clj
         (#(get-in % ["_embedded" "paymentOrders"]))
         (filter #(> (get % "nextDate") (date/today)))
         (map starling-scheduled-payment->scheduled-payment)
         (mapcat scheduled-payment->future-transactions))))
