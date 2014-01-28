(ns utterlyidle.server
  (:import [utterlyidle InvokeClojureResourceMethod]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.modules Modules]
           [com.googlecode.totallylazy Pair]
           (com.googlecode.utterlyidle.dsl StaticBindingBuilder))

  (:require [clojure.tools.namespace :refer :all]
            [clojure.java.io :refer [file]]))

(defn- binding->params [binding]
  (let [{:keys [query-params form-params cookie-params header-params path-params request-params]} binding]
    (mapv
      (fn [arg]
        (cond
          (some #{arg} request-params) (InvokeClojureResourceMethod/requestParam)
          (some #{arg} query-params) (InvokeClojureResourceMethod/queryParam arg)
          (some #{arg} form-params) (InvokeClojureResourceMethod/formParam arg)
          (some #{arg} cookie-params) (InvokeClojureResourceMethod/cookieParam arg)
          (some #{arg} header-params) (InvokeClojureResourceMethod/headerParam arg)
          (some #{arg} path-params) (InvokeClojureResourceMethod/pathParam arg)))
      (first (:arguments binding)))))

(defn- fn->binding [obj]
  (let [metadata (:binding (meta obj))]
    (InvokeClojureResourceMethod/binding
        (:path metadata)
      (.. (name (:method metadata)) (toUpperCase))
      (into-array String (:consumes metadata))
      (into-array String (:produces metadata))
      obj
      (into-array Pair (binding->params metadata)))))


(defn resources->binding [obj]
  (let [binding-meta (:binding (meta obj))]
    (seq (.. (StaticBindingBuilder/in (:url binding-meta)) (path (:path binding-meta)) (call)))))

(defn- as-binding [obj]
  (cond
    (instance? Binding obj) [obj]
    (= :function (get-in (meta obj) [:binding :type])) [(fn->binding obj)]
    (= :static-resources (get-in (meta obj) [:binding :type])) (resources->binding obj)))

(defn- bindings->array [bindings]
  (into-array ^Binding (mapcat as-binding (flatten bindings))))

(defn start-server
  "Starts server with specified resource bindings.
  e.g (start-server {:port 8080 :base-path \"/\"
      (with-resources-in-dir \"src/clojure/utterlyidle/example\"))"
  [{:keys [port base-path]} & bindings]
  (let [config (.. (ServerConfiguration.) (port (or port 0)))
        application (RestApplication. (BasePath/basePath (or base-path "/")))]
    (.. application (add (Modules/bindingsModule (bindings->array bindings))))
    (RestServer. application config)))

(defn stop-server [server]
  (.close server))