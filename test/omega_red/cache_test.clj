(ns omega-red.cache-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures]]
    [com.stuartsierra.component  :as component]
    [omega-red.cache :as cache]
    [omega-red.protocol :as redis]
    [omega-red.redis]))


(def redis-config
  {:host (or (System/getenv "REDIS_URL") "127.0.0.1")
   :port (Integer/parseInt (or (System/getenv "REDIS_PORT") "6379"))})


(def system
  {:redis (omega-red.redis/create redis-config)
   :cache (component/using
            (cache/create)
            [:redis])})


(def state (atom 0))

(def sys (atom nil))


(defn stateful []
  (swap! state inc))


(use-fixtures :once (fn [test]
                      (reset! state 0)
                      (reset! sys (component/start (component/map->SystemMap system)))
                      (redis/execute (:redis @sys) [:del "testing:1"])
                      (test)
                      (swap! sys component/stop)))


(deftest cache-test
  (testing "caches result of fetch call"
    (is (= 1 (cache/fetch-and-set (:cache @sys) {:fetch stateful
                                                 :cache-get [:get "testing:1"]
                                                 :cache-set (fn [cache-result]
                                                              [:set "testing:1" cache-result])})))
    (is (= 2 (stateful)))
    (is (= 3 (stateful)))
    ;; hmmmmm, this is not helpful
    (is (= "1" (cache/fetch-and-set (:cache @sys) {:fetch stateful
                                                   :cache-get [:get "testing:1"]
                                                   :cache-set (fn [cache-result]
                                                                [:set "testing:1" cache-result])}))))
  (testing "cache invalidation scenario"
    (redis/execute (:redis @sys) [:del "testing:1"])
    (is (= 4 (cache/fetch-and-set (:cache @sys) {:fetch stateful
                                                 :cache-get [:get "testing:1"]
                                                 :cache-set (fn [cache-result]
                                                              [:set "testing:1" cache-result])})))
    (is (= 5 (stateful))))
  (testing "cache get can be a function"
    (is (= "4" (cache/fetch-and-set (:cache @sys) {:fetch stateful
                                                   :cache-get (fn [] [:get "testing:1"])
                                                   :cache-set (fn [cache-result]
                                                                [:set "testing:1" cache-result])})))))
