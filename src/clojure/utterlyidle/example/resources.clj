(ns utterlyidle.example.resources
  (:require [utterlyidle.bindings :refer :all]))

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

(defresource test-binding [:get "/test-binding5"] {:query-params [name] :scoped-params {:foo foo}}
  (str "GET WITH SCOPED " name " " foo))

