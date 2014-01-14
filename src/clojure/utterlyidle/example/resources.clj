(ns utterlyidle.example.resources
  (:use clojure.tools.namespace
        [clojure.java.io :only [file]]
        utterlyidle.server
        utterlyidle.core
        ))

(defresource test-binding [:get "/test-binding"] {}
  "GET SIMPLE")

(defresource test-binding1 [:get "/test-binding1"] {:query-params [name other]}
  (str "GET " name " " other))

(defresource test-binding2 [:post "/test-binding2"] {:form-params [name other]}
  (str "POST " name " " other))

(defresource test-binding3 [:get "/test-binding3/{name}/{other}"] {:path-params [name other]}
  (str "GET " name " " other))

(defresource test-binding4 [:post "/test-binding4"] {:query-params [name]
                                                     :form-params [other]}
  (str "POST " name " " other))

