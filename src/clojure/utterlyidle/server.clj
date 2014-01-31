(ns utterlyidle.server
  (:import [utterlyidle.bindings ResourceBinding StaticResourceBinding ScopedParameterBinding]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.modules Modules]
           [com.googlecode.totallylazy Pair]
           (com.googlecode.utterlyidle.dsl StaticBindingBuilder)
           (sun.util ResourceBundleEnumeration)
           (com.googlecode.yadic Resolver))

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


(defn- resources->binding [binding]
  (seq (.. (StaticBindingBuilder/in (:url binding)) (path (:path binding)) (call))))

(defn- as-binding [obj]
  (cond
    (instance? ResourceBinding (:binding (meta obj))) (fn->binding obj)
    (instance? StaticResourceBinding obj) (resources->binding obj)
    :default []))

(defn- bindings->array [bindings]
  (into-array ^Binding (mapcat as-binding (flatten bindings))))


(defn start-server
  "Starts server with specified resource bindings.
  e.g (start-server {:port 8080 :base-path \"/\"
      (with-resources-in-dir \"src/clojure/utterlyidle/example\"))"
  [options & bindings]
  (let [{:keys [port base-path]} options
        config (.. (ServerConfiguration.) (port (or port 0)))
        application (RestApplication. (BasePath/basePath (or base-path "/")))]
    (.. application (add (Modules/bindingsModule (bindings->array bindings))))

    (let [x (filter #(instance? ScopedParameterBinding %) (flatten bindings))]
      (doseq [param x]
        (.. application
            (applicationScope)
            (addType (custom-type (name (:name param)))
                     (reify Resolver
                       (resolve [this type] (:value param)))))))


    {:server (RestServer. application config)}))

(defn stop-server [server]
  (.close (:server server)))