(def build-number (or (System/getenv "UTTERLYIDLE_CLJ_BUILD_NUMBER") "DEV-SNAPSHOT"))

(defproject utterlyidle-clj build-number
  :description "Clojure bindings for UtterlyIdle"
  :url "https://github.com/albertlatacz/utterlyidle-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :repositories {"Dan Bodart" "http://repo.bodar.com.s3.amazonaws.com/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [com.googlecode.utterlyidle/utterlyidle "733"]]
  :aot [utterlyidle.bindings utterlyidle.server]
  :main utterlyidle.example.server)