(ns omega-red.redis
  "Represents a redis connection spec,
  so we can use it when using Carmine, unlike the
  regular `wcar*` macro, recommended in Carmine docs."
  (:require [taoensso.carmine :as carmine]
            [taoensso.carmine.connections  :as connection]
            [omega-red.protocol :as proto]
            [com.stuartsierra.component :as component])
  (:import (java.io Closeable)))

(defn get-redis-fn*
  "Finds actual function instance, based on a keyword.
  So:
  :hset -> taoensso.carmine/hset
  which then can be used as normal function"
  [fn-name-keyword]
  (time (ns-resolve 'taoensso.carmine (symbol fn-name-keyword))))

(def get-redis-fn (memoize get-redis-fn*))

(defrecord Redis [pool spec]
  component/Lifecycle
  (start [this]
    (let [pool (connection/conn-pool :mem/fresh {})]
      (assoc this :pool pool)))
  (stop [this]
    (.close ^Closeable pool)
    (assoc this :pool nil :spec nil))
  proto/Redis
  (execute* [_this redis-fn args]
    (carmine/wcar {:pool pool :spec spec} (apply (get-redis-fn redis-fn) args)))
  (execute-pipeline* [_this redis-fns+args]
    (carmine/wcar {:pool pool :spec spec}
                  :as-pipeline
                  (mapv (fn [cmd+args]
                          (let [redis-fn (get-redis-fn (first cmd+args))]
                            (apply redis-fn (rest cmd+args))))
                        redis-fns+args))))

(defn create [{:keys [host port]}]
  {:pre [(string? host)
         (number? port)]}
  (map->Redis {:spec  {:host host :port port}}))
