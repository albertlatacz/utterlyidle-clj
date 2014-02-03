(ns utterlyidle.testing
  (:require [utterlyidle.bindings :refer :all]
            [utterlyidle.server :refer :all]))

(defmacro testing-server
  "Creates new testing server context for given bindings and invokes body within it.
  Use 'client' binding to query the server."
  [bindings & body]
  `(let [server# (start-server {} ~bindings)
         ~'client (.application (:server server#))]
     (try ~@body
          (finally (stop-server server#)))))
