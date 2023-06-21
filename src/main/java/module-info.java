// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.

/**
 * this is the Square Conquerer module.
 * <p>
 * this module contains the base game.
 * 
 * @author Patrick Hechler
 */
module de.hechler.patrick.games.squareconqerer {
	
	requires transitive java.desktop;
	requires jdk.incubator.concurrent;
	
	exports de.hechler.patrick.games.sc;
	exports de.hechler.patrick.games.sc.addons.addable;
	exports de.hechler.patrick.games.sc.addons;
	exports de.hechler.patrick.games.sc.connect;
	exports de.hechler.patrick.games.sc.error;
	exports de.hechler.patrick.games.sc.turn;
	exports de.hechler.patrick.games.sc.ui.pages;
	exports de.hechler.patrick.games.sc.ui.players;
	exports de.hechler.patrick.games.sc.values;
	exports de.hechler.patrick.games.sc.values.spec;
	exports de.hechler.patrick.games.sc.world;
	exports de.hechler.patrick.games.sc.world.entity;
	exports de.hechler.patrick.games.sc.world.ground;
	exports de.hechler.patrick.games.sc.world.init;
	exports de.hechler.patrick.games.sc.world.resource;
	exports de.hechler.patrick.games.sc.world.tile;
	exports de.hechler.patrick.utils.interfaces;
	exports de.hechler.patrick.utils.objects;
	
}
