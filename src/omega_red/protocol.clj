(ns omega-red.protocol)


(defprotocol Redis
  (execute
    [this redis-fn+args]
    "Executes single redis command - passed as JDBC-style vector: [:command the rest of args]")
  (execute-pipeline
    [this redis-fns+args]
    "Executes a series of commands + their args in a pipeline. Commands are a vector of vecs with the commands and their args. Use omega-red.protocol/excute-pipeline to invoke!"))


(defn cache-get-or-fetch
  "Tiny helper for the usual 'fetch from cache, and if there's a miss, use fetch function to get the data but also cache it'
  Options:
    - `cache-get` - function to fetch from cache (usally redis), accepts 0 args
    - `fetch` - the function to fetch data from a slow resource, accepts 0 args
    - `cache-set` - the function to store data in cache, receives result of `fetch` as the only arg

  Note:
  You need to ensure that resuls of `fetch` and `cache-get` return the same types, e.g. Redis' `SET foo 1`
  will cast 1 as string on read!"
  [{:keys [fetch cache-set cache-get]}]
  {:pre [(fn? fetch)
         (fn? cache-set)
         (fn? cache-get)]}
  (if-let [from-cache (cache-get)]
    from-cache
    (let [fetch-res (fetch)]
      (cache-set fetch-res)
      fetch-res)))
