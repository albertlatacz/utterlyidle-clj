(ns utterlyidle.client
  (:import [com.googlecode.utterlyidle RequestBuilder]
           [com.googlecode.utterlyidle.handlers ClientHttpHandler])
  (:refer-clojure :exclude (get)))


(defn- to-response-map [response]
  {:body (.. response (entity) (toString))
   :status {:code (.. response (status) (code))
            :description (.. response (status) (description))}
   })

(defn- make-request [method url params-builder more]
  (let [builder (RequestBuilder. method url)
        {:keys [accepting contentType]} more]
    (to-response-map
      (do
        (params-builder builder)
        (.. (ClientHttpHandler.)
          (handle (.. builder
                    (accepting accepting)
                    (contentType contentType)
                    (build)))
          )))))

(defn- with-query-params [params builder]
  (doseq [item params]
    (.. builder (query (name (key item)) (val item)))))

(defn- with-form-params [params builder]
  (doseq [item params]
    (.. builder (form (name (key item)) (val item)))))

(defn- with-request-body [body builder]
  (.. builder (entity body)))

(defn get
  ([url] (get url {}))
  ([url params]
    (make-request "GET" url (partial with-query-params params) {})))

(defn post
  ([url] (post url {}))
  ([url params]
    (make-request "POST" url (partial with-form-params params) {:contentType "application/x-www-form-urlencoded"}))
  ([url content-type body]
    (make-request "POST" url (partial with-request-body body) {:contentType content-type})))
