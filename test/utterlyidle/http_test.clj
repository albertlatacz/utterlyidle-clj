(ns utterlyidle.http_test
  (:require [clojure.test :refer :all]
            [utterlyidle.core :refer :all]))

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

(deftest supports-parsing-uri
  (is (= (parse-uri "http://www.domain.com:1234/some/path?q=1&q=2&p=encoded++param#fragment")
         {:uri       "http://www.domain.com:1234/some/path?q=1&q=2&p=encoded++param#fragment"
          :scheme    "http"
          :authority "www.domain.com:1234"
          :path      "/some/path"
          :query     "q=1&q=2&p=encoded++param"
          :fragment  "fragment"})))

(deftest supports-parsing-query-with-encoding
  (is (= (parse-query "q=1&q=2&p=encoded%FE%FF%00%0Aparam" "UTF-16")
         {"q" ["1" "2"]
          "p" "encoded\nparam"})))

