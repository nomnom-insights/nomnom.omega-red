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
                      (redis/execute (:redis @sys) [:del "testing:2"])
                      (redis/execute (:redis @sys) [:del "testing:bools"])
                      (test)
                      (swap! sys component/stop)))


(deftest cache-test
  (testing "caches result of fetch call"
    (let [get-or-fetch #(redis/cache-get-or-fetch {:fetch stateful
                                                   :cache-get (fn cache-get' []
                                                                (when-let [v (redis/execute (:redis @sys) [:get "testing:1"])]
                                                                  (Long/parseLong v)))
                                                   :cache-set (fn cache-set' [cache-result]
                                                                (redis/execute  (:redis @sys) [:set "testing:1" cache-result]))})]
      (is (= 1 (get-or-fetch)))
      (is (= 2 (stateful)))
      (is (= 3 (stateful)))
      (is (= 1 (get-or-fetch)))
      (testing "cache invalidation scenario"
        (redis/execute (:redis @sys) [:del "testing:1"])
        (is (= 4 (get-or-fetch)))
        (is (= 5 (stateful))))))
  (testing "different data types"
    (let [get-or-fetch #(redis/cache-get-or-fetch {:fetch (fn [] (str (stateful)))
                                                   :cache-get (fn cache-get' []
                                                                (redis/execute  (:redis @sys) [:hget "testing:2" "foo"]))
                                                   :cache-set (fn cache-set' [cache-result]
                                                                (redis/execute  (:redis @sys) [:hset "testing:2" "foo" cache-result]))})]
      ;; increments again because we're checking a different cache key!
      (is (= "6" (get-or-fetch)))
      (is (= "6" (get-or-fetch)))
      (is (= 7 (stateful)))
      (is (= "6" (get-or-fetch)))
      (redis/execute (:redis @sys) [:del "testing:2"])
      (is (= "8" (get-or-fetch)))))

  (testing "handles false boolean content correctly"
    (let [fetches (atom 0)
          get-or-fetch #(redis/cache-get-or-fetch {:fetch (fn []
                                                            (swap! fetches inc)
                                                            false)
                                                   :cache-get (fn cache-get' []
                                                                (redis/execute (:redis @sys) [:get "testing:bools"]))
                                                   :cache-set (fn cache-set' [cache-result]
                                                                (redis/execute  (:redis @sys) [:set  "testing:bools" cache-result]))})]
      (is (= (get-or-fetch) false))
      (is (= (get-or-fetch) false))
      (is (= (get-or-fetch) false))
      (is (= 1 @fetches)))))
