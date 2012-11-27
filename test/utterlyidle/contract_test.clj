(ns utterlyidle.contract_test
  (:use clojure.test
        utterlyidle.core
        [utterlyidle.server :as server]
        [utterlyidle.client :as client]
        )
  (:refer-clojure :exclude (get)))

(defn test-server [f]
  (def testServer
    (server/start 9000 "/test-server"
      (with-resources-in-ns 'utterlyidle.contract_test)))
  (f)
  (.close testServer))

(use-fixtures :once test-server)

(defn test-url [path]
  (str "http://localhost:9000/test-server" path))




(defresource get-binding-with-parameter [:get "/get-with-query-param"] {:query-params [param]}
  (str "GET " param))

(deftest supports-get-with-parameters
  (let [path "/get-with-query-param"]
    (is (= (:body (client/get (test-url path) {:param "Hello there"})) "GET Hello there"))))



(defresource get-binding-without-parameter [:get "/get-without-param"] {}
  (str "GET"))

(deftest supports-get-without-parameters
  (let [path "/get-without-param"]
    (is (= (:body (client/get (test-url path))) "GET"))))



(defresource post-binding-with-form-parameter [:post "/post-with-form-param"] {:form-params [param]}
  (str "POST " param))

(deftest supports-post-with-form-parameters
  (let [path "/post-with-form-param"]
    (is (= (:body (client/post (test-url path) {:param "Hello there"})) "POST Hello there"))))


(defresource post-binding-with-body [:post "/post-with-body"] {:as [request]}
  (str "POST " (.entity request)))

(deftest supports-post-with-body
  (let [path "/post-with-body"]
    (is (= (:body (client/post  (test-url path) "application/xml" "<helloThere/>")) "POST <helloThere/>"))))



(defresource binding-with-different-parameters [:get "/binding-with-different-parameters/{path-param}"]
  {:query-params [query-param]
   :path-params [path-param]}
  (str "GET " query-param " " path-param))

(deftest supports-different-parameters
  (let [path "/binding-with-different-parameters/there"]
    (is (= (:body (client/get (test-url path) {:query-param "Hello"})) "GET Hello there"))))