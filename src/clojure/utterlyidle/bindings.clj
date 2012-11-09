(ns utterlyidle.bindings
  (:use clojure.tools.namespace
        [clojure.java.io :only [file]]
        [clojure.core.match :only [match]])

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
  (:utterlyidle (meta func)))

(defn bindings-in-namespace [ns]
  (filter binding? (functions-in-namespace ns)))

(defn bindings-in-dir [dir]
  (let [namespaces (find-namespaces-in-dir (file dir))]
    (require-all namespaces)
    (mapcat bindings-in-namespace namespaces)))


(defn bind-resource [method path consumes produces query-params form-params path-params header-params cookie-params request-param func]
  (with-meta func
    (assoc (meta func)
      :utterlyidle {:method method
                    :path path
                    :consumes consumes
                    :produces produces
                    :query-params query-params
                    :form-params form-params
                    :path-params path-params
                    :header-params header-params
                    :cookie-params cookie-params
                    :request-param request-param
                    }
      )))


(defn- consumes-for-method [method]
  (cond
    (= method :get ) ["text/plain"]
    (= method :post ) ["text/plain" "application/x-www-form-urlencoded" "application/xml"]))

(defn- produces-for-method [method]
  (cond
    (= method :get ) ["text/html"]
    (= method :post ) ["text/html"]))

(defn parse-args [args]
  (let [method (nth (nth args 1) 0)]
    {:fn-name (nth args 0)
     :method method
     :path (nth (nth args 1) 1)
     :query-params (:query-params (nth args 2))
     :form-params (:form-params (nth args 2))
     :path-params (:path-params (nth args 2))
     :header-params (:header-params (nth args 2))
     :cookie-params (:cookie-params (nth args 2))
     :request-param (or (:as (nth args 2) 'request))
     :consumes (or (:consumes (nth args 2)) (consumes-for-method method))
     :produces (or (:produces (nth args 2)) (produces-for-method method))
     :body (drop 3 args)
     }))

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
  (let [binding-meta (:utterlyidle (meta binding))]
    (concat
      (map query-param (:query-params binding-meta))
      (map form-param (:form-params binding-meta))
      (map cookie-param (:cookie-params binding-meta))
      (map header-param (:header-params binding-meta))
      (map path-param (:path-params binding-meta))
      )))

(defn fn-to-binding [binding]
  (let [binding-meta (:utterlyidle (meta binding))]
    (ui-binding
      (:method binding-meta)
      (:path binding-meta)
      (:consumes binding-meta)
      (:produces binding-meta)
      (params-from-binding binding)
      binding)))

(defn- names [params]
  (vec (map name params)))


(defmacro defresource [& args]
  (let [{:keys [fn-name method path consumes produces query-params form-params path-params header-params cookie-params request-param body]} (parse-args args)]
    `(defn
       ~(bind-resource
          method
          path
          consumes
          produces
          (names query-params)
          (names form-params)
          (names path-params)
          (names header-params)
          (names cookie-params)
          (name (quote request-param))
          fn-name)
       ~(vec (concat [request-param] query-params form-params path-params header-params cookie-params))
       ~@body)))


