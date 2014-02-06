(ns utterlyidle.core)

(defn entity [object]
  (or (get-in object [:request :entity])
      (get-in object [:response :entity])))

(defn status-code [response]
  (get-in response [:response :status :code]))

(defn status-description [response]
  (get-in response [:response :status :description]))

(defn headers [object]
  (or (get-in object [:request :headers])
      (get-in object [:response :headers])))


