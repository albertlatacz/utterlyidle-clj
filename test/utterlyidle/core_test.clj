(ns utterlyidle.core_test
  (:require [clojure.test :refer :all]
            [utterlyidle.core :refer :all]
            [utterlyidle.testdata.bindings :refer :all]
            [utterlyidle.testdata.subns.bindings_in_subns :refer :all]))

(deftest finds-binded-functions-in-directory
  (is (= [#'utterlyidle.testdata.bindings/test-binding #'utterlyidle.testdata.subns.bindings_in_subns/test-binding-in-sub-ns]
        (with-resources-in-dir "test/utterlyidle/testdata"))))

(deftest finds-binded-functions-in-namespace
  (is (= [#'utterlyidle.testdata.bindings/test-binding]
        (with-resources-in-ns 'utterlyidle.testdata.bindings))))

(deftest binds-fn-correctly
  (is (= (do (meta (with-resource :get "/test" ["consumes"] ["produces"]
                     {:query-params [query-param] :form-params [form-param] :path-params [path-param] :cookie-params [cookie-param]
                      :header-params [header-param] :as [request]} (fn [request name] name))))
        {:utterlyidle {:arguments [["request" "name"]]
                       :method :get
                       :path "/test"
                       :query-params ["query-param"]
                       :form-params ["form-param"]
                       :path-params ["path-param"]
                       :cookie-params ["cookie-param"]
                       :header-params ["header-param"]
                       :request-params ["request"]
                       :consumes ["consumes"]
                       :produces ["produces"]
                       }})))

(defn test-resource [name] name)
(deftest binds-var-correctly
  (is (= (:arguments (:utterlyidle (do (meta (with-resource :get "/test" ["consumes"] ["produces"] {:query-params [name]} test-resource)))))
        [["name"]])))

