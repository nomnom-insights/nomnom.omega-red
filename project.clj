(defproject nomnom/omega-red "0.1.0"
  :description "Component firendly Redis client, based on Carmine"
  :url "https://github.com/nomnom-insights/nomnom.omega-red"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :deploy-repositories {"clojars" {:sign-releases false
                                   :username  :env/clojars_username
                                   :password :env/clojars_password}}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.stuartsierra/component "1.0.0"]
                 [com.taoensso/carmine "3.0.0"]]
  :plugins [[lein-cloverage "1.0.13" :exclusions [org.clojure/clojure]]]
  :globals {*warn-on-reflection* true}
  :profiles {:dev
             {:dependencies  [[org.clojure/tools.logging "1.1.0"]]}})
