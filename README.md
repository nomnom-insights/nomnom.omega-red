# omega-red

<img  src="https://uncannyxmen.net/sites/default/files/images/characters/omegared/omegared00.jpg" heighth="400px" align=right >

## A Redis component, based on [Carmine](https://github.com/ptaoussanis/carmine)

A simple Redis client wrapping [Carmine](https://github.com/ptaoussanis/carmine)
It's not meant to be a fancy DSL or an ORM. Just helps with componentizing the connection
and invoking Redis commands in more idiomatic way, without using macros, like the `wcar*` macro in Carmine's readme.


#### Design

The idea is that the component wraps the conection, and you pass "raw" Redis commands, like in the Redis shell or CLI, rather than invoking Carmine command functions + their arguments. The way it works is that  we convert the first keyword (command) to a Carmine function, cache it and then invoke it. The lookup/conversion is cached for later so that we don't pay the cost of the lookup too often.

If you want to mock the component - you'll need something that implements the following:

- `(execute* this <command> & args)` - for  single commands
- `(exececute-pipeline* this [ [command1  & args] [command2 & args]...])` - for pipeline

and fakes Redis behavior as needed.


# Example

```clojure
(ns omega-red.redis-test
  (:require [omega-red.protocol :as proto]
            [omega-red.redis]
            [com.stuartsierra.component :as component]))

(let [red (componet/start (omega-red.redis/create {:host "127.0.0.1" :port 6379}))]
    (println (= 0 (proto/execute red :exists "test.some.key"))) ; true
    (println (= "OK" (proto/execute red :set "test.some.key" "foo"))) ; true
    (println (= 1 (proto/execute red :exists "test.some.key"))) ; true
    (println (= "foo" (proto/execute red :get "test.some.key"))) ; true
    (println (= 1 (proto/execute red :del "test.some.key"))) ; true
    (component/stop red)
    (println (nil? (proto/execute red :get "test.some.key")))) ; true

;; pipeline execution
(println (= [nil "OK" "oh ok" 1]
       (proto/execute-pipeline red
                               [:get "test.some.key.pipe"]
                               [:set "test.some.key.pipe" "oh ok"]
                               [:get "test.some.key.pipe"]
                               [:del "test.some.key.pipe"]))) ; true

```

# Roadmap

Nothing at the moment. It's possible to create Components for Carmine's various exentions (message queue, Tundra) but they're not needed at the moment.

# Authors

<sup>In alphabetical order</sup>

- [Afonso Tsukamoto](https://github.com/AfonsoTsukamoto)
- [Łukasz Korecki](https://github.com/lukaszkorecki)
- [Marketa Adamova](https://github.com/MarketaAdamova)
