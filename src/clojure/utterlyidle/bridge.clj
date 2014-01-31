(ns utterlyidle.bridge
  (:import (com.googlecode.totallylazy Pair Option Sequences)
           (com.googlecode.utterlyidle NamedParameter QueryParameters FormParameters HeaderParameters PathParameters
                                       Request Binding UriTemplate)
           (com.googlecode.utterlyidle.cookies CookieParameters)
           (java.lang.reflect ParameterizedType Type)
           (com.googlecode.utterlyidle.bindings.actions Action)
           (clojure.lang IFn)
           (com.googlecode.utterlyidle.dsl DefinedParameter)))

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

(defn custom-type [name]
  (reify ParameterizedType
    (hashCode [this] (.hashCode name))
    (equals [this other] (.equals name other))
    (toString [this] (str "Custom type [" name "]"))
    (getActualTypeArguments [this] (into-array Type []))
    (getRawType [this] (class this))
    (getOwnerType [this] (class this))))


(defn- invoke-clojure [container]
  )

(defn- create-action []
  (reify Action
    (description [this] "Clojure Binding")
    (invoke [this container] (invoke-clojure container))
    (metaData [this] [])))

(defn- function-param [func]
  (Pair/pair IFn (Option/some (DefinedParameter. IFn func ))))

(defn- dispatch-function-parameters [func params]
  ;private static Sequence<Pair<Type, Option<Parameter>>> dispatchMethodParameters (IFn function, Pair<Type, Option<Parameter>> [] params)
  ;{
  ;  return sequence (functionParam (function))
  ;         .join (sequence (params)) ;
  ;         }
  )




(defn create-binding [path method consumes produces function params]
  ;(Binding. (create-action)
  ;          (UriTemplate/uriTemplate path)
  ;          method
  ;          (Sequences/sequence consumes)
  ;          (Sequences/sequence produces)
  ;          )




  ;public static Binding binding(String path, String method, String[] consumes, String[] produces, IFn function, Pair<Type, Option<Parameter>>[] params) throws NoSuchMethodException {
  ;return new Binding(
  ;                    dispatchAction(),
  ;                                  uriTemplate(path),
  ;                                  method,
  ;                                  sequence(consumes),
  ;                                  sequence(produces),
  ;                                  dispatchMethodParameters(function, params),
  ;                                  1, false, null);
  ;}
  )



