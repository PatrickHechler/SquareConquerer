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
package de.hechler.patrick.games.squareconqerer.world.placer;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.SCAddon;
import de.hechler.patrick.games.squareconqerer.addons.defaults.AddonDefaults;
import de.hechler.patrick.games.squareconqerer.addons.entities.AddonEntities;
import de.hechler.patrick.games.squareconqerer.addons.entities.EntityTrait;
import de.hechler.patrick.games.squareconqerer.addons.entities.EntityTraitWithVal;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.stuff.ACORNRandom;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;


/**
 * this is the default implementation of the {@link UserPlacer} interface
 * 
 * @author Patrick Hechler
 */
public class DefaultUserPlacer implements UserPlacer {
	
	private static final String THERE_IS_NO_CLASS_NAME                                      = Messages.getString("DefaultUserPlacer.no-class-name");         //$NON-NLS-1$
	private static final String NO_ADDON                                                    = Messages.getString("DefaultUserPlacer.no-addon");              //$NON-NLS-1$
	private static final String WORLD_IS_FULL_NOT_ENUGH_PLACE_FOR_ALL_PLAYERS               = Messages.getString("DefaultUserPlacer.world-is-full");         //$NON-NLS-1$
	private static final String GIVEN_TRAITS_DO_NOT_MATCH_THE_NEEDED_TRAITS                 = Messages.getString("DefaultUserPlacer.different-traits");      //$NON-NLS-1$
	private static final String TRIED_8_INVALID_POSITIONS_THERE_IS_A_FREE_POSITION_CONTINUE = Messages.getString("DefaultUserPlacer.bad-random");            //$NON-NLS-1$
	private static final String ENTITY_TRAITS_HAS_DIFFERENT_SIZE_THAN_ADDON_SAYS            = Messages.getString("DefaultUserPlacer.different-trait-sizes"); //$NON-NLS-1$
	
	private final Map<String, List<Map<String, EntityTraitWithVal>>> entityAmounts;
	
	private DefaultUserPlacer(Map<String, List<Map<String, EntityTraitWithVal>>> entityAmounts) {
		this.entityAmounts = entityAmounts;
	}
	
	/**
	 * create a new user placer with the {@link AddonDefaults#startEntities() default start entities} of all {@link SCAddon#addons() addons}
	 * 
	 * @return the user placer with default values
	 */
	public static DefaultUserPlacer createWithDefaults() {
		Map<String, List<Map<String, EntityTraitWithVal>>> entityAmounts = new HashMap<>();
		for (SCAddon addon : SCAddon.addons()) {
			Map<String, Collection<Map<String, EntityTraitWithVal>>> se = addon.defaults().startEntities();
			for (Entry<String, Collection<Map<String, EntityTraitWithVal>>> entry : se.entrySet()) {
				String                                      clsName = entry.getKey();
				Collection<Map<String, EntityTraitWithVal>> es      = entry.getValue();
				List<Map<String, EntityTraitWithVal>>       list    = get0(entityAmounts, addon, clsName);
				for (Map<String, EntityTraitWithVal> map : es) {
					list.add(new HashMap<>(map));
				}
			}
		}
		return new DefaultUserPlacer(entityAmounts);
	}
	
	/**
	 * create a new user placer with initially no start entities<br>
	 * to add entities use {@link #addEntity(SCAddon, String, Map)}
	 * 
	 * @return the new user placer
	 * @see #addEntity(SCAddon, String, Map)
	 */
	public static DefaultUserPlacer createWithNone() {
		return new DefaultUserPlacer(new HashMap<>());
	}
	
	private static final int SUB0_USR_PLCR = 0x2EDA1FC3;
	private static final int SUB1_USR_PLCR = 0x8C0F0626;
	private static final int SUB2_USR_PLCR = 0x796C2612;
	private static final int SUB3_USR_PLCR = 0x9871F044;
	private static final int SUB4_USR_PLCR = 0x209ABC36;
	private static final int FIN_USR_PLCR  = 0xDADA38F3;
	
