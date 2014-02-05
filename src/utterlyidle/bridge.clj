(ns utterlyidle.bridge
  (:import (com.googlecode.totallylazy Pair Option Sequences Uri)
           (com.googlecode.utterlyidle NamedParameter QueryParameters FormParameters HeaderParameters PathParameters
                                       Request Binding UriTemplate Application ParametersExtractor Response Requests
                                       Responses Status)
           (com.googlecode.utterlyidle.cookies CookieParameters)
           (java.lang.reflect ParameterizedType Type)
           (com.googlecode.utterlyidle.bindings.actions Action)
           (clojure.lang IFn)
           (com.googlecode.utterlyidle.dsl DefinedParameter StaticBindingBuilder)
           (com.googlecode.utterlyidle.bindings MatchedBinding)
           (com.googlecode.yadic Resolver)
           (com.googlecode.utterlyidle.handlers ClientHttpHandler))
  (:require [clojure.set :refer [map-invert]]
            [clojure.string :refer [join]]))


(defn- header-parameters->vec [headers]
  (vec (map #(vector (.first %) (.second %)) headers)))

(defn- vec->header-parameters [headers-list]
  (HeaderParameters/headerParameters
    (map (fn [[k v]] (Pair/pair k v)) headers-list)))

(defn entity [object]
  (or (get-in object [:request :entity])
      (get-in object [:response :entity])))

(defn status-code [response]
  (get-in response [:response :status :code]))

(defn status-description [response]
  (get-in response [:response :status :description]))

(defn headers [object]
  (or (get-in object [:request :headers])
      (get-in object [:response :headers])))

(defn response? [response-map]
  (and (associative? response-map)
       (contains? response-map :response)))

(defn request? [request-map]
  (and (associative? request-map)
       (contains? request-map :request)))

(defn request [params]
  {:request params})

(defn response [params]
  {:response params})

(defn response->map [^Response response]
  {:response
    {:status  {:code        (.. response (status) (code))
               :description (.. response (status) (description))}
     :headers (header-parameters->vec (.headers response))
     :entity  (.. response (entity) (toString))}})

(defn request->map [^Request request]
  {:request
    {:method  (.method request)
     :uri     (.. request (uri) (toString))
     :entity  (.. request (entity) (toString))
     :headers (header-parameters->vec (.headers request))}})

(defn map->request [request-map]
  (Requests/request (get-in request-map [:request :method])
                    (Uri/uri (get-in request-map [:request :uri]))
                    (vec->header-parameters (headers request-map))
                    (get-in request-map [:request :entity])))


(defn map->response [response-map]
  (Responses/response (Status/status (status-code response-map) (status-description response-map))
                      (vec->header-parameters (headers response-map))
                      (entity response-map)))

(defn- as-sequence [coll]
  (.. (Sequences/sequence) (join coll)))

(defn ^:dynamic client-http-handler []
  (ClientHttpHandler.))

(defn make-request [method uri & {:keys [headers body client] :or {client (client-http-handler)} :as req}]
  (-> (.handle client (map->request (request (merge {:method method :uri uri} (dissoc req :client)))))
      (response->map)))

(defn- named-parameter [parameter-type name]
  (Pair/pair String (Option/some (NamedParameter. name parameter-type (Option/none)))))

(defn request-param []
  (Pair/pair Request (Option/none)))

(defn query-param [name]
  (named-parameter QueryParameters name))

(defn form-param [name]
  (named-parameter FormParameters name))

(defn cookie-param [name]
  (named-parameter CookieParameters name))

(defn header-param [name]
  (named-parameter HeaderParameters name))

(defn path-param [name]
  (named-parameter PathParameters name))

(defn static-resources-binding [binding]
  (let [builder (.. (StaticBindingBuilder/in (:url binding)) (path (:path binding)))]
    (do
      (doall (map (fn [[extension media-type]] (.set builder extension media-type)) (:extensions binding)))
      (seq (.call builder)))))

(defn value-resolver [value]
  (reify Resolver
    (resolve [this type] value)))

(defn custom-type [name]
  (reify ParameterizedType
    (hashCode [this] (.hashCode name))
    (equals [this other] (.equals name other))
    (toString [this] (str "Custom type [" name "]"))
    (getActualTypeArguments [this] (into-array Type []))
    (getRawType [this] (class this))
    (getOwnerType [this] (class this))))

(defn- as-action-params [params]
  (map #(cond
         (instance? Request %) (request->map %)
         :default %
         ) params))

(defn- as-action-result [result]
  (cond
    (response? result) (map->response result)
    :default result))

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
            scoped-params (map #(.resolve request-scope (custom-type (str (first %))))
                               (get-in (meta func) [:binding :scoped-params]))]
        (as-action-result
          (apply func (as-action-params (concat scoped-params request-params))))))))

(defn- dispatch-function-parameters [func params]
  (cons (Pair/pair IFn (Option/some (DefinedParameter. IFn func)))
        params))

(defn create-binding [path method consumes produces function params]
  (Binding.
    (create-action)
    (UriTemplate/uriTemplate path)
    method
    (as-sequence consumes)
    (as-sequence produces)
    (as-sequence (dispatch-function-parameters function params))
    1 false nil))





