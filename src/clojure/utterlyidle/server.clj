(ns utterlyidle.server
  (:require [utterlyidle.core :refer :all]
            [clojure.tools.namespace :refer :all]
            [clojure.java.io :refer [file]])

  (:import [utterlyidle InvokeClojureResourceMethod]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.modules Modules]
           [com.googlecode.totallylazy Pair]))

(defn- params-from-binding [binding]
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

(defn- fn->binding [binding]
  (let [binding-meta (:utterlyidle (meta binding))]
    (InvokeClojureResourceMethod/binding
        (:path binding-meta)
      (. (name (:method binding-meta)) toUpperCase)
      (into-array String (:consumes binding-meta))
      (into-array String (:produces binding-meta))
      binding
      (into-array Pair (params-from-binding binding-meta)))))

(defn start
  "Starts server with specified resource bindings.
  e.g
    (server/start {:port 8080 :base-path \"/\"
      (with-resources-in-dir \"src/clojure/utterlyidle/example\"))"
  [{:keys [port base-path]} & bindings]
  (let [conf (.. (ServerConfiguration/defaultConfiguration) (port (or port 0)))
        app (proxy [RestApplication] [(BasePath/basePath (or base-path "/"))])]
    (.add app
          (Modules/bindingsModule
            (into-array ^Binding (map fn->binding (flatten bindings)))))
    (RestServer. app conf)))