	/** {@inheritDoc} */
	@Override
	public void writePlacer(Connection conn) throws IOException {
		int len = this.entityAmounts.size();
		conn.writeInt(len);
		for (Entry<String, List<Map<String, EntityTraitWithVal>>> entry : this.entityAmounts.entrySet()) {
			if (--len < 0) throw new ConcurrentModificationException();
			conn.writeInt(SUB0_USR_PLCR);
			String                                type      = entry.getKey();
			List<Map<String, EntityTraitWithVal>> entities  = entry.getValue();
			String                                addonName = addonName(type);
			String                                clsName   = clsName(type, addonName);
			SCAddon                               addon     = SCAddon.addon(addonName);
			List<String>                          traits    = new ArrayList<>(addon.entities().traits(clsName).keySet());
			int                                   tlen      = traits.size();
			Map<String, Integer>                  is        = new HashMap<>(tlen);
			conn.writeString(addonName);
			conn.writeString(clsName);
			conn.writeInt(SUB1_USR_PLCR);
			conn.writeInt(tlen);
			for (int i = 0; i < traits.size(); i++) {
				String traitName = traits.get(i);
				if (--tlen < 0) throw new ConcurrentModificationException();
				conn.writeString(traitName);
				is.put(traitName, Integer.valueOf(i));
			}
			if (tlen != 0) throw new ConcurrentModificationException();
			conn.writeInt(SUB2_USR_PLCR);
			int elen = entities.size();
			conn.writeInt(elen);
			for (Map<String, EntityTraitWithVal> vals : entities) {
				if (--elen < 0) throw new ConcurrentModificationException();
				conn.writeInt(SUB3_USR_PLCR);
				if (vals.size() != traits.size()) throw new IllegalStateException(ENTITY_TRAITS_HAS_DIFFERENT_SIZE_THAN_ADDON_SAYS);
				for (String traitName : traits) {
					EntityTraitWithVal tn = vals.get(traitName);
					EntityTraitWithVal.writeTrait(tn, conn);
				}
			}
			if (elen != 0) throw new ConcurrentModificationException();
			conn.writeInt(SUB4_USR_PLCR);
		}
		if (len != 0) throw new ConcurrentModificationException();
		conn.writeInt(FIN_USR_PLCR);
	}
	
	/**
	 * reads a {@link DefaultUserPlacer}
	 * 
	 * @param conn the connection
	 * 
	 * @return the {@link DefaultUserPlacer} which was read
	 * 
	 * @throws IOException if an IO error occurs
	 * 
	 * @see #writePlacer(Connection)
	 */
	public static UserPlacer readPlacer(Connection conn) throws IOException {
		DefaultUserPlacer dup = new DefaultUserPlacer(new HashMap<>());
		int               len = conn.readPos();
		while (len-- > 0) {
			conn.readInt(SUB0_USR_PLCR);
			SCAddon                  addon   = SCAddon.addon(conn.readString());
			String                   clsName = conn.readString();
			Map<String, EntityTrait> traits  = addon.entities().traits(clsName);
			conn.readInt(SUB1_USR_PLCR);
			int tlen = traits.size();
			conn.readInt(tlen);
			String[] traitNames = new String[tlen];
			for (int i = 0; tlen-- > 0; i++) {
				traitNames[i] = conn.readString();
			}
			conn.readInt(SUB2_USR_PLCR);
			int                                   elen = conn.readPos();
			List<Map<String, EntityTraitWithVal>> es   = new ArrayList<>(elen);
			while (elen-- > 0) {
				conn.readInt(SUB3_USR_PLCR);
				Map<String, EntityTraitWithVal> ets = new HashMap<>(traitNames.length);
				for (int i = 0; i < traitNames.length; i++) {
					EntityTrait        trait    = traits.get(traitNames[i]);
					EntityTraitWithVal traitVal = EntityTraitWithVal.readTrait(trait, conn);
					ets.put(traitNames[i], traitVal);
				}
				es.add(ets);
			}
			conn.readInt(SUB4_USR_PLCR);
		}
		conn.readInt(FIN_USR_PLCR);
		return dup;
	}
	
	/** {@inheritDoc} */
	@Override
	public void initilize(World world, User[] usrs, ACORNRandom rnd) {
		int sum = 0;
		for (List<Map<String, EntityTraitWithVal>> list : this.entityAmounts.values()) {
			sum += list.size();
		}
		int     size = (int) Math.sqrt(sum) + 1;
		Point[] p    = new Point[usrs.length];
		int     xlen = world.xlen();
		int     ylen = world.ylen();
		for (int i = 0; i < p.length; i++) {
			int x;
			int y;
			int cnt = -1;
			do {
				if (++cnt == 8) {
					checkPossible(p, i, size, xlen, ylen);
					System.err.println(TRIED_8_INVALID_POSITIONS_THERE_IS_A_FREE_POSITION_CONTINUE);
				}
				x = rnd.nextInt(xlen - size);
				y = rnd.nextInt(ylen - size);
			} while (isUsed(p, i, size, x, y));
			p[i] = new Point(x, y);
			initUsr(rnd, usrs[i], world, x, y, size, sum);
		}
	}
	
