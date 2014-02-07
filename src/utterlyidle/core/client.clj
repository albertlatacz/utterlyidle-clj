(ns utterlyidle.core
  (:import (com.googlecode.utterlyidle.annotations HttpMethod)
           (java.net URLEncoder)
           (com.googlecode.utterlyidle.handlers ClientHttpHandler)
           (com.googlecode.utterlyidle Requests Response HeaderParameters)
           (com.googlecode.totallylazy Uri Pair))
  (:require [utterlyidle.core.utils :refer :all]))

(defn ^:dynamic client-http-handler []
  (ClientHttpHandler.))

(defn- make-request [method uri & {:keys [headers entity client] :or {client (client-http-handler)} :as req}]
  (-> (.handle client (map->request {:request (merge {:method method :uri uri} (dissoc req :client))}))
      (response->map)))

(defn GET [uri & {:keys [headers entity client] :as request}]
  (apply make-request HttpMethod/GET uri (filter-empty-pairs request)))

(defn POST [uri & {:keys [headers entity client] :as request}]
  (apply make-request HttpMethod/POST uri (filter-empty-pairs request)))

(defn PUT [uri & {:keys [headers entity client] :as request}]
  (apply make-request HttpMethod/PUT uri (filter-empty-pairs request)))

(defn DELETE [uri & {:keys [headers entity client] :as request}]
  (apply make-request HttpMethod/DELETE uri (filter-empty-pairs request)))

(defn HEAD [uri & {:keys [headers entity client] :as request}]
  (apply make-request HttpMethod/HEAD uri (filter-empty-pairs request)))

(defn OPTIONS [uri & {:keys [headers entity client] :as request}]
  (apply make-request HttpMethod/OPTIONS uri (filter-empty-pairs request)))

