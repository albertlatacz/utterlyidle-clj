(ns utterlyidle.testdata.subns.bindings_in_subns
  (:require [utterlyidle.core :refer :all]))

(defresource test-binding-in-sub-ns [:get "/binding-in-sub-ns"] {}
  "Test binding in sub-namespace")
