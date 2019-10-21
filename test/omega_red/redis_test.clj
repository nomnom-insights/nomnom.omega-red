(ns omega-red.redis-test
  (:require [omega-red.protocol :as proto]
            [omega-red.redis]
            [clojure.test :refer :all]))

(deftest redis-ops
  (let [red (.start (omega-red.redis/create {:host "127.0.0.1" :port 6379}))]
    (is (= 0 (proto/execute red :exists "test.some.key")))
    (is (= "OK" (proto/execute red :set "test.some.key" "foo")))
    (is (= 1 (proto/execute red :exists "test.some.key")))
    (is (= "foo" (proto/execute red :get "test.some.key")))
    (is (= 1 (proto/execute red :del "test.some.key")))
    (.stop red)
    (is (nil? (proto/execute red :get "test.some.key")))))

(deftest redis-pipelne
  (let [red (.start (omega-red.redis/create {:host "127.0.0.1" :port 6379}))]
    (is (= 0 (proto/execute red :exists "test.some.key.pipe")))
    (is (= [nil "OK" "oh ok" 1]
           (proto/execute-pipeline red
                                   [:get "test.some.key.pipe"]
                                   [:set "test.some.key.pipe" "oh ok"]
                                   [:get "test.some.key.pipe"]
                                   [:del "test.some.key.pipe"])))
    (is (= 0 (proto/execute red :exists "test.some.key.pipe")))
    (.stop red)))
