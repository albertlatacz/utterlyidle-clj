(ns utterlyidle.bindings_test
  (:import [java.net URL]
           [utterlyidle.bindings ResourceBinding StaticResourceBinding])
  (:require [clojure.test :refer :all]
            [utterlyidle.bindings :refer :all]
            [utterlyidle.media-types :refer :all]
            [utterlyidle.testdata.bindings :refer :all]
            [utterlyidle.testdata.subns.bindings_in_subns :refer :all]))

(deftest finds-binded-functions-in-directory
  (is (= [#'utterlyidle.testdata.bindings/test-binding #'utterlyidle.testdata.subns.bindings_in_subns/test-binding-in-sub-ns]
         (with-resources-in-dir "test/utterlyidle/testdata"))))

(deftest finds-binded-functions-in-namespace
  (is (= [#'utterlyidle.testdata.bindings/test-binding]
         (with-resources-in-ns 'utterlyidle.testdata.bindings))))

(deftest binds-fn-correctly
  (is (= (do (meta (with-resource :get "/test"
                                  {:consumes      [application-json] :produces [application-xml] :query-params [query-param]
                                   :form-params   [form-param] :path-params [path-param] :cookie-params [cookie-param]
                                   :header-params [header-param] :scoped-params {:foo foo} :as [request]} (fn [request name] name))))
         {:binding (ResourceBinding.
                     :get
                     "/test"
                     [application-json]
                     [application-xml]
                     ["query-param"]
                     ["form-param"]
                     ["path-param"]
                     ["header-param"]
                     ["cookie-param"]
                     ["request"]
                     [[:foo "foo"]]
                     [["request" "name"]])})))

(deftest use-default-media-types-for-produces-and-consumes
  (is (= (do (meta (with-resource :get "/test" {} (fn [] "test"))))
         {:binding (ResourceBinding.
                     :get
                     "/test"
                     ["*/*"]
                     ["*/*"]
                     []
                     []
                     []
                     []
                     []
                     []
                     []
                     [[]])})))


(defn test-resource [name] name)
(deftest binds-var-correctly
  (is (= (:arguments (:binding (do (meta (with-resource :get "/test" {:query-params [name]} test-resource)))))
         [["name"]])))

(deftest produces-static-resource-binding
  (let [url (URL. "file:/some/url")
        path "/static"]
    (is (= (meta (with-static-resources-in url path))
           {:binding {:type :static-resources
                      :url  url
                      :path path}}))))


(deftest produces-static-resource-binding
  (is (.endsWith (str (package-url 'utterlyidle.testdata.subns.bindings_in_subns))
                 "test/utterlyidle/testdata/subns/")))