	private void initUsr(ACORNRandom rnd, User usr, World world, int x, int y, int size, int remainUnitCount) {
		Point[] p = new Point[remainUnitCount];
		for (Entry<String, List<Map<String, EntityTraitWithVal>>> entry : this.entityAmounts.entrySet()) {
			String                                type      = entry.getKey();
			List<Map<String, EntityTraitWithVal>> list      = entry.getValue();
			String                                addonName = addonName(type);
			AddonEntities                         aes       = SCAddon.addon(addonName).entities();
			String                                clsName   = clsName(type, addonName);
			for (Map<String, EntityTraitWithVal> es : list) {
				int x0;
				int y0;
				int cnt = -1;
				do {
					if (++cnt == 8) {
						checkPossible(p, remainUnitCount, 1, size, size);
						System.err.println(TRIED_8_INVALID_POSITIONS_THERE_IS_A_FREE_POSITION_CONTINUE);
					}
					x0 = rnd.nextInt(size);
					y0 = rnd.nextInt(size);
				} while (isUsed(p, p.length - remainUnitCount, size, x0, y0));
				x0 += x;
				y0 += y;
				Entity e = aes.createEntity(clsName, usr, es, x0, y0);
				Tile   t = world.tile(x0, y0);
				p[p.length - remainUnitCount--] = new Point(x0, y0);
				switch (e) {
				case @SuppressWarnings("preview") Unit u -> t.unit(u);
				case @SuppressWarnings("preview") Building b -> t.build(b);
				}
			}
		}
	}
	
	private static void checkPossible(Point[] p, int i, int size, int xlen, int ylen) {
		xlen -= size;
		ylen -= size;
		int need = p.length - i;
		for (int x = 0; x < xlen; x++) {
			for (int y = 0; y < ylen; y++) {
				if (!isUsed(p, i, size, x, y) && --need <= 0) return;
			}
		}
		throw new IllegalStateException(WORLD_IS_FULL_NOT_ENUGH_PLACE_FOR_ALL_PLAYERS);
	}
	
