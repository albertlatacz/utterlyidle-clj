(defproject utterlyidle-clj "1.0.0"
  :description "Clojure bindings for UtterlyIdle"
  :url "https://github.com/albertlatacz/utterlyidle-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-path "src/clojure"
  :java-source-path "src/java"
  :repositories {"Dan Bodart" "http://repo.bodar.com.s3.amazonaws.com/"}
  :plugins [[codox "0.6.3"]]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.namespace "0.1.0"]
                 [com.googlecode.utterlyidle/utterlyidle "558"]
                 [com.googlecode.lazyrecords/lazyrecords "104"]
                 ;                 [hiccup "0.3.6"]
                 ]
  :dev-dependencies [[codox "0.4.0"]]
  :main utterlyidle.example.restServer
  ;  :aot utterlyidle
  )