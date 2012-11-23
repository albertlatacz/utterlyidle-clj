(ns utterlyidle.example.restServer
  (:use clojure.tools.namespace
        utterlyidle.bindings
        utterlyidle.server
        [clojure.java.io :only [file]]))


(defn var-binding-example [request name] (str "Hello from var! name=" name))
(defn -main [& m]
  (start 8001 "/"
    (with-resource :get "/var-binding" ["text/plain"] ["text/plain"] {:query-params ["name"]} var-binding-example)
    (with-resource :get "/fn-binding" ["text/plain"] ["text/plain"] {:query-params ["name"]} (fn [request name] (str "Hello from fn! name=" name)))
    (with-resources-in-dir "src/clojure/utterlyidle/example")
  )
  )