(ns utterlyidle.bindings_test
  (:use clojure.test
        utterlyidle.bindings
        utterlyidle.testdata.bindings
        utterlyidle.testdata.subns.bindings_in_subns
        ))

(deftest finds-binded-functions-in-directory
  (is (= [#'utterlyidle.testdata.bindings/test-binding #'utterlyidle.testdata.subns.bindings_in_subns/test-binding-in-sub-ns]
        (with-resources-in-dir "test/utterlyidle/testdata"))))

(deftest finds-binded-functions-in-namespace
  (is (= [#'utterlyidle.testdata.bindings/test-binding]
        (with-resources-in-ns 'utterlyidle.testdata.bindings))))

(deftest finds-functions-in-namespace
  (is (= [#'utterlyidle.testdata.bindings/test-function #'utterlyidle.testdata.bindings/test-binding]
        (functions-in-namespace 'utterlyidle.testdata.bindings))))

(deftest check-resource-is-binded-correctly
  (let [binded-resource (do (with-resource :get "/test" ["consumes"] ["produces"] {:query-params ["name"]} (fn [name] name)))]
    (is (binding? binded-resource))))

(deftest binds-fn-correctly
  (is (= (do (meta (with-resource :get "/test" ["consumes"] ["produces"] {:query-params ["name"]} (fn [name] name))))
        {:utterlyidle {:arguments [["name"]]
                       :method :get
                       :path "/test"
                       :query-params ["name"]
                       :form-params nil
                       :path-params nil
                       :cookie-params nil
                       :header-params nil
                       :request-param nil
                       :consumes ["consumes"]
                       :produces ["produces"]
                       }})))

(defn test-resource [name] name)
(deftest binds-var-correctly
  (is (= (:arguments (:utterlyidle (do (meta (with-resource :get "/test" ["consumes"] ["produces"] {:query-params ["name"]} test-resource)))))
        [["name"]])))