	private static boolean isUsed(Point[] p, int i, int size, int x, int y) {
		while (i-- > 0) {
			if (Math.abs(x - p[i].x) < size && Math.abs(y - p[i].y) < size) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * adds the given start entity to this user placer
	 * 
	 * @param addon   the {@link SCAddon#name}
	 * @param clsName the class name (see {@link AddonEntities#entityClassses()})
	 * @param traits  the entity traits (see {@link AddonEntities#traits(String)})
	 */
	public void addEntity(SCAddon addon, String clsName, Map<String, EntityTraitWithVal> traits) {
		Map<String, EntityTraitWithVal> tcpy  = new HashMap<>(traits);
		Map<String, EntityTrait>        needs = addon.entities().traits(clsName);
		if (needs.size() != tcpy.size()) throw new IllegalArgumentException(GIVEN_TRAITS_DO_NOT_MATCH_THE_NEEDED_TRAITS);
		for (EntityTrait t : needs.values()) {
			EntityTraitWithVal twv = tcpy.get(t.name());
			if (twv == null || !t.equals(twv.trait())) throw new IllegalArgumentException(GIVEN_TRAITS_DO_NOT_MATCH_THE_NEEDED_TRAITS);
		}
		get0(this.entityAmounts, addon, clsName).add(tcpy);
	}
	
	/**
	 * removes all start entities of this user placer
	 */
	public void removeAll() {
		this.entityAmounts.clear();
	}
	
	/**
	 * removes all start entities of this user placer which belong to the given {@link SCAddon}
	 * 
	 * @param addon the addon which should have no start entities
	 */
	public void removeAll(SCAddon addon) {
		if (addon == null) throw new NullPointerException(NO_ADDON);
		for (String clsName : addon.entities().entityClassses().values()) {
			this.entityAmounts.remove(key(addon, clsName));
		}
	}
	
	/**
	 * removes all start entities of this user placer which belong to the given {@link SCAddon} and have the given class name (see
	 * {@link AddonEntities#entityClassses()})
	 * 
	 * @param addon   the addon which should have no start entities
	 * @param clsName the class name (see {@link AddonEntities#entityClassses()})
	 * @return the previous entities from the given addon with the given class name
	 */
	public List<Map<String, EntityTraitWithVal>> removeAll(SCAddon addon, String clsName) {
		if (addon == null) throw new NullPointerException(NO_ADDON);
		if (clsName == null) throw new NullPointerException(THERE_IS_NO_CLASS_NAME);
		return this.entityAmounts.remove(key(addon, clsName));
	}
	
	/**
	 * removes the start entity with the given {@link SCAddon#addon(Entity) addon}, {@link AddonEntities#entityClassses() class name} and {@link List#remove(int)
	 * index}
	 * 
	 * @param addon   the addon
	 * @param clsName the class name
	 * @param index   the index
	 * @return the removed start entity
	 */
	public Map<String, EntityTraitWithVal> remove(SCAddon addon, String clsName, int index) {
		if (addon == null) throw new NullPointerException(NO_ADDON);
		if (clsName == null) throw new NullPointerException(THERE_IS_NO_CLASS_NAME);
		return this.entityAmounts.get(key(addon, clsName)).remove(index);
	}
	
	/**
	 * gets a copy of all start entities of this user placer which belong to the given {@link SCAddon} and have the given class name (see
	 * {@link AddonEntities#entityClassses()})
	 * <p>
	 * the index of the list returned can be used to remove them (see {@link #remove(SCAddon, String, int)})
	 * 
	 * @param addon   the addon
	 * @param clsName the class name (see {@link AddonEntities#entityClassses()})
	 * @return the entities from the given addon with the given class name
	 */
	public List<Map<String, EntityTraitWithVal>> get(SCAddon addon, String clsName) {
		if (addon == null) throw new NullPointerException(NO_ADDON);
		if (clsName == null) throw new NullPointerException(THERE_IS_NO_CLASS_NAME);
		List<Map<String, EntityTraitWithVal>> list   = this.entityAmounts.get(key(addon, clsName));
		List<Map<String, EntityTraitWithVal>> result = new ArrayList<>();
		for (Map<String, EntityTraitWithVal> map : list) {
			result.add(new HashMap<>(map));
		}
		return result;
	}
	
	/**
	 * returns all start entities of this user placer which belong to the given {@link SCAddon}
	 * 
	 * @param addon the addon which should have no start entities
	 * @return all start entities of this user placer which belong to the given {@link SCAddon}
	 */
	public Map<String, List<Map<String, EntityTraitWithVal>>> get(SCAddon addon) {
		if (addon == null) throw new NullPointerException(NO_ADDON);
		Map<String, List<Map<String, EntityTraitWithVal>>> result = new HashMap<>();
		for (Entry<String, List<Map<String, EntityTraitWithVal>>> entry : this.entityAmounts.entrySet()) {
			String                                key  = entry.getKey();
			List<Map<String, EntityTraitWithVal>> list = entry.getValue();
			
			String addonName = addonName(key);
			if (!addon.name.equals(addonName)) continue;
			String clsName = clsName(key, addonName);
			
			List<Map<String, EntityTraitWithVal>> resList = result.computeIfAbsent(clsName, cls -> new ArrayList<>());
			for (Map<String, EntityTraitWithVal> map : list) {
				resList.add(new HashMap<>(map));
			}
		}
		return result;
	}
	
	/**
	 * returns all start entities of this user placer
	 * 
	 * @return all start entities of this user placer
	 */
	public Map<SCAddon, Map<String, List<Map<String, EntityTraitWithVal>>>> getAll() {
		Map<SCAddon, Map<String, List<Map<String, EntityTraitWithVal>>>> result = new HashMap<>();
		for (Entry<String, List<Map<String, EntityTraitWithVal>>> entry : this.entityAmounts.entrySet()) {
			String                                key  = entry.getKey();
			List<Map<String, EntityTraitWithVal>> list = entry.getValue();
			
			String  addonName = addonName(key);
			String  clsName   = clsName(key, addonName);
			SCAddon addon     = SCAddon.addon(addonName);
			
			Map<String, List<Map<String, EntityTraitWithVal>>> clsMap  = result.computeIfAbsent(addon, a -> new HashMap<>());
			List<Map<String, EntityTraitWithVal>>              resList = clsMap.computeIfAbsent(clsName, cls -> new ArrayList<>());
			for (Map<String, EntityTraitWithVal> map : list) {
				resList.add(new HashMap<>(map));
			}
		}
		return result;
	}
	
	private static List<Map<String, EntityTraitWithVal>> get0(Map<String, List<Map<String, EntityTraitWithVal>>> entityAmounts, SCAddon addon, String clsName) {
		return entityAmounts.computeIfAbsent(key(addon, clsName), s -> new ArrayList<>());
	}
	
	private static String key(SCAddon addon, String clsName) { return addon.name + '\0' + clsName; }
	
	private static String clsName(String type, String addonName) { return type.substring(addonName.length() + 1); }
	
	private static String addonName(String type) { return type.substring(0, type.indexOf('\0')); }
	
}
