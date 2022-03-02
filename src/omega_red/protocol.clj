(ns omega-red.protocol)


(defprotocol Redis
  (execute
    [this redis-fn+args]
    "Executes single redis command - passed as JDBC-style vector: [:command the rest of args]")
  (execute-pipeline
    [this redis-fns+args]
    "Executes a series of commands + their args in a pipeline. Commands are a vector of vecs with the commands and their args. Use omega-red.protocol/excute-pipeline to invoke!"))


(defprotocol Caching
  (cache-get-or-fetch
    [this {:keys [fetch cache-set cache-get]}]
    "Protocol for implementing read or fetch + cache workflow. `fetch` function returns the data you want to hold on to. `cache-set` function accepts 2 arguments: the redis connection component and the result of fetch. `cache-get` accepts 1 arg - the redis connection and does the fetch for you"))
