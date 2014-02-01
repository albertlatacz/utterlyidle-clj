(ns utterlyidle.bridge
  (:import (com.googlecode.totallylazy Pair Option Sequences)
           (com.googlecode.utterlyidle NamedParameter QueryParameters FormParameters HeaderParameters PathParameters
                                       Request Binding UriTemplate Application ParametersExtractor)
           (com.googlecode.utterlyidle.cookies CookieParameters)
           (java.lang.reflect ParameterizedType Type)
           (com.googlecode.utterlyidle.bindings.actions Action)
           (clojure.lang IFn)
           (com.googlecode.utterlyidle.dsl DefinedParameter StaticBindingBuilder)
           (com.googlecode.utterlyidle.bindings MatchedBinding)
           (com.googlecode.yadic Resolver))
  (:require [clojure.set :refer [map-invert]])
  )

(defn- as-sequence [coll]
  (.. (Sequences/sequence) (join coll)))

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

(defn static-resources-binding [url path]
  (seq (.. (StaticBindingBuilder/in url)
           (path path)
           (call))))

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
        (apply func (concat scoped-params request-params))))))

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