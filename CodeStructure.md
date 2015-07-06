**This is the copy version of**
http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:atom_framework:docs

# Question: How can I structure my code? #

http://hub.jmonkeyengine.org/forum/topic/how-to-structure-the-game-code/
## The pattern ##

Read the explain about some design patterns in Design design

This queston evolve several patterns like Singleton, Manager, Factory and related directly to the Central idiom of Atom's framework!
The central

Now talking about the long “getManager().getSubManager().getItsManagedObject().doSomeThing()” Consider asking your self this when you code:

  * Can singleton help?
  * Can hierarchy actually help?
  * How secure is your application, is it a game or anything else?
  * How perfomance sensitive your game is?

For this particular example, related to “Law of Demeter” that @zanval88 mentioned is about “communicating range scope” and “hiding information”, appear as “encapsulation” and “reference” in Java.

  1. If all your Manager classes is public, and referenced every where chances that they can be Singleton (read more about singleton for its downside).
  1. If not, you should nest your Manager and its ManagedObject it arbitrary order and way. Consider this for a game, it’s almost always not necessary!
    1. When you have your Managers and Object form a “tree” and obey a “cycle” or “protocol” or “common pattern”, may be contract them with an Interface or force them to extend the same base. [DID this in my Atom framework!](I.md)
    1. Actually while a lower (near the core) Manager are more powerful and can capture the whole process, the higher level Manager (extended Manager or sub manager) know better about detail and specific aspects. The split should be carefully thought first to avoid un necessary burden of too much Managers. Personally, I usually think about an interesting example about a society with many cops and few citizens to imagine about this balance.
  1. For part of the game, when security are most important but not the performance, or for external services. Long indirect call is the most appropriate method, because of the availability (something should be injected, lazy loaded…) and security (some protol given, real info is hidden from our view,…etc). Without a check of availability, chance that you have NPE all the time!
  1. For part of the game that need performance. I said “a big deal of performance” you should try “singleton” for final class (and bare with its other issuses) or bet in some other flatten type of reference (little bit worse performance – one level of indirect) should also be concerned:
> > array or list with an index. map via Class(Type) apear as Template in java …
> > > ex: StateManager.getState(ClassName.class).doSomething();
> > > or even more abstract somekind of Central.get(“StateManager”).do(“ItsFunctionName”)!!! I do this in groovy and for Java 8 dynamic invoking

> > evolve your hierarchy with EntitySystem paradigm

# Atom convention about code structure #
_Common video gamedev has patterns!_

I have a strong conceptual POV about video game, which affected by cinematography a lot. Because English is not my native language I can misunderstood the real meaning of the noun but I’ve tried to find the right words in decade.

This one is mine, maybe (only me but noone else) :p :

## So consider this 5 level of separation: ##

  1. Main : The main entry, have everything only relate to this single game, single application. Also game specific Configs should be here
  1. Core : The shared part can be used in almost every application share the same base
  1. Stage : The ‘Stage’ is the base of entities, activites and events… It’s not nessesary care about the gameplay but the World, Camera, Light and Effects, Cinematic. Stage contain most of the underlying logic, and the actors.
  1. GamePlay: The part care about the routine of the game, the player, characters, stories, items, gameactions, techtree… it’s make the game look more like a game than an normal software or a movie. Gameplay contain most of the interactive part.
  1. State : Even your game routine can be modeled by something else not States, I introduced State to be more friendly with JME3′s AppState concept. I ultilized it and leveraged it, and you should also.

## Others optional: ##

  * Network : For network game, blending between state, sub-routine and arbitrary events is difficult and may require other kind of paradigm, that’s why is not in Stage, but elsewhere outside.
  * Services: If your game use external services such as Web or IAP or something like that.
  * UI: UI stand for user interface, almost everygame have user interfaces but not all, so it’s also optional
  * Script: For scripting, in my application, I embed Groovy everywhere, but I also preseved a place to hold “tranditional” run-time script that only be awared and executed when the game is already running.
  * Model: If you are in a company or from a strong “standard lized” Java workflow, it’s nearly you’ll come up with some Bean like this, but it’s kind of for normal software not a game.
  * Generated: Also if you have to embed some XML or some generated sources
  * DB: Of course Database and persistent solution can be here( if not better be in the Service section)

…

The directory : **src**<pre>
my.game.name<br>
main<br>
core<br>
state<br>
gameplay<br>
(*)network<br>
(*)services<br>
(*)ui<br>
(*)others<br>
</pre>
More detailed, You can find a better example here in my game examples: atomixtuts

# Atom framework's Manager #


> Read about the Manager idea here atomcore

Atom also have a lot of Managers. Here suggestion for implemented one!, you can call it a convention because it's not forced on you to gain more flexibility.

Manager can has SubManagers, as a List or a Map (Hierarchy or not is not forced)

Manager can be extended or Singleton or DefaultInstance (can be getDefault() but not a singleton) here and there. Manager all “attend” in a “cycle” but not “obey”. They can implement ICycle to mark they executor as Obey stricly to the Cycle.

you can do Main.getManager(Class) if your Main support it, or doing the long reference getManager().getSubManager().

You can see it as a hybrid or “no contract yet” – “not stricted to” way of implementing to get “best of both world” out of:

  1. singleton vs default vs linked instance
  1. hierarchy vs flatten component base
  1. cycle attend vs a random routine.

In this implementation, I also try to have a good balance between flexibility and usefulness, clearance and performance. In the near future, when my framework is proved to be statable and useful, I will release it fully.
Configurations
More coding convention