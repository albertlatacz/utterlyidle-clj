(ns utterlyidle.helpers
  (:import [utterlyidle.bindings ResourceBinding StaticResourceBinding ScopedParameterBinding]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.modules Modules]
           (java.net InetAddress URLEncoder))

  (:require [clojure.tools.namespace :refer :all]
            [utterlyidle.bridge :refer :all]
            [utterlyidle.bindings :refer :all]
            [clojure.java.io :refer [file]]
            [clojure.string :refer [join]]))

(defn url-encode
  [unencoded & [encoding]]
  (URLEncoder/encode unencoded (or encoding "UTF-8")))

(defn filter-empty-pairs [params]
  (reduce concat (remove (comp nil? second) params)))


(defn as-request-params [params encoding]
  (letfn [(param-name [param]
                      (if (keyword? param) (name param) (str param)))
          (explode-params [[name values]]
                          (map #(str (param-name name) "=" (url-encode (str %) encoding))
                               (flatten (vector values))))]
    (join "&" (mapcat explode-params params))))

(defn binding->params [binding]
  (let [{:keys [query-params form-params cookie-params header-params path-params request-params]} binding]
    (keep identity
          (map
            (fn [arg]
              (cond
                (some #{arg} request-params) (request-param)
                (some #{arg} query-params) (query-param arg)
                (some #{arg} form-params) (form-param arg)
                (some #{arg} cookie-params) (cookie-param arg)
                (some #{arg} header-params) (header-param arg)
                (some #{arg} path-params) (path-param arg)))
            (first (:arguments binding))))))

(defn fn->binding [func]
  (let [binding (:binding (meta func))]
    (vector
      (create-binding
        (:path binding)
        (.. (name (:method binding)) (toUpperCase))
        (:consumes binding)
        (:produces binding)
        func
        (binding->params binding)))))

(defn as-binding [obj]
  (cond
    (instance? ResourceBinding (:binding (meta obj))) (fn->binding obj)
    (instance? StaticResourceBinding obj) (static-resources-binding obj)
    :default []))

(defn bindings->array [bindings]
  (into-array ^Binding (mapcat as-binding (flatten bindings))))

