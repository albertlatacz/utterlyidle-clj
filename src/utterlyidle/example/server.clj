(ns utterlyidle.example.server
  (:gen-class)
  (:require [utterlyidle.bindings :refer :all]
            [utterlyidle.core :refer :all]))

(defn var-binding-example [req name]
  (str "Hello from var! name=" name))

(defn -main [& args]
  (start-server {:port 8001 :base-path "/"}
                (with-resource :get "/var-binding" {:query-params [name] :as [req]} var-binding-example)
                (with-resource :get "/fn-binding" {:query-params [name] :as [req]} (fn [req name] (str "Hello from fn! name=" name)))
                (with-resources-in-ns 'utterlyidle.example.resources)
                (with-static-resources-in (package-url 'utterlyidle.example.resources) "static")
                (with-application-scoped {:foo "Hello there!"})))