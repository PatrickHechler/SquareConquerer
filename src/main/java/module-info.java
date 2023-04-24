module de.hechler.patrick.games.squareconqerer {
	
	requires transitive java.desktop;
	
	exports de.hechler.patrick.games.squareconqerer;
	exports de.hechler.patrick.games.squareconqerer.addons;
	exports de.hechler.patrick.games.squareconqerer.addons.entities;
	exports de.hechler.patrick.games.squareconqerer.addons.records;
	exports de.hechler.patrick.games.squareconqerer.connect;
	exports de.hechler.patrick.games.squareconqerer.exceptions;
	exports de.hechler.patrick.games.squareconqerer.exceptions.enums;
	exports de.hechler.patrick.games.squareconqerer.interfaces;
	exports de.hechler.patrick.games.squareconqerer.objects;
	exports de.hechler.patrick.games.squareconqerer.ui;
	exports de.hechler.patrick.games.squareconqerer.world;
	exports de.hechler.patrick.games.squareconqerer.world.entity;
	exports de.hechler.patrick.games.squareconqerer.world.enums;
	exports de.hechler.patrick.games.squareconqerer.world.placer;
	exports de.hechler.patrick.games.squareconqerer.world.resource;
	exports de.hechler.patrick.games.squareconqerer.world.stuff;
	exports de.hechler.patrick.games.squareconqerer.world.tile;
	exports de.hechler.patrick.games.squareconqerer.world.turn;
	
	uses de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon;
	
	provides de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon with de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;
	
}
