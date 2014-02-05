(ns utterlyidle.client
  (:import [com.googlecode.utterlyidle RequestBuilder FormParameters Response Request Requests HeaderParameters]
           [com.googlecode.utterlyidle.handlers ClientHttpHandler]
           (com.googlecode.utterlyidle.annotations HttpMethod)
           (com.googlecode.totallylazy Uri Pair)
           (java.net URLEncoder))
  (:refer-clojure :exclude [get])
  (:require [clojure.string :refer [join]]
            [utterlyidle.bridge :refer :all]))

(defn- header-parameters->vec [headers]
  (vec (map #(vector (.first %) (.second %)) headers)))

(defn- vec->header-parameters [headers-list]
  (HeaderParameters/headerParameters
    (map (fn [[k v]] (Pair/pair k v)) headers-list)))

(defn- response->map [^Response response]
  {:response
    {:status  {:code        (.. response (status) (code))
               :description (.. response (status) (description))}
     :headers (header-parameters->vec (.headers response))
     :entity  (.. response (entity) (toString))}})

(defn- request->map [^Request request]
  {:request
    {:method  (.method request)
     :uri     (.. request (uri) (toString))
     :entity  (.. request (entity) (toString))
     :headers (header-parameters->vec (.headers request))}})

(defn- map->request [request-map]
  (Requests/request (get-in request-map [:request :method])
                    (Uri/uri (get-in request-map [:request :uri]))
                    (vec->header-parameters (get-in request-map [:request :headers]))
                    (get-in request-map [:request :entity])))

(defn ^:dynamic client-http-handler []
  (ClientHttpHandler.))

(defn- make-request [method uri & {:keys [headers body client] :or {client (client-http-handler)} :as request}]
  (-> (.handle client (map->request {:request (merge {:method method :uri uri} (dissoc request :client))}))
      (response->map)))

(defn- filter-empty-pairs [params]
  (reduce concat (remove (comp nil? second) params)))

(defn- url-encode
  [unencoded & [encoding]]
  (URLEncoder/encode unencoded (or encoding "UTF-8")))


(defn- as-request-params [params encoding]
  (letfn [(param-name [param]
                      (if (keyword? param) (name param) (str param)))
          (explode-params [[name values]]
                          (map #(str (param-name name) "=" (url-encode (str %) encoding))
                               (flatten (vector values))))]
    (join "&" (mapcat explode-params params))))


(defn GET [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/GET uri (filter-empty-pairs request)))

(defn POST [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/POST uri (filter-empty-pairs request)))

(defn PUT [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/PUT uri (filter-empty-pairs request)))

(defn DELETE [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/DELETE uri (filter-empty-pairs request)))

(defn HEAD [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/HEAD uri (filter-empty-pairs request)))

(defn OPTIONS [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/OPTIONS uri (filter-empty-pairs request)))


(defn uri [base & {:keys [params encoding]}]
  (let [query-params (as-request-params params encoding)]
    (if-not (empty? query-params)
      (str base "?" query-params)
      base)))

(defn form [& {:keys [params encoding]}]
  (as-request-params params encoding))


(defn entity [response]
  (get-in response [:response :entity]))

(defn status-code [response]
  (get-in response [:response :status :code]))

(defn status-description [response]
  (get-in response [:response :status :description]))

(defn headers [response]
  (get-in response [:response :headers]))