(ns utterlyidle.example.restServer
  (:use clojure.tools.namespace
        utterlyidle.bindings
        utterlyidle.server
        [clojure.java.io :only [file]]))


(defn var-binding-example [name value req1 req2] (str "Hello from var! name=" name value " " (.method req1) " " (.method req2)))
(defn -main [& m]
  (start 8001 "/"
    (with-resource :get "/var-binding" ["text/plain"] ["text/plain"] {:query-params [value name] :as [req1 req2]} var-binding-example)
    (with-resource :get "/fn-binding" ["text/plain"] ["text/plain"] {:query-params [name] :as [req]} (fn [req name] (str "Hello from fn! name=" name)))
    (with-resources-in-dir "src/clojure/utterlyidle/example")
  )
  )