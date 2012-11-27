(ns utterlyidle.testdata.bindings
  (:use utterlyidle.core))


(defn test-function []
  "Test function")

(defresource test-binding [:get "/binding"] []
  "Test binding")
