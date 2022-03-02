(ns omega-red.cache-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures]]
    [com.stuartsierra.component  :as component]
    [omega-red.protocol :as redis]
    [omega-red.redis]))


(def redis-config
  {:host (or (System/getenv "REDIS_URL") "127.0.0.1")
   :port (Integer/parseInt (or (System/getenv "REDIS_PORT") "6379"))})


(def system
  {:redis (omega-red.redis/create redis-config)})


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
    (let [get-or-fetch #(redis/cache-get-or-fetch (:redis @sys) {:fetch stateful
                                                                 :cache-get (fn cache-get' [r]
                                                                              (when-let [v (redis/execute r [:get "testing:1"])]
                                                                                (Long/parseLong v)))
                                                                 :cache-set (fn cache-set' [r cache-result]
                                                                              (redis/execute r [:set "testing:1" cache-result]))})]
      (is (= 1 (get-or-fetch)))
      (is (= 2 (stateful)))
      (is (= 3 (stateful)))

      (is (= 1 (get-or-fetch)))
      (testing "cache invalidation scenario"
        (redis/execute (:redis @sys) [:del "testing:1"])
        (is (= 4 (get-or-fetch)))
        (is (= 5 (stateful)))))))
