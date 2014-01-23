(ns utterlyidle.testdata.bindings
  (:require [utterlyidle.bindings :refer :all]))

(defn test-function []
  "Test function")

(defresource test-binding [:get "/binding"] []
  "Test binding")
