(ns utterlyidle.core
  (:import (utterlyidle.bindings ResourceBinding StaticResourceBinding ScopedParameterBinding)
           (com.googlecode.utterlyidle ServerConfiguration BasePath RestApplication)
           (java.net InetAddress URLEncoder)
           (com.googlecode.utterlyidle.modules Modules)
           (com.googlecode.utterlyidle.httpserver RestServer)
           (com.googlecode.utterlyidle.annotations HttpMethod)
           (com.googlecode.utterlyidle.handlers ClientHttpHandler))
  (:require [utterlyidle.helpers :refer :all]
            [utterlyidle.bridge :refer :all]
            [utterlyidle.bindings :refer :all]))

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

(defn start-server
  "Starts server with specified resource bindings.
  e.g (start-server {:port 8080 :base-path \"/\"
      (with-resources-in-dir \"src/clojure/utterlyidle/example\"))"
  [{:keys [port base-path max-threads bind-address] :or {port 0, base-path "/", max-threads 50, bind-address "0.0.0.0"}} & bindings]
  (let [config (.. (ServerConfiguration.)
                   (port port)
                   (maxThreadNumber max-threads)
                   (bindAddress (InetAddress/getByName bind-address))
                   (basePath (BasePath/basePath base-path)))
        application (RestApplication. (BasePath/basePath base-path))]
    (.. application (add (Modules/bindingsModule (bindings->array bindings))))
    (let [x (filter #(instance? ScopedParameterBinding %) (flatten bindings))]
      (doseq [param x]
        (.. application
            (applicationScope)
            (addType (custom-type (name (:name param))) (value-resolver (:value param))))))
    {:server (RestServer. application config)}))

(defn stop-server [server]
  (.close (:server server)))
