(ns utterlyidle.core
  (:import (com.googlecode.utterlyidle ServerConfiguration BasePath RestApplication Binding ParametersExtractor
                                       HeaderParameters Request Responses Status NamedParameter PathParameters
                                       FormParameters QueryParameters Application UriTemplate)
           (java.net InetAddress URLEncoder)
           (com.googlecode.utterlyidle.modules Modules)
           (com.googlecode.utterlyidle.httpserver RestServer)
           (com.googlecode.utterlyidle.annotations HttpMethod)
           (com.googlecode.utterlyidle.handlers ClientHttpHandler)
           (com.googlecode.totallylazy Pair Sequences Option)
           (com.googlecode.utterlyidle.cookies CookieParameters)
           (com.googlecode.utterlyidle.dsl StaticBindingBuilder DefinedParameter)
           (com.googlecode.yadic Resolver)
           (java.lang.reflect ParameterizedType Type)
           (com.googlecode.utterlyidle.bindings MatchedBinding)
           (clojure.lang IFn)
           (com.googlecode.utterlyidle.bindings.actions Action))
  (:require [utterlyidle.core.utils :refer :all]))


(defn- named-parameter [parameter-type name]
  (Pair/pair String (Option/some (NamedParameter. name parameter-type (Option/none)))))

(defn- request-param []
  (Pair/pair Request (Option/none)))

(defn- query-param [name]
  (named-parameter QueryParameters name))

(defn- form-param [name]
  (named-parameter FormParameters name))

(defn- cookie-param [name]
  (named-parameter CookieParameters name))

(defn- header-param [name]
  (named-parameter HeaderParameters name))

(defn- path-param [name]
  (named-parameter PathParameters name))

(defn- static-resources-binding [binding]
  (let [builder (.. (StaticBindingBuilder/in (:url binding)) (path (:path binding)))]
    (do
      (doall (map (fn [[extension media-type]] (.set builder extension media-type)) (:extensions binding)))
      (seq (.call builder)))))

(defn- value-resolver [value]
  (reify Resolver
    (resolve [this type] value)))


(defn- as-action-params [params]
  (map #(cond
         (instance? Request %) (request->map %)
         :default %
         ) params))

(defn- as-action-result [result]
  (cond
    (response? result) (map->response result)
    :default result))

(defrecord CustomType [name]
  Type
  (toString [this] name))

(defn- custom-type [name]
  (CustomType. name))

(defn- create-action []
  (reify Action
    (description [this] "Clojure Binding")
    (metaData [this] [])
    (invoke [this request-scope]
      (let [request (.get request-scope Request)
            application (.get request-scope Application)
            binding (.. request-scope (get MatchedBinding) (value))
            [func & request-params] (seq (.. (ParametersExtractor. (.uriTemplate binding) application (.parameters binding))
                                             (extract request)))
            scoped-params (map (fn[[k v]] (.resolve request-scope (custom-type (name k))))
                               (get-in (meta func) [:binding :scoped-params]))]
        (as-action-result
          (apply func (as-action-params (concat scoped-params request-params))))))))

(defn- dispatch-function-parameters [func params]
  (cons (Pair/pair IFn (Option/some (DefinedParameter. IFn func)))
        params))

(defn- create-binding [path method consumes produces function params]
  (Binding.
    (create-action)
    (UriTemplate/uriTemplate path)
    method
    (as-sequence consumes)
    (as-sequence produces)
    (as-sequence (dispatch-function-parameters function params))
    1 false nil))

(defn- binding->params [binding]
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

(defn- fn->binding [func]
  (let [binding (:binding (meta func))]
    (vector
      (create-binding
        (:path binding)
        (.. (name (:method binding)) (toUpperCase))
        (:consumes binding)
        (:produces binding)
        func
        (binding->params binding)))))

(defn- as-binding [obj]
  (cond
    (instance? ResourceBinding (:binding (meta obj))) (fn->binding obj)
    (instance? StaticResourceBinding obj) (static-resources-binding obj)
    :default []))

(defn- bindings->array [bindings]
  (into-array ^Binding (mapcat as-binding (flatten bindings))))



(defn start-server
  "Starts server with specified resource bindings.
  e.g (start-server {:port 8080 :base-path \"/\"
      (with-resources-in-dir \"src/clojure/utterlyidle/example\"))"
  [{:keys [port base-path max-threads bind-address] :or {port 0, base-path "/", max-threads 50, bind-address "0.0.0.0"}} & bindings]
  (let [config (.. (ServerConfiguration.)
                   (port port)
                   (maxThreadNumber max-threads)
                   (bindAddress (InetAddress/getByName bind-address))
                   (basePath (BasePath/basePath base-path)))
        application (RestApplication. (BasePath/basePath base-path))]
    (.. application (add (Modules/bindingsModule (bindings->array bindings))))
    (let [x (filter #(instance? ScopedParameterBinding %) (flatten bindings))]
      (doseq [param x]
        (.. application
            (applicationScope)
            (addType (custom-type (name (:name param)))
                     (value-resolver (:value param))))))
    {:server (RestServer. application config)}))


(defn stop-server [server]
  (.close (:server server)))


(defmacro testing-server
  "Creates new testing server context for given bindings and invokes body within it.
  Use 'client' binding to query the server."
  [bindings & body]
  `(let [server# (apply start-server (cons {} (flatten [~bindings])))]
     (binding [client-http-handler (fn[] (.application (:server server#)))]
       (try ~@body
            (finally (stop-server server#))))))
