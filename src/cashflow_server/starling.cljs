(ns cashflow-server.starling
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.string :as gstring]
            goog.string.format
            [cljs.core.async :as async :refer [<!]]
            [clojure.string :as string]
            ["fs" :as fs]
            [cashflow-server.date :as date]
            [cashflow-server.utils :as utils]))

(defn savings-goal->recurrence-rule [savings-goal]
  (let [recurring-transfer (get savings-goal "recurringTransfer")
        recurrence-rule (get recurring-transfer "recurrenceRule")]
    {:start-date (get recurrence-rule "startDate")
     :frequency (get recurrence-rule "frequency")
     :interval (get recurrence-rule "interval")
     :count (get recurrence-rule "count")
     :amount (/ (get-in recurring-transfer ["currencyAndAmount" "minorUnits"])
                -100)
     :narrative (get savings-goal "name")}))

(defn scheduled-payment->recurrence-rule [scheduled-payment]
  (let [recurrence-rule (get scheduled-payment "recurrenceRule")]
    {:start-date (get recurrence-rule "startDate")
    :frequency (get recurrence-rule "frequency")
    :interval (get recurrence-rule "interval")
    :count (get recurrence-rule "count")
    :amount (* -1 (get scheduled-payment "amount"))
    :narrative (get scheduled-payment "reference")}))

(defn fetch-past-transactions [{:keys [STARLING_TOKEN]}]
  (go
    (->> {:hostname "api.starlingbank.com"
          :path "/api/v1/transactions"
          :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}
         utils/https-get-async
         <!
         (.parse js/JSON)
         js->clj
         (#(get-in % ["_embedded" "transactions"])))))

(defn fetch-recurring-transfer [token savings-goal]
  (go
    (->> {:hostname "api.starlingbank.com"
          :path (str "/api/v1/savings-goals/"
                     (get savings-goal "uid")
                     "/recurring-transfer")
          :headers {:Authorization (str "Bearer " token)}}
         utils/https-get-async
         <!
         (.parse js/JSON)
         js->clj)))

(defn fetch-savings-goals [{:keys [STARLING_TOKEN]}]
  (go
    (let [finished? #(= (get-in % ["totalSaved" "minorUnits"])
                        (get-in % ["target" "minorUnits"]))]
      (->> {:hostname "api.starlingbank.com"
            :path "/api/v1/savings-goals"
            :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}
           utils/https-get-async
           <!
           (.parse js/JSON)
           js->clj
           (#(get % "savingsGoalList"))
           (filter (complement finished?))
           (map #(go (->> (fetch-recurring-transfer STARLING_TOKEN %)
                          <!
                          (assoc % "recurringTransfer"))))
           async/merge
           (async/into [])
           <!))))

(defn fetch-scheduled-payments [{:keys [STARLING_TOKEN nilkey]}]
  (go
    (let [in-future? #(> (get % "nextDate")
                         (date/today))]
      (->> {:hostname "api.starlingbank.com"
            :path "/api/v1/payments/scheduled"
            :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}
           utils/https-get-async
           <!
           (.parse js/JSON)
           js->clj
           (#(get-in % ["_embedded" "paymentOrders"]))
           (filter in-future?)))))

(defn recurrence-rule->payment-dates
  [{:keys [count frequency interval start-date] :as recurrence-rule}]
  (let [one-off-payment? (and (= 1 count)
                              (nil? frequency)
                              (nil? interval))
        add-fn (if (= "MONTHLY" frequency)
                 date/add-months
                 date/add-weeks)]
    (if one-off-payment?
      [start-date]
      (->> (range 0 count)
           (map #(* interval %))
           (map #(str (add-fn start-date %)))))))

(defn recurrence-rule->future-transactions
  [{:keys [amount narrative] :as recurrence-rule}]
  (->> recurrence-rule
       recurrence-rule->payment-dates
       (map (fn [date]
              {:source "Starling"
               :narrative narrative
               :amount amount
               :date (str date "T23:59:59.999Z")
               :id (str narrative
                        amount
                        date)}))))

(defn future-transactions [env-vars]
  (go
    (let [recurrence-rules
          (concat (map savings-goal->recurrence-rule
                       (<! (fetch-savings-goals env-vars)))
                  (map scheduled-payment->recurrence-rule
                       (<! (fetch-scheduled-payments env-vars))))]
      (mapcat recurrence-rule->future-transactions recurrence-rules))))

(defn past-transactions [env-vars]
  (go
    (->> (fetch-past-transactions env-vars)
         <!
         (map (fn [obj]
                {:source "Starling"
                 :id (get obj "id")
                 :date (get obj "created")
                 :narrative (get obj "narrative")
                 :amount (gstring/format "%.2f" (get obj "amount"))
                 :balance (gstring/format "%.2f" (get obj "balance"))})))))

(defn transactions [env-vars]
  (go
    (concat (<! (past-transactions env-vars))
            (<! (future-transactions env-vars)))))
