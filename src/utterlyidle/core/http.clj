(ns utterlyidle.core
  (:import (java.net URLEncoder URLDecoder))
  (:require [clojure.string :refer [join split]]
            [utterlyidle.core.utils :refer :all]))

(defn url-encode
  [unencoded & [encoding]]
  (URLEncoder/encode unencoded (or encoding "UTF-8")))

(defn url-decode
  [encoded & [encoding]]
  (URLDecoder/decode encoded (or encoding "UTF-8")))

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


(defn- as-request-params [params encoding]
  (letfn [(param-name [param]
                      (if (keyword? param) (name param) (str param)))
          (explode-params [[name values]]
                          (map #(str (param-name name) "=" (url-encode (str %) encoding))
                               (flatten (vector values))))]
    (join "&" (mapcat explode-params params))))


(defn parse-query [query & [encoding]]
  (as-> query params
        (split params #"&")
        (map (comp (fn[x] (map #(url-decode % encoding) x))
                   #(split % #"=")) params)
        (group-by first params)
        (map-values (comp single-or-coll
                          #(mapv second %)) params)))

(defn parse-uri [uri & [encoding]]
  (let [RFC-3986 #"^(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*)(?:\?([^#]*))?(?:#(.*))?"
        uri-parts (re-find RFC-3986 uri)]
    {:uri       (uri-parts 0)
     :scheme    (uri-parts 1)
     :authority (uri-parts 2)
     :path      (uri-parts 3)
     :query     (uri-parts 4)
     :fragment  (uri-parts 5)}))


(defn uri [base & {:keys [params encoding]}]
  (let [query-params (as-request-params params encoding)]
    (if-not (empty? query-params)
      (str base "?" query-params)
      base)))

(defn form [& {:keys [params encoding]}]
  (as-request-params params encoding))



; Headers
(def Accept "Accept")
(def Accept-Charset "Accept-Charset")
(def Accept-Encoding "Accept-Encoding")
(def Accept-Language "Accept-Language")
(def Authorization "Authorization")
(def Cache-Control "Cache-Control")
(def Content-Encoding "Content-Encoding")
(def Content-Language "Content-Language")
(def Content-Length "Content-Length")
(def Content-Location "Content-Location")
(def Content-Type "Content-Type")
(def Content-MD5 "Content-MD5")
(def Date "Date")
(def ETag "ETag")
(def Expires "Expires")
(def Host "Host")
(def If-Match "If-Match")
(def If-Modified-Since "If-Modified-Since")
(def If-None-Match "If-None-Match")
(def If-Unmodified-Since "If-Unmodified-Since")
(def Last-Modified "Last-Modified")
(def Location "Location")
(def User-Agent "User-Agent")
(def Vary "Vary")
(def WWW-Authenticate "WWW-Authenticate")
(def Cookie "Cookie")
(def Set-Cookie "Set-Cookie")
(def X-Forwarded-For "X-Forwarded-For")
(def X-Forwarded-Proto "X-Forwarded-Proto")
(def X-CorrelationID "X-CorrelationID")
(def Transfer-Encoding "Transfer-Encoding")
(def Access-Control-Allow-Origin "Access-Control-Allow-Origin")

; Media types
(def wildcard "*/*")
(def application-xml "application/xml")
(def application-atom-xml "application/atom+xml")
(def application-xhtml-xml "application/xhtml+xml")
(def application-svg-xml "application/svg+xml")
(def application-javascript "application/javascript")
(def application-json "application/json")
(def application-pdf "application/pdf")
(def application-form-urlencoded "application/x-www-form-urlencoded")
(def application-octet-stream "application/octet-stream")
(def application-ms-excel "application/vnd.ms-excel")
(def multipart-form-data "multipart/form-data")
(def text-plain "text/plain")
(def text-csv "text/csv")
(def text-xml "text/xml")
(def text-html "text/html")
(def text-css "text/css")
(def text-javascript "text/javascript")
(def text-cache-manifest "text/cache-manifest")
(def image-png "image/png")
(def image-gif "image/gif")
(def image-x-icon "image/x-icon")
(def image-jpeg "image/jpeg")
(def image-svg "image/svg+xml")