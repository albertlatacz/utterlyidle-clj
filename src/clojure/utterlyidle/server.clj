(ns utterlyidle.server
  (:import [utterlyidle InvokeClojureResourceMethod]
           [utterlyidle.bindings ResourceBinding StaticResourceBinding]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.modules Modules]
           [com.googlecode.totallylazy Pair]
           (com.googlecode.utterlyidle.dsl StaticBindingBuilder)
           (sun.util ResourceBundleEnumeration))

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

(defn- fn->binding [func]
  (let [binding (:binding (meta func))]
    (vector
      (InvokeClojureResourceMethod/binding (:path binding)
        (.. (name (:method binding)) (toUpperCase))
        (into-array String (:consumes binding))
        (into-array String (:produces binding))
        func
        (into-array Pair (binding->params binding))))))


(defn- resources->binding [binding]
  (seq (.. (StaticBindingBuilder/in (:url binding)) (path (:path binding)) (call))))

(defn- as-binding [obj]
  (cond
    (instance? ResourceBinding (:binding (meta obj))) (fn->binding obj)
    (instance? StaticResourceBinding obj) (resources->binding obj)))

(defn- bindings->array [bindings]
  (into-array ^Binding (mapcat as-binding (flatten bindings))))


(defprotocol Server
  (start [this options bindings])
  (stop [this]))

(defrecord UtterlyIdleServer []
  Server
  (start [this options bindings]
    (let [{:keys [port base-path]} options
          config (.. (ServerConfiguration.) (port (or port 0)))
          application (RestApplication. (BasePath/basePath (or base-path "/")))]
      (.. application (add (Modules/bindingsModule (bindings->array bindings))))
      (assoc this :server (RestServer. application config))))

  (stop [this]
    (.close (:server this))))


(defn start-server
  "Starts server with specified resource bindings.
  e.g (start-server {:port 8080 :base-path \"/\"
      (with-resources-in-dir \"src/clojure/utterlyidle/example\"))"
  [options & bindings]
  (start (UtterlyIdleServer.) options bindings))

(defn stop-server [server]
  (stop server))