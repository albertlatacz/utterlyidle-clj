(ns utterlyidle.testing
  (:require [utterlyidle.bindings :refer :all]
            [utterlyidle.bridge :refer :all]
            [utterlyidle.core :refer :all]))

(defmacro testing-server
  "Creates new testing server context for given bindings and invokes body within it.
  Use 'client' binding to query the server."
  [bindings & body]
  `(let [server# (apply start-server (cons {} (flatten [~bindings])))]
     (binding [client-http-handler (fn[] (.application (:server server#)))]
       (try ~@body
          (finally (stop-server server#))))))
