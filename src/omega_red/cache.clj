(ns omega-red.cache
  (:require
    [omega-red.protocol :as redis]))


(defprotocol Caching
  (fetch-and-set
    [this {:keys [fetch cache-set cache-get]}]
    "Protocol for implementing read or fetch + cache workflow. At minium, `fetch-and-set` should return result of `fetch`"))


(defn fetch-and-set* [redis {:keys [fetch ; function that fetches data
                                    cache-set ; function that receives the fetch result and returns redis args for caching
                                    cache-get ; vector of args to pass to redis, or a function that returns one to read from cache
                                    ]}]
  (or
    ;; XXX: are read ops always returning falsey value for cache miss?
    (redis/execute redis (if (fn? cache-get) (cache-get) cache-get))
    (let [result (fetch)]
      (redis/execute redis (cache-set result))
      result)))


(defrecord Cache [redis]
  Caching
  (fetch-and-set [this {:keys [fetch cache-get cache-set]}]
    (fetch-and-set* (:redis this) {:fetch fetch
                                   :cache-get cache-get
                                   :cache-set cache-set})))


(defn create []
  (->Cache nil))
