(ns utterlyidle.example.server
  (:gen-class :main true)
  (:use clojure.tools.namespace
        utterlyidle.core
        utterlyidle.server
        [clojure.java.io :only [file]]))


(defn var-binding-example [req name]
  (str "Hello from var! name=" name))

(defn -main [& m]
  (start 8001 "/"
    (with-resource :get "/var-binding" ["*/*"] ["*/*"] {:query-params [name] :as [req]} var-binding-example)
    (with-resource :get "/fn-binding" ["*/*"] ["*/*"] {:query-params [name] :as [req]} (fn [req name] (str "Hello from fn! name=" name)))
    (with-resources-in-ns 'utterlyidle.example.resources)))