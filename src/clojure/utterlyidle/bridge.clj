(ns utterlyidle.bridge
  (:import (com.googlecode.totallylazy Pair Option)
           (com.googlecode.utterlyidle NamedParameter QueryParameters FormParameters HeaderParameters PathParameters
                                       Request)
           (com.googlecode.utterlyidle.cookies CookieParameters)))

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
