(ns utterlyidle.bindings
  (:import (com.googlecode.utterlyidle.dsl StaticBindingBuilder))
  (:require [clojure.tools.namespace :refer :all]
            [clojure.java.io :refer [file as-url resource]]
            [clojure.string :refer [join split]]))

(defn- functions-in-namespace [ns]
  (require ns)
  (let [resolve-func (fn [func] (ns-resolve ns func))
        funcs (keys (ns-publics ns))]
    (map resolve-func funcs)))

(defn- fn->args [form]
  (let [form-sym (first form)]
    (if (and (symbol? form-sym) (= 'fn form-sym))
      (list (second form)))))

(defn- extract-args [form]
  (if (symbol? form)
    (mapv #(mapv name %) (:arglists (meta (resolve form))))
    (mapv #(mapv name %) (fn->args form))))

(defn- resolve-media-types [types]
  (if-not (empty? types)
    types
    ["*/*"]))

(defn- parse-args [args]
  (let [method (nth (nth args 1) 0)]
    {:fn-name (nth args 0)
     :method method
     :path (nth (nth args 1) 1)
     :query-params (:query-params (nth args 2))
     :form-params (:form-params (nth args 2))
     :path-params (:path-params (nth args 2))
     :header-params (:header-params (nth args 2))
     :cookie-params (:cookie-params (nth args 2))
     :request-params (vec (:as (nth args 2)))
     :consumes (:consumes (nth args 2))
     :produces (:produces (nth args 2))
     :body (drop 3 args)}))

(defn- package-root [ns]
  (let [metadata (meta (first (functions-in-namespace ns)))]
    (as-> (str (resource (:file metadata))) path
          (split path #"/")
          (drop-last (inc (count (filter #{\.} (str (:ns metadata))))) path)
          (join "/" path)
          (str path "/"))))

(defn- ns->dir [ns]
  (as-> (str ns) path
        (split path #"\.")
        (drop-last path)
        (join "/" path)
        (str path "/")))


(defrecord ResourceBinding [method path consumes produces query-params form-params path-params header-params cookie-params request-params arguments])
(defn resource-binding [method path consumes produces query-params form-params path-params header-params cookie-params request-params arguments]
  (ResourceBinding. method path consumes produces query-params form-params path-params header-params cookie-params request-params arguments))

(defrecord StaticResourceBinding [url path])
(defn static-resource-binding [url path]
  (StaticResourceBinding. url path))

(defn bind-function [method path consumes produces query-params form-params path-params header-params cookie-params request-params func args]
  (with-meta func
    (assoc (meta func)
      :binding (ResourceBinding.
                 method
                 path
                 (resolve-media-types consumes)
                 (resolve-media-types produces)
                 query-params
                 form-params
                 path-params
                 header-params
                 cookie-params
                 request-params
                 args))))

(defn with-resources-in-ns
  "Returns all binded resources in given namespace."
  [ns]
  (filter #(:binding (meta %)) (functions-in-namespace ns)))

(defn with-resources-in-dir
  "Returns all binded resources in given directory."
  [dir]
  (let [namespaces (find-namespaces-in-dir (file dir))]
    (mapcat with-resources-in-ns namespaces)))

(defn package-url
  "Resolves package url (java.net.URL) of given namespace.
  e.g. Given a 'aaa/bbb.clj' namespace, calling (package-url 'aaa.bbb)
  resolves to file:/path/to/sources/aaa or jar:file:/path/to.jar!/aaa"
  [ns]
  (as-url (str (package-root ns) (ns->dir ns))))

(defn with-static-resources-in
  "Creates static resources binding for specified url (java.net.URL) and path.
  e.g (with-static-resources-in (package-url 'aaa.bbb) \"/static\")"
  [url path]
  (with-meta
    {}
    {:binding (StaticResourceBinding. url path)}))

(defmacro with-resource
  "Binds function or symbol as a resource. Since named parameters are required only defn and fn forms are supported."
  [method path params function]
  (let [{:keys [query-params form-params path-params header-params cookie-params consumes produces as]} params]
    `(bind-function
       ~method
       ~path
       ~consumes
       ~produces
       ~(mapv name query-params)
       ~(mapv name form-params)
       ~(mapv name path-params)
       ~(mapv name header-params)
       ~(mapv name cookie-params)
       ~(mapv name as)
       ~function
       ~(extract-args function))))

(defmacro defresource
  "Defines resource that can be binded by with-resources-in-dir or with-resources-in-ns."
  [& args]
  (let [{:keys [fn-name method path consumes produces query-params form-params path-params header-params cookie-params request-params body]} (parse-args args)
        fn-params (concat request-params query-params form-params path-params header-params cookie-params)]
    `(defn
       ~(bind-function
          method
          path
          consumes
          produces
          (mapv name query-params)
          (mapv name form-params)
          (mapv name path-params)
          (mapv name header-params)
          (mapv name cookie-params)
          (mapv name request-params)
          fn-name
          [(mapv name fn-params)])
       ~(vec fn-params)
       ~@body)))