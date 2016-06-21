(defproject soundera "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.76"]
                 [org.clojure/core.async "0.2.385"]
                 
                 [com.cemerick/url "0.1.1"]]

  :plugins [[lein-cljsbuild "1.1.3"]]

  :cljsbuild {
    :builds [{
      :id "dev"
      :source-paths ["src/cljs"]
      :compiler {
        :output-to "resources/web/js/gen/dev/soundera.js"
        :output-dir "resources/web/js/gen/dev"
        :optimizations :none
        :source-map true
        :pretty-print true
      }
    }
    {
      :id "prod"
      :source-paths ["src/cljs"]
      :compiler {
        :output-to "resources/web/js/gen/prod/soundera.js"
        :output-dir "resources/web/js/gen/prod"
        :optimizations :advanced
        :pretty-print false
      }
    }]
  }

  :source-paths ["src/clj"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
)
