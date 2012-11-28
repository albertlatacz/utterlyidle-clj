(ns utterlyidle.server
  (:use utterlyidle.core
        clojure.tools.namespace
        [clojure.java.io :only [file]])

  (:import [utterlyidle ClojureBinding]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration UriTemplate]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.dsl DslBindings BindingBuilder]
           [com.googlecode.utterlyidle.modules Modules Module]
           [com.googlecode.totallylazy Pair]))

(defn- params-from-binding [binding]
  (let [{:keys [query-params form-params cookie-params header-params path-params request-params]} binding]
    (mapv
      (fn [arg]
        (cond
          (some #{arg} request-params) (ClojureBinding/requestParam)
          (some #{arg} query-params) (ClojureBinding/queryParam arg)
          (some #{arg} form-params) (ClojureBinding/formParam arg)
          (some #{arg} cookie-params) (ClojureBinding/cookieParam arg)
          (some #{arg} header-params) (ClojureBinding/headerParam arg)
          (some #{arg} path-params) (ClojureBinding/pathParam arg)))
      (first (:arguments binding)))))

(defn- fn->binding [binding]
  (let [binding-meta (:utterlyidle (meta binding))]
    (ClojureBinding/binding
      (:path binding-meta)
      (. (name (:method binding-meta)) toUpperCase)
      (into-array String (:consumes binding-meta))
      (into-array String (:produces binding-meta))
      binding
      (into-array Pair (params-from-binding binding-meta)))))

(defn start
  "Starts server with specified resource bindings.
  e.g
    (server/start 8080
      (with-resources-in-dir \"src/clojure/utterlyidle/example\"))"
  [port base-path & bindings]
  (let [conf (. (ServerConfiguration/defaultConfiguration) port port)
        app (proxy [RestApplication] [(BasePath/basePath base-path)])]
    (.add app
      (Modules/bindingsModule
        (into-array ^Binding (map fn->binding (flatten bindings)))))
    (RestServer. app conf)))