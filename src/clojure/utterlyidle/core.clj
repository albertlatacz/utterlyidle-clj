(ns ^{:author "Albert Latacz",
      :doc    "Core binding functions for UtterlyIdle"}
  utterlyidle.core
  (:require [clojure.tools.namespace :refer :all]
            [clojure.java.io :refer [file]]))

(defn- functions-in-namespace [ns]
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

(defn- consumes-for-method [method]
  (cond
    (= method :get ) ["*/*"]
    (= method :post ) ["application/x-www-form-urlencoded" "application/xml"]))

(defn- produces-for-method [method]
  (cond
    (= method :get ) ["text/html"]
    (= method :post ) ["text/html"]))

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
     :consumes (or (:consumes (nth args 2)) (consumes-for-method method))
     :produces (or (:produces (nth args 2)) (produces-for-method method))
     :body (drop 3 args)
     }))

(defn with-binding [method path consumes produces query-params form-params path-params header-params cookie-params request-params func args]
  (with-meta func
    (assoc (meta func)
      :utterlyidle {:arguments args
                    :method method
                    :path path
                    :consumes consumes
                    :produces produces
                    :query-params query-params
                    :form-params form-params
                    :path-params path-params
                    :header-params header-params
                    :cookie-params cookie-params
                    :request-params request-params
                    }
      )))

(defn with-resources-in-ns
  "Returns all binded resources in given namespace."
  [ns]
  (require ns)
  (filter #(:utterlyidle (meta %)) (functions-in-namespace ns)))

(defn with-resources-in-dir
  "Returns all binded resources in given directory."
  [dir]
  (let [namespaces (find-namespaces-in-dir (file dir))]
    (mapcat with-resources-in-ns namespaces)))

(defmacro with-resource
  "Binds function or symbol as a resource. Since named parameters are required only defn and fn forms are supported."
  [method path consumes produces params function]
  (let [{:keys [query-params form-params path-params header-params cookie-params as]} params]
    `(with-binding
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
       ~(with-binding
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


