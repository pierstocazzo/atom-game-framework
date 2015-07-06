# Architecture and components #

Here are its architecture and components.

## Atom Core Libraries ##

  * Atom Core : addition to JME3 core packages.
    * Cross game-genre elements: stage, cycle, entity, logic, trigger, event, config;
    * Managers: Advanced assets manager, dependecy injection, factory, scripting, basic DB..;
    * Common case: Common state, common scenerio, common UIs…
    * More Buzz? AtomCore documentation
  * Atom Scripting Base technology for use Groovy (potentional Scala, Jython..) as JME game scripting language…
    * Provide Test bed enviroment, thread-safe and intelligent Groovy swing component to extend the SDK in seconds
    * More Buzz?atomscripting
  * Atom AI : a "framework" to bring AI to jME3 game (also means real-time application)! But it's awesome in its own way.
    * Focus in AI coding, creating, testing, simulating, profiling in 3d enviroments.
    * Come with tools as SDK plugins!
    * Check Atom AI wiki for more buzz
## Ingame editor facilities and kits ##
  * AtomEditor:
  * Atom2DEditor:
  * Code gen: a "framework" that intend to become the base technologies for all generation related techs in the Atom framework. codegen
    * Focus in provide general and abstract way to modeling|design game|real-time app concept and object, source codes.
    * Its first attempt to become a GLSL, Groovy generator, then become a Logic, source code generator…
    * Come with tools as SDK plugins!
  * City gen: a "framework" at first try to be a city generator, then grow up to be fullfill every geometric generating operations in 3D.
    * Focus in "Level" generator with 3d models, blueprint and geometric shapes, such as dugeon, city, rivers, mountain, trees…
    * Can corporate with Code gen and other geometric libs to become a generative 3D editor…
    * Come with tools as SDK plugins!

## Atom SDK ##

  * Atom SDK : Expansion for current functions and features of the jME SDK, more intuitive more user friendly and suchs
    * Full List? atomsdk
  * Teehee Composer : Act as the base editor for video, cinematic, audio, effects, facial composer… anything require time-base keyframed or unlinear editing like sequences.
    * An almighty composer, think about 3DSMax or Adobe After Effect in 3D
    * Come with a lot of tools for the SDK : teehee
      * Cinematic composer
      * Dialogue composer
      * Effect composer
      * Particle composer
      * Animation composer
  * RPG Creator : Despite of its name, its not just for Role playing game!
    * Provide functions to create| test| config basic game with these key elements : characters| stories| skills| items| modes| regions… almost every game genre has them embeded partly ( cross game genre)
    * Come with tools as SDK plugins! rpgcreator
  * Nextgen Tools
    * Facial tools : Think FaceFX for JME :p facial
    * Character customization management tools : Smart way to organize and corporate your assets, config, database and code for CC cc
    * Vitural reality tools : Toolset for corporate vitural reality artifact in your app vr
    * MMORPG tools : Toolset for creating of a MMORPG game's component and all its management structure. Epic! mmorpgtools
    * Human Simulation tools: Think advanced locomotion and AI (like Mechanim of Unity) multiply 10. In fact, it's quite similar with tool from Autodesk that simulations social beheviours of human characters. Epic! humansim
## AtomEx ##
  * Atom Ex : addition to Atom framework which make its much more modulizable, extensible and enterprise ready. Distributed computing, web based, database… much more.
> > More Buzz? AtomEx documentation