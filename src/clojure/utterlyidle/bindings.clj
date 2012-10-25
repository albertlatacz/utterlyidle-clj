(ns utterlyidle.bindings
  (:use clojure.tools.namespace
        [clojure.java.io :only [file]])

  (:import [utterlyidle ClojureBinding]
           [com.googlecode.utterlyidle Binding BasePath RestApplication ServerConfiguration UriTemplate]
           [com.googlecode.utterlyidle.httpserver RestServer]
           [com.googlecode.utterlyidle.dsl DslBindings BindingBuilder]
           [com.googlecode.utterlyidle.modules Modules Module]
           [com.googlecode.totallylazy Pair])
  )

(defn- require-all [& namespaces]
  (doseq [ns (mapcat identity namespaces)]
    (require ns)))

(defn functions-in-namespace [ns]
  (let [resolve-func (fn [func] (ns-resolve ns func))
        funcs (keys (ns-publics ns))]
    (map resolve-func funcs)))

(defn binding? [func]
  (:utterlyidle-binding (meta func)))

(defn bindings-in-namespace [ns]
  (filter binding? (functions-in-namespace ns)))

(defn bindings-in-dir [dir]
  (let [namespaces (find-namespaces-in-dir (file dir))]
    (require-all namespaces)
    (mapcat bindings-in-namespace namespaces)))


(defn bind-resource [method path query-params form-params path-params header-params cookie-params func]
  (with-meta func (assoc (meta func)
                    :utterlyidle-binding true
                    :utterlyidle-method method
                    :utterlyidle-path path
                    :utterlyidle-query-params query-params
                    :utterlyidle-form-params form-params
                    :utterlyidle-path-params path-params
                    :utterlyidle-header-params header-params
                    :utterlyidle-cookie-params cookie-params
                    )))

(defn parse-args [args]
  {:fn-name (nth args 0)
   :method (nth (nth args 1) 0)
   :path (nth (nth args 1) 1)
   :query-params (:query-params (nth args 2))
   :form-params (:form-params (nth args 2))
   :path-params (:path-params (nth args 2))
   :header-params (:header-params (nth args 2))
   :cookie-params (:cookie-params (nth args 2))
   :body (drop 3 args)
   })

(defn form-param [name]
  (ClojureBinding/formParam name))

(defn query-param [name]
  (ClojureBinding/queryParam name))

(defn cookie-param [name]
  (ClojureBinding/cookieParam name))

(defn header-param [name]
  (ClojureBinding/headerParam name))

(defn path-param [name]
  (ClojureBinding/pathParam name))


(defn ui-binding [method path consumes produces params function]
  (ClojureBinding/binding path (. (name method) toUpperCase) (into-array String consumes) (into-array String produces) function (into-array Pair params)))

(defn params-from-binding [binding]
  (let [binding-meta (meta binding)]
    (concat
      (map query-param (:utterlyidle-query-params binding-meta))
      (map form-param (:utterlyidle-form-params binding-meta))
      (map cookie-param (:utterlyidle-cookie-params binding-meta))
      (map header-param (:utterlyidle-header-params binding-meta))
      (map path-param (:utterlyidle-path-params binding-meta))
      )))

(defn fn-to-binding [binding]
  (let [binding-meta (meta binding)]
    (ui-binding
      (:utterlyidle-method binding-meta)
      (:utterlyidle-path binding-meta)
      ["text/plain" "application/x-www-form-urlencoded"]
      ["text/html"]
      (params-from-binding binding)
      binding)))

(defn- names [params]
  (vec (map name params)))


(defmacro defresource [& args]
  (let [{:keys [fn-name method path query-params form-params path-params header-params cookie-params body]} (parse-args args)]
    `(defn ~(bind-resource method path (names query-params) (names form-params) (names path-params) (names header-params) (names cookie-params)
              fn-name) ~(vec (concat query-params form-params path-params header-params cookie-params))
       ~@body)))


