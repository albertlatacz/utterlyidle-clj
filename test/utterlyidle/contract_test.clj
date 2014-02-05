(ns utterlyidle.contract_test
  (:import (com.googlecode.utterlyidle MediaType RequestBuilder))
  (:require [clojure.test :refer :all]
            [utterlyidle.bindings :refer :all]
            [utterlyidle.media-types :refer :all]
            [utterlyidle.core :refer :all]
            [utterlyidle.headers :refer :all]
            [utterlyidle.testing :refer :all]
            [utterlyidle.bridge :refer :all]))

(defn test-server [f]
  (def testServer
    (start-server {:port 9000 :base-path "/test-server"}
                  (with-resources-in-ns 'utterlyidle.contract_test)
                  (with-static-resources-in
                    (package-url 'utterlyidle.testdata.bindings)
                    "static"
                    :extensions {"ping" MediaType/IMAGE_PNG})
                  (with-application-scoped {:application-scoped "app scoped"})))
  (binding [client-http-handler (fn [] (.application (:server testServer)))]
    (f))
  (stop-server testServer))

(use-fixtures :once test-server)



(defn test-url [path]
  (str "/test-server" path))

(defresource get-binding-with-parameter [:get "/get-with-query-param"] {:query-params [param]}
  (str "GET " param))

(deftest supports-get-with-parameters
  (is (= (-> (GET "/get-with-query-param?param=Hello%20there") (entity))
         "GET Hello there")))


(defresource get-binding-different-produces-and-consumes [:get "/get-with-prod-cons"]
  {:consumes [wildcard]
   :produces [text-plain]}
  (str "GET CONSUMING '" wildcard "' AND PRODUCING '" text-plain "'"))

(deftest supports-binding-different-produces-and-consumes
  (is (= (-> (GET "/get-with-prod-cons") (entity))
         "GET CONSUMING '*/*' AND PRODUCING 'text/plain'")))


(defresource get-binding-without-parameter [:get "/get-without-param"] {}
  (str "GET"))

(deftest supports-get-without-parameters
  (is (= (-> (GET "/get-without-param") (entity))
         "GET")))


(defresource post-binding-with-form-parameter [:post "/post-with-form-param"] {:form-params [param]}
  (str "POST " param))

(deftest supports-post-with-form-parameters
  (is (= (-> (POST "/post-with-form-param" :headers {Content-Type application-form-urlencoded} :entity "param=Hello there") (entity))
         "POST Hello there")))


(defresource post-binding-with-body [:post "/post-with-body"] {:as [request]}
  (str "POST " (entity request)))

(deftest supports-post-with-body
  (is (= (-> (POST "/post-with-body" :headers {Content-Type application-xml} :entity "<helloThere/>") (entity))
         "POST <helloThere/>")))


(defresource binding-with-different-parameters [:get "/binding-with-different-parameters/{path-param}"]
  {:query-params [query-param]
   :path-params [path-param]}
  (str "GET " query-param " " path-param))

(deftest supports-different-parameters
  (is (= (-> (GET "/binding-with-different-parameters/there?query-param=Hello") (entity))
         "GET Hello there")))

(defresource binding-with-application-scoped [:get "/binding-with-application-scoped"]
  {:scoped-params {:application-scoped app-scoped}}
  (str "GET SCOPED " app-scoped))

(deftest supports-application-scoping
  (is (= (-> (GET "/binding-with-application-scoped") (entity))
         "GET SCOPED app scoped")))

(deftest supports-static-resources
  (is (= (-> (GET "/static/test.ping") (status-code))
         200)))


(deftest testing-server-works-for-multiple-bindings
  (testing-server [(with-application-scoped {:a-value "some value"})
                   (with-resource :get "/" {:scoped-params {:a-value some-val}}
                                  (fn [some-val] (str "TEST " some-val)))]
    (is (= (-> (GET "/") (entity))
           "TEST some value"))))


(deftest testing-server-works-for-single-binding
  (testing-server (with-resource :get "/" {} (fn [] "TEST"))
    (is (= (-> (GET "/") (entity))
           "TEST"))))