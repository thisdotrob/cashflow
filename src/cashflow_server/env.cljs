(ns cashflow-server.env)

(def get-env-var (partial aget (.-env js/process)))

(def no-nils? (partial every? (complement nil?)))

(defn validate [env-keys]
  (let [env-vars (reduce (fn [acc k] (assoc acc k (get-env-var (name k))))
                         {}
                         env-keys)]
    (if (no-nils? (vals env-vars))
      env-vars
      (throw "Missing env var"))))
