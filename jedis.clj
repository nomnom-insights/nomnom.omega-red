(import (redis.clients.jedis  Jedis JedisPool JedisPooled))

(def p (JedisPooled. "0.0.0.0" 6379))

(.close p)


;; https://javadoc.io/doc/redis.clients/jedis/2.9.0/redis/clients/jedis/Jedis.html


(defmacro execute [conn cmd+args]
  (let [[cmd & args] cmd+args
        argv (vec args)
        cmd-sym (symbol (name cmd))]
    `(. #^redis.clients.jedis.JedisPooled ~conn ~cmd-sym  ~@argv)))


(defn varargs [c argv]
  (let [arrified (into-array c argv)]
  (vary-meta arrified assoc :tag (class arrified))))

(defn string-array [v]
^"[Ljava.lang.String;" (into-array String v))

  (execute p [:rpush "fo1" (into-array String [ "one" "two" "one" "two" "one" "two" "one" "two"])])
  (execute p [:rpush "fo1" "one" "two"])
  (execute p [:lrange "fo1" 0 -1]) ;; => ["one" "two" "one" "two" "one" "two" "one" "two"]
  (execute p [:setex "foo" 10 "bar"])  ;;  "OK"
  (execute p [:get "foo"]) ;;  nil after expiry

  (execute p [:hmset "test" {"foo" "bar"}]) ;; OK
  (execute p [:hmget "test" (into-array ["foo"])]) ;; ["bar"]
;; note camelCase!
(execute p [:hgetAll "test"]) ;; {"foo" "bar"}
;; ugly but doesn't reflect
  (execute p [:hmget "test" ^"[Ljava.lang.String;" (into-array String ["foo"])])
  (execute p [:hmget "test" (string-array ["foo"])])


(.hmget p "test" ^"[Ljava.lang.String;" (into-array ["foo"]))

(clojure.walk/macroexpand-all '(inv r [:get "foo"]))
(clojure.walk/macroexpand-all '(execute ^Jedis r [:hmget "test" (into-array String ["foo"])]))
(clojure.walk/macroexpand-all   '(execute p [:rpush "fo1" (varargs String [ "one" "two" "one" "two" "one" "two" "one" "two"])]))


(class (into-array  [1]))
