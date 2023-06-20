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

/**
 * this package contains all {@link de.hechler.patrick.games.squareconqerer.world.World Worlds}:
 * <ul>
 * <li>{@link de.hechler.patrick.games.squareconqerer.world.RootWorld RootWorld}</li>
 * <li>{@link de.hechler.patrick.games.squareconqerer.world.RootWorld.Builder RootWorld.Builder}</li>
 * <li>{@link de.hechler.patrick.games.squareconqerer.world.UserWorld UserWorld}</li>
 * </ul>
 * additionally there are the following classes, which operate on worlds:
 * <ul>
 * <li>{@link de.hechler.patrick.games.squareconqerer.world.OpenWorld OpenWorld}
 * <ul>
 * <li>this class can be used to send {@link de.hechler.patrick.games.squareconqerer.world.World Worlds} over a
 * {@link de.hechler.patrick.games.squareconqerer.connect.Connection Connection}</li>
 * </ul>
 * </li>
 * <li>{@link de.hechler.patrick.games.squareconqerer.world.PageWorld PageWorld}
 * <ul>
 * <li>this class can be used to generate a {@link de.hechler.patrick.games.squareconqerer.world.World World} from a
 * {@link de.hechler.patrick.games.squareconqerer.addons.pages.SCPage SCPage}</li>
 * </ul>
 * </li>
 * </ul>
 */
package de.hechler.patrick.games.squareconqerer.world;
