(ns utterlyidle.testdata.bindings
  (:require [utterlyidle.core :refer :all]))

(defn test-function []
  "Test function")

(defresource test-binding [:get "/binding"] []
  "Test binding")
