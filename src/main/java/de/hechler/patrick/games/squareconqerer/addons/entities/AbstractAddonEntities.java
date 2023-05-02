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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.stuff.IntMap;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

/**
 * this class can be used to help implementing the {@link AddonEntities} interface
 * 
 * @author Patrick Hechler
 */
public abstract class AbstractAddonEntities implements AddonEntities {
	
	private final Map<Class<? extends Entity>, String> entitiCls;
	
	/**
	 * creates a new {@link AbstractAddonEntities} instance
	 * 
	 * @param entitiCls the entity classes with their names (see {@link #entityClassses()})
	 */
	public AbstractAddonEntities(Map<Class<? extends Entity>, String> entitiCls) {
		this.entitiCls = Collections.unmodifiableMap(new HashMap<>(entitiCls));
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this method sends the units coordinates, lives, carry amount and resource<br>
	 * the rest has to be send by the {@link #finishSendUnit(Connection, Unit)} method
	 */
	@Override
	public void sendUnit(Connection conn, Unit u) throws IOException {
		conn.writeInt(u.x());
		conn.writeInt(u.y());
		conn.writeInt(u.lives());
		int ca = u.carryAmount();
		conn.writeInt(ca);
		if (ca != 0) {
			OpenWorld.writeRes(conn, u.carryRes());
		}
		finishSendUnit(conn, u);
	}
	
	/**
	 * sends the given units special data<br>
	 * the ({@link Entity#x() x}|{@link Entity#y() y}) coordinates, {@link Entity#lives() lives}, {@link Unit#carryAmount() carryAmount} and {@link Unit#carryRes()
	 * carryRes} are already send when this method is invoked by {@link #sendUnit(Connection, Unit)}
	 * 
	 * @param conn the connection
	 * @param u    the unit to be send
	 * 
	 * @throws IOException if an IO error occurs
	 */
	protected abstract void finishSendUnit(Connection conn, Unit u) throws IOException;
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this method receives the units coordinates, lives, carry amount and resource<br>
	 * the rest has to be received by the {@link #finishRecieveUnit(Connection, User, int, int, int, int, Resource)} method
	 */
	@Override
	public Unit recieveUnit(Connection conn, User usr) throws IOException {
		int      x     = conn.readPos();
		int      y     = conn.readPos();
		int      lives = conn.readStrictPos();
		int      ca    = conn.readPos();
		Resource res   = null;
		if (ca != 0) {
			res = RemoteWorld.readRes(conn);
		}
		return finishRecieveUnit(conn, usr, x, y, lives, ca, res);
	}
	
	/**
	 * receives the given units special data<br>
	 * the ({@link Entity#x() x}|{@link Entity#y() y}) coordinates, {@link Entity#lives() lives}, {@link Unit#carryAmount() carryAmount} and {@link Unit#carryRes()
	 * carryRes} are already send when this method is invoked by {@link #sendUnit(Connection, Unit)}
	 * 
	 * @param conn  the connection
	 * @param usr   the {@link Entity#owner() owner} of the unit
	 * @param x     the {@link Entity#x() x} coordinate of the unit
	 * @param y     the {@link Entity#y() y} coordinate of the unit
	 * @param lives the {@link Entity#lives() lives} of the unit
	 * @param ca    the {@link Unit#carryAmount() carry Amount} of the unit
	 * @param res   the {@link Unit#carryRes() carry Resource} of the unit
	 * 
	 * @return the received unit
	 * 
	 * @throws IOException if an IO error occurs
	 */
	protected abstract Unit finishRecieveUnit(Connection conn, User usr, int x, int y, int lives, int ca, Resource res) throws IOException;
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this method sends the ({@link Entity#x() x}|{@link Entity#y() y}) coordinates, {@link Entity#lives() lives}, {@link Building#isFinishedBuild() build finished}
	 * status and if {@link Building#isFinishedBuild()} is <code>false</code> this method also sends the {@link Building#remainingBuildTurns() remaining Build-Turns}
	 * and {@link Building#neededResources() needed Resources}<br>
	 * everything else has to be send by the {@link #finishSendBuild(Connection, Building)} method
	 */
	@Override
	public void sendBuild(Connection conn, Building b) throws IOException {
		conn.writeInt(b.x());
		conn.writeInt(b.y());
		conn.writeInt(b.lives());
		boolean fb = b.isFinishedBuild();
		conn.writeByte(fb ? 1 : 0);
		if (!fb) {
			conn.writeInt(b.remainingBuildTurns());
			IntMap<ProducableResourceType> res = b.neededResources();
			if (res == null) {
				conn.writeInt(0);
			} else {
				int[] arr = res.array();
				conn.writeInt(arr.length);
				for (int i = 0; i < arr.length; i++) {
					conn.writeInt(arr[i]);
				}
			}
		}
		finishSendBuild(conn, b);
	}
	
	/**
	 * sends the buildings special data<br>
	 * when this method is invoked, the ({@link Entity#x() x}|{@link Entity#y() y}) coordinates, {@link Entity#lives() lives}, {@link Building#isFinishedBuild()
	 * build finished} status and if <code>{@link Building#isFinishedBuild() b.isFinishedBuild()} == false</code> also the {@link Building#remainingBuildTurns()
	 * remaining Build-Turns} and {@link Building#neededResources() needed Resources} are already send
	 * 
	 * @param conn the connection
	 * @param b    the building to be send
	 * 
	 * @throws IOException if an IO error occurs
	 */
	protected abstract void finishSendBuild(Connection conn, Building b) throws IOException;
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this method only receives the ({@link Entity#x() x}|{@link Entity#y() y}) coordinates, {@link Entity#lives() lives}, {@link Building#isFinishedBuild() build
	 * finished} status and if {@link Building#isFinishedBuild()} is <code>false</code> this method also the {@link Building#remainingBuildTurns() remaining
	 * Build-Turns} and {@link Building#neededResources() needed Resources}<br>
	 * everything else has to be received by the {@link #finishSendBuild(Connection, Building)} method
	 */
	@Override
	public Building recieveBuild(Connection conn, User usr) throws IOException {
		int                            x          = conn.readPos();
		int                            y          = conn.readPos();
		int                            lives      = conn.readStrictPos();
		boolean                        fb         = conn.readByte(0, 1) != 0;
		int                            buildTurns = 0;
		IntMap<ProducableResourceType> res        = null;
		if (!fb) {
			buildTurns = conn.readPos();
			if (conn.readInt(0, ProducableResourceType.count()) != 0) {
				res = IntMap.createEnumIntMap(ProducableResourceType.class);
				int[] arr = res.array();
				for (int i = 0; i < arr.length; i++) {
					arr[i] = conn.readPos();
				}
			}
		}
		return finishRecieveBuild(conn, usr, x, y, lives, fb, buildTurns, res);
	}
	
	/**
	 * receives the buildings special data
	 * <p>
	 * this method does not need to receive the ({@link Entity#x() x}|{@link Entity#y() y}) coordinates, {@link Entity#lives() lives},
	 * {@link Building#isFinishedBuild() build finished} status and if {@link Building#isFinishedBuild()} is <code>false</code> this method also the
	 * {@link Building#remainingBuildTurns() remaining Build-Turns} and {@link Building#neededResources() needed Resources}
	 * 
	 * @param conn       the connection
	 * @param usr        the {@link Entity#owner() owner} of the building
	 * @param x          the {@link Entity#x() x} coordinate of the building
	 * @param y          the {@link Entity#y() y} coordinate of the building
	 * @param lives      the {@link Entity#lives() lives} of the building
	 * @param fb         the {@link Building#isFinishedBuild() finished build} status of the building
	 * @param buildTurns the remaining {@link Building#remainingBuildTurns() build turns} of the building
	 * @param buildRes   the needed {@link Building#neededResources() build resources} of the building
	 * 
	 * @return the received building
	 * 
	 * @throws IOException if an IO error occurs
	 */
	protected abstract Building finishRecieveBuild(Connection conn, User usr, int x, int y, int lives, boolean fb, int buildTurns,
			IntMap<ProducableResourceType> buildRes) throws IOException;
	
	/** {@inheritDoc} */
	@Override
	public Map<Class<? extends Entity>, String> entityClassses() { return this.entitiCls; }
	
}
