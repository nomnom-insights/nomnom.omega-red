(ns omega-red.protocol)

(defprotocol Redis
  ;; Protocols do not support var-args (without macro magic)
  ;; The idea is that we  define a wrapper fn which is variadic and dispatch on that
  ;; A bit odd, but works when outside,
  ;; with the exception that record implementing the interface has to implement
  ;; execute* and execute-pipeline*
  (execute* [this redis-fn args]
    "Executes single redis command - use omega-red.protocol/execute to invoke!")
  (execute-pipeline* [this redis-fns+args]
    "Executes a series of commands + their args in a pipeline. Commands are a vector of vecs with the commands and their args. Use omega-red.protocol/excute-pipeline to invoke!"))

(defn execute
  "Wrapper function to allow variadic args dispatch.
  Invokes a redis command + its args:
  (execute redis :get \"key.name\")"
  [this redis-fn & args]
  (execute* this redis-fn args))

(defn execute-pipeline
  "Wrapper function to allow variadic args dispatch.
  Executes a series of redis commands in a pipeline:
  (execute-pipeline redis [:get \"key\"] [:del \"key\"]"
  [this & redis-fns+args]
  (execute-pipeline* this redis-fns+args))
