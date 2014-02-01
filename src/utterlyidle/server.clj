(ns utterlyidle.server
  (:import [utterlyidle.bindings ResourceBinding StaticResourceBinding ScopedParameterBinding]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.modules Modules]
           (java.net InetAddress))

  (:require [clojure.tools.namespace :refer :all]
            [utterlyidle.bridge :refer :all]
            [utterlyidle.bindings :refer :all]
            [clojure.java.io :refer [file]]))


(defn- binding->params [binding]
  (let [{:keys [query-params form-params cookie-params header-params path-params request-params]} binding]
    (keep identity
          (map
            (fn [arg]
              (cond
                (some #{arg} request-params) (request-param)
                (some #{arg} query-params) (query-param arg)
                (some #{arg} form-params) (form-param arg)
                (some #{arg} cookie-params) (cookie-param arg)
                (some #{arg} header-params) (header-param arg)
                (some #{arg} path-params) (path-param arg)))
            (first (:arguments binding))))))

(defn- fn->binding [func]
  (let [binding (:binding (meta func))]
    (vector
      (create-binding
        (:path binding)
        (.. (name (:method binding)) (toUpperCase))
        (:consumes binding)
        (:produces binding)
        func
        (binding->params binding)))))

(defn- as-binding [obj]
  (cond
    (instance? ResourceBinding (:binding (meta obj))) (fn->binding obj)
    (instance? StaticResourceBinding obj) (static-resources-binding (:url obj) (:path obj))
    :default []))

(defn- bindings->array [bindings]
  (into-array ^Binding (mapcat as-binding (flatten bindings))))


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