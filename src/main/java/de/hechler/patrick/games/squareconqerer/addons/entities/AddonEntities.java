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
package de.hechler.patrick.games.squareconqerer.addons.entities;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;

public interface AddonEntities {
	
	/**
	 * sends the given unit
	 * <p>
	 * this method must send all needed data over the connection<br>
	 * this method must be compatible with the {@code recieveUnit(...)} method
	 * 
	 * @param conn the connection on which the unit should be send
	 * @param u    the unit to be send
	 * 
	 * @throws IOException if the connection throws an {@link IOException}
	 */
	void sendUnit(Connection conn, Unit u) throws IOException;
	
	/**
	 * receives a unit over the given {@link Connection}
	 * 
	 * @param conn the connection on which the unit should be read
	 * 
	 * @return the unit which was send over the connection
	 * 
	 * @throws IOException              if the connection throws an
	 *                                  {@link IOException}
	 * @throws StreamCorruptedException if the connection retrieved invalid data
	 */
	Unit recieveUnit(Connection conn, User usr) throws IOException, StreamCorruptedException;
	
	/**
	 * sends the given building
	 * <p>
	 * this method must send all needed data over the connection<br>
	 * this method must be compatible with the {@code recieveBuild(...)} method
	 * 
	 * @param conn the connection on which the unit should be send
	 * @param b    the building to be send
	 * 
	 * @throws IOException if the connection throws an {@link IOException}
	 */
	void sendBuild(Connection conn, Building b) throws IOException;
	
	/**
	 * receives a building over the given {@link Connection}
	 * 
	 * @param conn the connection on which the building should be read
	 * 
	 * @return the building which was send over the connection
	 * 
	 * @throws IOException              if the connection throws an
	 *                                  {@link IOException}
	 * @throws StreamCorruptedException if the connection retrieved invalid data
	 */
	Building recieveBuild(Connection conn, User usr) throws IOException, StreamCorruptedException;
	
	/**
	 * returns a map containing <b>all</b> entity classes added by this addon.
	 * <p>
	 * note that all classes means all classes, subclasses are also needed to be in
	 * this map, even if their superclass is already in the map.
	 * <p>
	 * the mapping keys are the classes and the values are the names<br>
	 * the name can be chosen freely, it does not needs to be the
	 * {@link Class#getName()} or something else
	 * <p>
	 * the map is allowed to have multiple classes with the same name<br>
	 * if there are multiple maps to the same key, they are considered to be the
	 * same type.<br>
	 * the map is not allowed to have unit classes with the same name as a building
	 * class (classes are only allowed to have the same name, if they are both unit
	 * or both building)
	 * 
	 * @return a map containing all entity classes created by this addon as keys and
	 *         their names as values
	 */
	Map<Class<? extends Entity>, String> entityClassses();
	
	/**
	 * returns the traits of the entity type with the given name
	 * 
	 * @param clsName the name of the entity class
	 * 
	 * @return the traits of the entity
	 */
	Map<String, EntityTrait> traits(String clsName);
	
	/**
	 * returns the traits of the entity class
	 * 
	 * @param cls the entity class
	 * 
	 * @return the traits of the entity
	 */
	default Map<String, EntityTrait> traits(Class<? extends Entity> cls) { return traits(entityClassses().get(cls)); }
	
	/**
	 * creates and returns a new entity with the given traits
	 * <p>
	 * executing
	 * <code>({@link #entityClassses() entityClassses}().{@link Map#get(Object) get}({@link #createEntity(String, User, Map, int, int) createEntity}(clsName, traits)).{@link String#equals(Object) equals}(clsName))</code>
	 * has to result in <code>true</code> (and unused object allocation)
	 * 
	 * @param <E>     the entity type
	 * @param clsName the class name of the entity to create (see
	 *                {@link #entityClassses()})
	 * @param usr     the {@link Entity#owner() owner}
	 * @param traits  the traits of the entity (see {@link #traits(String)})
	 * 				
	 * @return the newly created entity with the given traits
	 */
	<E extends Entity> E createEntity(String clsName, User usr, Map<String, EntityTraitWithVal> traits, int x, int y);
	
}
