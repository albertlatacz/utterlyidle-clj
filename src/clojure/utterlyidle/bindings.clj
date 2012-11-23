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
  (:utterlyidle (meta func)))

(defn with-resources-in-ns [ns]
  (filter binding? (functions-in-namespace ns)))

(defn with-resources-in-dir [dir]
  (let [namespaces (find-namespaces-in-dir (file dir))]
    (require-all namespaces)
    (mapcat with-resources-in-ns namespaces)))

(defn fn->args [form]
  (let [form-sym (first form)]
    (if (and (symbol? form-sym) (= 'fn form-sym))
      (list (second form)))))

(defmacro extract-args [form]
  (if (symbol? form)
    (mapv #(mapv name %) (:arglists (meta (resolve form))))
    (mapv #(mapv name %) (fn->args form))
    ))


(defn with-binding-meta [method path consumes produces query-params form-params path-params header-params cookie-params request-params func args]
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
     :request-params (vec (:as (nth args 2)))
     :consumes (or (:consumes (nth args 2)) (consumes-for-method method))
     :produces (or (:produces (nth args 2)) (produces-for-method method))
     :body (drop 3 args)
     }))

(defn params-from-binding [binding]
  (let [{:keys [query-params form-params cookie-params header-params path-params request-params]} binding]
    (mapv
      (fn [arg]
        (cond
          (some #(= arg %) request-params) (ClojureBinding/requestParam)
          (some #(= arg %) query-params) (ClojureBinding/queryParam arg)
          (some #(= arg %) form-params) (ClojureBinding/formParam arg)
          (some #(= arg %) cookie-params) (ClojureBinding/cookieParam arg)
          (some #(= arg %) header-params) (ClojureBinding/headerParam arg)
          (some #(= arg %) path-params) (ClojureBinding/pathParam arg)))
      (first (:arguments binding)))))

(defn fn->binding [binding]
  (let [binding-meta (:utterlyidle (meta binding))]
    (prn binding-meta)
    (ClojureBinding/binding
      (:path binding-meta)
      (. (name (:method binding-meta)) toUpperCase)
      (into-array String (:consumes binding-meta))
      (into-array String (:produces binding-meta))
      binding
      (into-array Pair (params-from-binding binding-meta)))))

(defmacro with-resource [method path consumes produces params function]
  (let [{:keys [query-params form-params path-params header-params cookie-params as]} params]
    `(with-binding-meta
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
       (extract-args ~function))))

(defmacro defresource [& args]
  (let [{:keys [fn-name method path consumes produces query-params form-params path-params header-params cookie-params request-params body]} (parse-args args)
        fn-params (concat request-params query-params form-params path-params header-params cookie-params)]
    `(defn
       ~(with-binding-meta
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


