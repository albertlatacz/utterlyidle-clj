(ns utterlyidle.example.server
  (:gen-class)
  (:require [utterlyidle.core :refer :all]
            [utterlyidle.server :refer :all]))

(defn var-binding-example [req name]
  (str "Hello from var! name=" name))

(defn -main [& args]
  (start 8001 "/"
         (with-resource :get "/var-binding" {:query-params [name] :as [req]} var-binding-example)
         (with-resource :get "/fn-binding" {:query-params [name] :as [req]} (fn [req name] (str "Hello from fn! name=" name)))
         (with-resources-in-ns 'utterlyidle.example.resources)))