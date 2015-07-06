# AtomCore #

**AtomCore is for 3D gamedev based in OpenGL and JME3**.

Its the foundation and fundamental facilities of Atom framework. It functionalities are focused in making 3D games! Its Codes and CodeStructure are shown here!

Some Design are made to help making games in Atom easy, fast and also best performance ability in various platforms.

# Packages #
**sg.atom.core**    	Core elements of the framework.

**sg.atom.entity**	Concepts and Facilities to build up Game object. (Beta)

**sg.atom.fx**		Concepts and Facilities to create and manage animations and effects

**sg.atom.gameplay**	Concepts and facilities for games (cross-genre)

**sg.atom.logic**		Basic block for building game from a programming language via formal system.

**sg.atom.net**		Concepts and interfaces for connectivity and communication via networks

**sg.atom.stage**		Concepts and facilities for cinematography like games

**sg.atom.state**		Additional for JME3 app state (bridge between to systems) and some common states for a common games

**sg.atom.ui**		General GUI for user interaction and styling in hierachy (non-strict) elements

**sg.atom.utils**		Collections of userful utilities and datastructures, algorimths here and there.

**sg.atom.world**		Concepts and interfaces to build and manage the game world and enviroment

## Detailed codes & packages ##

<pre>
Packages<br>
*sg.atom.core *   	Core elements of the framework.<br>
actor			Concepts & Facilities for simple in JVM actor framework.<br>
annotations 	Annotations to setting up elements in java code. (Same in every packages!)<br>
assets 			Facilities to import / export assets from JME3 pipeline<br>
bean			Facilities to use Java bean in Atom context with mapping and binding.<br>
config			Facilities to use Configs in Atom, with the help of Common Configuration<br>
context			Bridge concepts help to bring entities from one enviroment to others. Cross platform<br>
event			Concepts & Facilities for simple in JVM actor / messaging framework.<br>
execution		Facilities for execution, with help of Common lang and Guava<br>
lifecycle		Concepts for game (and real time application) cycle<br>
monitor			Facilities to monitor your game and application<br>
timing			Concepts & Facilities for real time application<br>
<br>
<br>
*sg.atom.entity	*	Concepts and Facilities to build up Game object. (Beta)<br>
<br>
*sg.atom.fx*			Concepts and Facilities to create and manage animations and effects<br>
anim			Concepts for animation<br>
automatic 		Automatic driven for animation<br>
constraint		Other way to declare relationship between entities and activities<br>
filters			Additions to JME3 filters<br>
functional		Functional flavours for effects<br>
particles 		Concepts to build bigger system from smaller part<br>
sprite			Concepts for cross dimensional elements<br>
timeline		Enhance of timming framework<br>
transition		Transition between stateful objects<br>
tween			Object interpolations.<br>
<br>
*sg.atom.gameplay*	Concepts and facilities for games (cross-genre)<br>
action			Concepts and interfaces for action in games<br>
controls		Additions for JME3 character controls<br>
league			Leagues  group and tournament of players<br>
managers		Manager of leagues  group and tournament of players<br>
player			Player and their data<br>
replay			To record the game activities<br>
score			To recored the game results<br>
<br>
*sg.atom.logic*		Basic block for building game from a programming language via formal system.<br>
<br>
*sg.atom.net*			Concepts and interfaces for connectivity and communication via networks<br>
<br>
*sg.atom.stage*		Concepts and facilities for cinematography like games<br>
actor			Bridge from entities to actor framework!<br>
cine			Sostiphicate cinematic framework for complex video games<br>
helpers			"Inplace" controls which know about Stage. Bridge from JME3 Controls concepts<br>
input			Sophisticate high level input system use for develop and test game<br>
select			Facilities for selecting (from input) an on screen spatial or entities<br>
sound			Additional facilities to JME3 sound system<br>
sync			Additional facilities to syncing between multi thread progress<br>
<br>
*sg.atom.state*		Additional for JME3 app state (bridge between to systems) and some common states for a common games<br>
<br>
*sg.atom.ui*			General GUI for user interaction and styling in hierachy (non-strict) elements<br>
<br>
*sg.atom.utils*		Collections of userful utilities and datastructures, algorimths here and there.<br>
Note: This package contains a lot of stuff borrowed from libraries and should be clean up. Do not rely too much in this library!<br>
<br>
*sg.atom.world*		Concepts and interfaces to build and manage the game world and enviroment<br>
gen				Generate the world from data<br>
geometry		Maths for geometries<br>
lod				Level of detail framework provides a lot of methods to optimize scene and geometry.<br>
material		Additions for JME3 material system<br>
physics			Additions for JME3 physic system<br>
rendering		Additions for JME3 render system<br>
terrain			Additions for JME3 terrain system<br>
visibility		Additions for JME3 cull and partition system<br>
</pre>

# How to use #