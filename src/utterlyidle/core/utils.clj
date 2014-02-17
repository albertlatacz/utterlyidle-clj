(ns utterlyidle.core.utils
  (:import (com.googlecode.utterlyidle HeaderParameters Requests Responses Status Request Response)
           (com.googlecode.totallylazy Pair Uri Sequences)
           (java.net URLEncoder)))

(defn- header-parameters->vec [headers]
  (vec (map #(vector (.first %) (.second %)) headers)))

(defn- vec->header-parameters [headers-list]
  (HeaderParameters/headerParameters
    (map (fn [[k v]] (Pair/pair k v)) headers-list)))

(defn response? [response-map]
  (and (associative? response-map)
       (contains? response-map :response)))

(defn request? [request-map]
  (and (associative? request-map)
       (contains? request-map :request)))

(defn request->map [^Request request]
  {:request
    {:method  (.method request)
     :uri     (.. request (uri) (toString))
     :entity  (.. request (entity) (toString))
     :headers (header-parameters->vec (.headers request))}})

(defn map->response [response-map]
  (try
    (Responses/response (Status/status (get-in response-map [:response :status :code])
                                       (get-in response-map [:response :status :description]))
                        (vec->header-parameters (get-in response-map [:response :headers]))
                        (get-in response-map [:response :entity]))
    (catch Exception e (throw (RuntimeException. (str "Couldn't create " (.getName Response) " from map " response-map))))))

(defn response->map [^Response response]
  {:response
    {:status  {:code        (.. response (status) (code))
               :description (.. response (status) (description))}
     :headers (header-parameters->vec (.headers response))
     :entity  (.. response (entity) (toString))}})

(defn map->request [request-map]
  (Requests/request (get-in request-map [:request :method])
                    (Uri/uri (get-in request-map [:request :uri]))
                    (vec->header-parameters (get-in request-map [:request :headers]))
                    (get-in request-map [:request :entity])))

(defn as-sequence [coll]
  (.. (Sequences/sequence) (join coll)))

(defn filter-empty-pairs [params]
  (reduce concat (remove (comp nil? second) params)))


(defn map-values [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))

(defn single-or-coll [x]
  (if (and (coll? x) (= (count x) 1))
    (first x)
    x))
