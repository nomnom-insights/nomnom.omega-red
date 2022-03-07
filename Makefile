# Assumes shared configs and secrets!

fmt:
	cljstyle fix .

lint:
	clj-kondo --lint .


deploy:
	lein deploy clojars
