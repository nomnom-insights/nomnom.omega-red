# omega-red

[![CircleCI](https://circleci.com/gh/nomnom-insights/nomnom.omega-red.svg?style=svg)](https://circleci.com/gh/nomnom-insights/nomnom.omega-red)

[![Clojars Project](https://img.shields.io/clojars/v/nomnom/omega-red.svg)](https://clojars.org/nomnom/omega-red)

<img  src="https://uncannyxmen.net/sites/default/files/images/characters/omegared/omegared00.jpg" heighth="400px" align=right >

## A Redis component, based on [Carmine](https://github.com/ptaoussanis/carmine)

A simple Redis client wrapping [Carmine](https://github.com/ptaoussanis/carmine)
It's not meant to be a fancy DSL or an ORM. Just helps with componentizing the connection
and invoking Redis commands in more idiomatic way, without using macros, like the `wcar*` macro in Carmine's readme.


#### Design

The idea is that the component wraps the conection, and you pass "raw" Redis commands, like in the Redis shell or CLI, rather than invoking Carmine command functions + their arguments. The way it works is that  we convert the first keyword (command) to a Carmine function, cache it and then invoke it. The lookup/conversion is cached for later so that we don't pay the cost of the lookup too often.

If you want to mock the component - you'll need something that implements the following protocol (defined as  `omega-red.protocol.Redis`):

- `(execute this [:command + args])` - for  single commands
- `(exececute-pipeline this [ [:command1 + args] [:command2 + args]...])` - for pipeline operations

and fakes Redis behavior as needed.


# Example

```clojure
(ns omega-red.redis-test
  (:require [omega-red.protocol :as proto]
            [omega-red.redis]
            [com.stuartsierra.component :as component]))

(let [redis-conn (componet/start (omega-red.redis/create {:host "127.0.0.1" :port 6379}))]
    (is (= 0 (proto/execute redis-conn [:exists "test.some.key"]))) ; true
    (is (= "OK" (proto/execute redis-conn [:set "test.some.key" "foo"]))) ; true
    (is (= 1 (proto/execute redis-conn [:exists "test.some.key"]))) ; true
    (is (= "foo" (proto/execute redis-conn [:get "test.some.key"]))) ; true
    (is (= 1 (proto/execute redis-conn [:del "test.some.key"]))) ; true
    (component/stop red)
    (is (nil? (proto/execute redis-conn [:get "test.some.key"])))) ; true

;; pipeline execution
(is (= [nil "OK" "oh ok" 1]
       (proto/execute-pipeline redis-conn
                               [[:get "test.some.key.pipe"]
                                [:set "test.some.key.pipe" "oh ok"]
                                [:get "test.some.key.pipe"]
                                [:del "test.some.key.pipe"]]))) ; true

```

## Change log


- 1.0.0-SNAPSHOT - **Breaking change!** Changes signature of `execute` to accept a vector, and `execute-pipeline` to accept a vector of vectors. This makes it easier to work with variadic Redis commands (`hmset` etc) and compose commands
- 0.1.0- 2019/10/23 - Initial Public Offering

# Roadmap

- [ ] explicit connection pool component with its own lifecycle
- [ ] *maybe* move off Carmine and use Jedis or Lettuce directly (because of the point above)


# Authors

<sup>In alphabetical order</sup>

- [Afonso Tsukamoto](https://github.com/AfonsoTsukamoto)
- [Łukasz Korecki](https://github.com/lukaszkorecki)
- [Marketa Adamova](https://github.com/MarketaAdamova)
