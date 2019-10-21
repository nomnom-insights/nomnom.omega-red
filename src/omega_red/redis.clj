(ns omega-red.redis
  "Represents a redis connection spec,
  so we can use it when using Carmine, unlike the
  regular `wcar*` macro, recommended in Carmine docs."
  (:require [taoensso.carmine :as carmine]
            [omega-red.protocol :as proto]
            [com.stuartsierra.component :as component]))

(defn get-redis-fn*
  "Finds actual function instance, based on a keyword.
  So:
  :hset -> taoensso.carmine/hset
  which then can be used as normal function"
  [fn-name-keyword]
  (ns-resolve 'taoensso.carmine (symbol fn-name-keyword)))

(def get-redis-fn (memoize get-redis-fn*))

(defrecord Redis [pool spec conn]
  component/Lifecycle
  (start [this]
    (assoc this :conn {:pool pool :spec spec}))
  (stop [this]
    (assoc this :conn nil))
  proto/Redis
  (execute* [this redis-fn args]
    (carmine/wcar conn (apply (get-redis-fn redis-fn) args)))
  (execute-pipeline* [this redis-fns+args]
    (carmine/wcar conn
                  :as-pipeline
                  (mapv (fn [cmd+args]
                          (let [redis-fn (get-redis-fn (first cmd+args))]
                            (apply redis-fn (rest cmd+args))))
                        redis-fns+args))))

(defn create [{:keys [host port]}]
  {:pre [(string? host)
         (number? port)]}
  (->Redis {} {:host host :port port} nil))
