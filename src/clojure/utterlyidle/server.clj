(ns utterlyidle.server
  (:use clojure.tools.namespace
        [clojure.java.io :only [file]]
        utterlyidle.bindings
        )

  (:import [utterlyidle ClojureBinding]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration UriTemplate]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.dsl DslBindings BindingBuilder]
           [com.googlecode.utterlyidle.modules Modules Module]
           [com.googlecode.totallylazy Pair]))

(defn start [port bindings]
  (let [conf (. (ServerConfiguration/defaultConfiguration) port port)
        app (proxy [RestApplication] [(BasePath/basePath "/")])]
    (.add app
      (Modules/bindingsModule
        (into-array ^Binding (map fn-to-binding bindings))))
    (RestServer. app conf)))