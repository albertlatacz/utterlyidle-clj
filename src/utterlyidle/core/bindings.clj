(ns utterlyidle.core
  (:require [clojure.java.io :refer [file as-url resource]]
            [clojure.string :refer [join split]]
            [utterlyidle.core.utils :refer :all]))

(use '[clojure.tools.namespace :refer [find-namespaces-in-dir]])

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
  {:fn-name        (nth args 0)
   :method         (nth (nth args 1) 0)
   :path           (nth (nth args 1) 1)
   :query-params   (:query-params (nth args 2))
   :form-params    (:form-params (nth args 2))
   :path-params    (:path-params (nth args 2))
   :header-params  (:header-params (nth args 2))
   :cookie-params  (:cookie-params (nth args 2))
   :request-params (:as (nth args 2))
   :consumes       (:consumes (nth args 2))
   :produces       (:produces (nth args 2))
   :scoped-params  (:scoped-params (nth args 2))
   :body           (drop 3 args)})

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


(defrecord ResourceBinding [method path consumes produces query-params form-params path-params header-params cookie-params request-params scoped-params arguments])
(defrecord StaticResourceBinding [url path extensions])
(defrecord ScopedParameterBinding [name value])

(defn bind-function [func method path consumes produces query-params form-params path-params header-params cookie-params request-params scoped-params args]
  (vary-meta
    func assoc
    :binding (ResourceBinding.
               (eval method)
               (eval path)
               (resolve-media-types (mapv eval consumes))
               (resolve-media-types (mapv eval produces))
               query-params
               form-params
               path-params
               header-params
               cookie-params
               (vec request-params)
               (vec (map (fn [[x y]] [x y]) scoped-params))
               args)))

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
  [url path & {:keys [extensions]}]
  (StaticResourceBinding. url path extensions))

(defn with-application-scoped
  "Creates bindings in application scope."
  [params]
  (map (fn [[k v]] (ScopedParameterBinding. k v)) params))

(defmacro with-resource
  "Binds function or symbol as a resource. Since named parameters are required only defn and fn forms are supported."
  [method path params function]
  (let [{:keys [scoped-params query-params form-params path-params header-params cookie-params consumes produces as]} params]
    `(bind-function
       ~function
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
       ~(map-values (fn [p] (name p)) scoped-params)
       ~(extract-args function))))

(defmacro defresource
  "Defines resource that can be binded by with-resources-in-dir or with-resources-in-ns."
  [& args]
  (let [{:keys [fn-name method path consumes produces query-params form-params path-params header-params cookie-params request-params body scoped-params]} (parse-args args)
        fn-params (concat (map second scoped-params) request-params query-params form-params path-params header-params cookie-params)]
    `(defn
       ~(bind-function
          fn-name
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
          (vec (map-values (fn [x] (name x)) scoped-params))
          [(mapv name fn-params)])
       ~(vec fn-params)
       ~@body)))