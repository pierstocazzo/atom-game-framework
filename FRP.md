# Introduction #

http://en.wikipedia.org/wiki/Reactive_programming
http://en.wikipedia.org/wiki/Functional_reactive_programming

Elm is a new rising GUI framework that use FRM intensively

http://en.wikipedia.org/wiki/Elm_%28programming_language%29

# So how FRM is implemented in Atom framework ? #

FRM in Atom is implemented after i have spent sometime reviewed some java reactive opensource frameworks:
  * reactive4j
  * reactor
  * rxjava

With the experience working on Dataflow before, I've decided to work on a fork which stand under the umbrella of Guava and Guice. May be in the future when reactor get mature enough, Atom will move completely under reactor or akka.

Detail design papers here:

Some differencies in approaches can be distinguish by below topics
  * Schedulers
  * Event mechanisms
  * Selector
  * Functional flavours
  * Observer over iterators

In my implementation (AtomReactor):
  * Schedulers: are the same as those who use by AtomTasks, tied with OpenGL thread constrainst.
  * Event: can also be generic or can be generated to be hard routine ( zero flow ; read: Tasks )
  * Selector: can be descripted with Rule, and validated by Predicate chains. The matching progress ultilize ReteOO or PHREAKY as in Drools.
  * Functional flavours:
    * omits all Action1,2,3.. or Function1,2,3 ... as standard and design choices of Guava. Instead it used ImmutableList and Optional for arrays of parameters. This slightly cause performance drop but return huge flexibility (explained in the design paper above) and much more clean design.
    * 
  * Use obsever with monitoring flavor, aka stream cardinality (read Flow)
Additional implementation notes:
  * Use concurrent NIO buffer (from Netty) as Reactor
  * Use ringbuffer (from Disruptor) as Reactor and RxJava
  * Use shaded tree to prun unused (abandon) stream-line. This is kind of advanced gabage collector helper.