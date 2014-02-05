(ns utterlyidle.client
  (:import [com.googlecode.utterlyidle RequestBuilder FormParameters Response Request Requests HeaderParameters]
           [com.googlecode.utterlyidle.handlers ClientHttpHandler]
           (com.googlecode.utterlyidle.annotations HttpMethod)
           (com.googlecode.totallylazy Uri Pair))
  (:refer-clojure :exclude [get])
  (:require [utterlyidle.bridge :refer :all]))

(defn- header-parameters->vec [headers]
  (vec (map #(vector (.first %) (.second %)) headers)))

(defn- vec->header-parameters [headers-list]
  (HeaderParameters/headerParameters
    (map (fn [[k v]] (Pair/pair k v)) headers-list)))

(defn- response->map [^Response response]
  {:response
   {:status {:code (.. response (status) (code))
             :description (.. response (status) (description))}
    :headers (header-parameters->vec (.headers response))
    :entity (.. response (entity) (toString))}})

(defn- request->map [^Request request]
  {:request
   {:method (.method request)
    :uri (.. request (uri) (toString))
    :entity (.. request (entity) (toString))
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

(defn- as-params [params]
  (reduce concat (remove (comp nil? second) params)))


(defn GET [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/GET uri (as-params request)))

(defn POST [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/POST uri (as-params request)))

(defn PUT [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/PUT uri (as-params request)))

(defn DELETE [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/DELETE uri (as-params request)))

(defn HEAD [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/HEAD uri (as-params request)))

(defn OPTIONS [uri & {:keys [headers body client] :as request}]
  (apply make-request HttpMethod/OPTIONS uri (as-params request)))


(defn entity [response]
  (get-in response [:response :entity]))

(defn status-code [response]
  (get-in response [:response :status :code]))

(defn status-description [response]
  (get-in response [:response :status :description]))

(defn headers [response]
  (get-in response [:response :headers]))