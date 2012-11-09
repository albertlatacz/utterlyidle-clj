(ns utterlyidle.bindings_test
  (:use clojure.test
        utterlyidle.bindings
        utterlyidle.testdata.bindings
        utterlyidle.testdata.subns.bindings_in_subns
        ))

(deftest finds-binded-functions-in-directory
  (is (= [#'utterlyidle.testdata.bindings/test-binding #'utterlyidle.testdata.subns.bindings_in_subns/test-binding-in-sub-ns]
        (bindings-in-dir "test/utterlyidle/testdata"))))

(deftest finds-binded-functions-in-namespace
  (is (= [#'utterlyidle.testdata.bindings/test-binding]
        (bindings-in-namespace 'utterlyidle.testdata.bindings))))

(deftest finds-functions-in-namespace
  (is (= [#'utterlyidle.testdata.bindings/test-function #'utterlyidle.testdata.bindings/test-binding]
        (functions-in-namespace 'utterlyidle.testdata.bindings))))

(deftest binds-function-correctly
  (let [binded-resource (bind-resource :post "/test" ["consumes"] ["produces"] ["query-param"] ["form-param"] ["path-param"] ["header-param"] ["cookie-param"] "request" (fn [name] ""))]
    (is (binding? binded-resource))
    (is (= (meta binded-resource)
          {:utterlyidle {:method :post
                         :path "/test"
                         :query-params ["query-param"]
                         :form-params ["form-param"]
                         :path-params ["path-param"]
                         :cookie-params ["cookie-param"]
                         :header-params ["header-param"]
                         :request-param "request"
                         :consumes ["consumes"]
                         :produces ["produces"]
                         }}))))

