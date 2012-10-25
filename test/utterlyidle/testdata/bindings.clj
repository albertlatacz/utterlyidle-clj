(ns utterlyidle.testdata.bindings
  (:use utterlyidle.bindings))


(defn test-function []
  "Test function")

(defresource test-binding [:get "/binding"] []
  "Test binding")
