(ns utterlyidle.client-test
  (:require [clojure.test :refer :all]
            [utterlyidle.client :refer :all]))

(deftest creating-simple-uri-without-params
  (is (= (uri "http://www.domain.com")
         "http://www.domain.com")))

(deftest creating-simple-uri-with-default-encoding
  (is (= (uri "http://www.domain.com" :params {:param "multi\nline val" "list" [1 2]})
         "http://www.domain.com?param=multi%0Aline+val&list=1&list=2")))

(deftest creating-uri-with-different-encoding
  (is (= (uri "http://www.domain.com" :encoding "UTF-16" :params {:param "multi\nline val"})
         "http://www.domain.com?param=multi%FE%FF%00%0Aline+val")))

(deftest creating-form-parameters-with-different-encoding
  (is (= (form :encoding "UTF-16" :params {:param "multi\nline val" "list" [1 2]})
         "param=multi%FE%FF%00%0Aline+val&list=1&list=2")))