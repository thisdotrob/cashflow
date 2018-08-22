(ns cashflow-app.data-transformation)

(def json->clj (comp #(js->clj % :keywordize-keys true) js/JSON.parse))

(def clj->json (comp js/JSON.stringify clj->js))
