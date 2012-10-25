(defproject utterlyidle-clojure "1.0.0-SNAPSHOT"
  :description "Clojure bindings for UtterlyIdle"
  :source-path "src/clojure"
  :java-source-path "src/java"
  :repositories {"Dan Bodart" "http://repo.bodar.com/"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.namespace "0.1.0"]
                 [com.googlecode.utterlyidle/utterlyidle "558"]
                 [com.googlecode.lazyrecords/lazyrecords "104"]
;                 [hiccup "0.3.6"]
                 ]
  :main utterlyidle.example.restServer
;  :aot utterlyidle
  )