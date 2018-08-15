(defproject broker-searcher "0.1.0-SNAPSHOT"
  :description "Open Service Broker Searcher."
  :url "https://github.com/evanlouie/osb-searcher"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.7"]]
  :main ^:skip-aot broker-searcher.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
