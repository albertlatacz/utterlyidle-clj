(ns utterlyidle.example.restServer
  (:use clojure.tools.namespace
        utterlyidle.bindings
        utterlyidle.server
        [clojure.java.io :only [file]]))

(defn -main [& m]
  (start 8001
    (bindings-in-dir "src/clojure/utterlyidle/example"))
  )