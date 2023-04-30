//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.

import de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon;
import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;

/**
 * this is the Square Conquerer module.
 * <p>
 * this module contains the base game.
 * 
 * @author Patrick Hechler
 *
 * @uses SquareConquererAddon the {@link SquareConquererAddon} class should be used to add stuff to the game
 * @provides SquareConquererAddon with the base game addon {@link TheGameAddon}
 */
module de.hechler.patrick.games.squareconqerer {
	
	requires transitive java.desktop;
	requires jdk.incubator.concurrent;
	
	exports de.hechler.patrick.games.squareconqerer;
	exports de.hechler.patrick.games.squareconqerer.addons;
	exports de.hechler.patrick.games.squareconqerer.addons.entities;
	exports de.hechler.patrick.games.squareconqerer.addons.pages;
	exports de.hechler.patrick.games.squareconqerer.connect;
	exports de.hechler.patrick.games.squareconqerer.exceptions;
	exports de.hechler.patrick.games.squareconqerer.exceptions.enums;
	exports de.hechler.patrick.games.squareconqerer.interfaces;
	exports de.hechler.patrick.games.squareconqerer.stuff;
	exports de.hechler.patrick.games.squareconqerer.ui;
	exports de.hechler.patrick.games.squareconqerer.world;
	exports de.hechler.patrick.games.squareconqerer.world.entity;
	exports de.hechler.patrick.games.squareconqerer.world.enums;
	exports de.hechler.patrick.games.squareconqerer.world.placer;
	exports de.hechler.patrick.games.squareconqerer.world.resource;
	exports de.hechler.patrick.games.squareconqerer.world.stuff;
	exports de.hechler.patrick.games.squareconqerer.world.tile;
	exports de.hechler.patrick.games.squareconqerer.world.turn;
	
	uses SquareConquererAddon;
	
	provides SquareConquererAddon with TheGameAddon;
	
}
