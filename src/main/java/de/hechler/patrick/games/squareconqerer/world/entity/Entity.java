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
package de.hechler.patrick.games.squareconqerer.world.entity;

import java.util.Collections;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.SCAddon;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

/**
 * this is the superclass of {@link Unit} and {@link Building}
 * 
 * @author Patrick Hechler
 */
public sealed interface Entity extends ImageableObj permits Unit, Building {
	
	/**
	 * returns the current x coordinate of this entity
	 * 
	 * @return the current x coordinate of this entity
	 */
	int x();
	
	/**
	 * returns the current y coordinate of this entity
	 * 
	 * @return the current y coordinate of this entity
	 */
	int y();
	
	/**
	 * returns the current owner of this entity
	 * 
	 * @return the current owner of this entity
	 */
	User owner();
	
	/**
	 * returns the current lives of this entity
	 * 
	 * @return the current lives of this entity
	 */
	int lives();
	
	/**
	 * returns the maximum lives of this entity
	 * 
	 * @return the maximum lives of this entity
	 */
	int maxLives();
	
	/**
	 * returns the current view range of this entity
	 * 
	 * @return the current view range of this entity
	 */
	int viewRange();
	
	/**
	 * returns a copy of this current entity
	 * 
	 * @return a copy of this current entity
	 */
	Entity copy();
	
	/**
	 * returns an empty list
	 * <p>
	 * the returned list is sorted with the {@link Unit#compareTo(Unit)}
	 * 
	 * @return an empty list
	 */
	default List<Unit> units() {
		return Collections.emptyList();
	}
	
	/**
	 * returns the localized name of this entity
	 * 
	 * @return the localized name of this entity
	 */
	String localName();
	
	/**
	 * returns the global ordinal value of this entity
	 * 
	 * @return the global ordinal value of this entity
	 */
	@Override
	default int ordinal() {
		return switch (this) {
		case @SuppressWarnings("preview") Unit u -> SCAddon.addon(u).oridinalOffsetUnit() + addonLocalOrdinal();
		case @SuppressWarnings("preview") Building b -> SCAddon.addon(b).oridinalOffsetBuilding() + addonLocalOrdinal();
		};
	}
	
	/**
	 * returns the addon local ordinal value of this entity
	 * 
	 * @return the addon local ordinal value of this entity
	 */
	int addonLocalOrdinal();
	
}
