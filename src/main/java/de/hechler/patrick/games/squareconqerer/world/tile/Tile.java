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
package de.hechler.patrick.games.squareconqerer.world.tile;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.StackWalker.Option;
import java.text.Format;
import java.util.List;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPage;
import de.hechler.patrick.games.squareconqerer.world.PageWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld.Builder;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.placer.UserPlacer;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObjs;
import jdk.incubator.concurrent.ScopedValue;

/**
 * this class represents a single tile at the world
 * <p>
 * a tile has a ground and a resource (possibly {@link OreResourceType#NONE})<br>
 * a tile can be {@link #visible() visible}.<br>
 * a tile can have a {@link #building()}<br>
 * a tile can have a {@link #unit()}
 * 
 * @author Patrick Hechler
 */
public sealed class Tile permits RemoteTile {
	
	private static final String TILTLE_BUT_NO_PAGE       = Messages.getString("Tile.title-no-page");               //$NON-NLS-1$
	private static final String PAGE_BUT_NO_TILTLE       = Messages.getString("Tile.page-no-title");               //$NON-NLS-1$
	private static final Format ILLEGAL_CALLER           = Messages.getFormat("Tile.illegal-caller");              //$NON-NLS-1$
	private static final String I_HAVE_NO_PAGE           = Messages.getString("Tile.have-no-page");                //$NON-NLS-1$
	private static final String COULD_NOT_LOAD_UNVISIBLE = Messages.getString("Tile.warn-loading-visible-failed"); //$NON-NLS-1$
	private static final Format LOG_CREATE_ICON          = Messages.getFormat("Tile.log-create-icon");             //$NON-NLS-1$
	private static final String NO_GROUND_OR_NO_RESOURCE = Messages.getString("Tile.null-ground-or-resource");     //$NON-NLS-1$
	
	private static Icon[] icons;
	
	/**
	 * the ground type of this tile
	 */
	public final GroundType      ground;
	/**
	 * the resource type of this tile
	 */
	public final OreResourceType resource;
	private boolean              visible;
	private Building             build;
	private Unit                 unit;
	private Supplier<SCPage>     page;
	private String               title;
	
	/**
	 * creates a new tile with the given ground, resource and initial visibility
	 * 
	 * @param ground   the ground type of the new tile
	 * @param resource the resource type of the new tile
	 * @param visible  the initial visibility of the new tile
	 */
	public Tile(GroundType ground, OreResourceType resource, boolean visible) {
		if (ground == null || resource == null) {
			throw new NullPointerException(NO_GROUND_OR_NO_RESOURCE);
		}
		this.ground   = ground;
		this.resource = resource;
		this.visible  = visible;
	}
	
	private Tile(GroundType type, OreResourceType resource, boolean visible, Building build, Unit unit, Supplier<SCPage> page, String title) {
		this.ground   = type;
		this.resource = resource;
		this.visible  = visible;
		this.build    = build;
		this.unit     = unit;
		this.page     = page;
		this.title    = title;
	}
	
	private static BufferedImage notVisible;
	private static boolean       triedLoading;
	
	/**
	 * returns an {@link Icon} representing this tile with the given sizes
	 * 
	 * @param width  the width of the icon
	 * @param height the height of the icon
	 * 
	 * @return an {@link Icon} representing this tile with the given sizes
	 */
	public Icon icon(int width, int height) {
		return icon(this, width, height);
	}
	
	private static Icon icon(Tile t, int width, int height) {
		if (icons == null) {
			icons = new Icon[(OreResourceType.count() * GroundType.count() * (Building.COUNT) * (Unit.COUNT)) << 1];
		}
		int index = (t.ground.ordinal() * OreResourceType.count() + t.resource.ordinal()) * (Building.COUNT);
		index = (index + Building.ordinal(t.build)) * (Unit.COUNT);
		index = (index + Unit.ordinal(t.unit)) << 1;
		if (t.visible) index++;
		if (icons[index] == null || icons[index].getIconWidth() != width || icons[index].getIconHeight() != height) {
			System.out.println(Messages.format(LOG_CREATE_ICON, t));
			BufferedImage img = ImageableObjs.immage(t.ground, t.resource, t.build, t.unit);
			if (!t.visible) {
				if (notVisible == null && !triedLoading) {
					try {
						notVisible = ImageIO.read(t.getClass().getResource("/img/not-visible.png")); //$NON-NLS-1$
					} catch (IOException e) {
						System.err.println(COULD_NOT_LOAD_UNVISIBLE);
						e.printStackTrace();
						triedLoading = true;
					}
				}
				if (notVisible != null) {
					BufferedImage img0 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
					Graphics2D    g    = img0.createGraphics();
					g.drawImage(img, 0, 0, null);
					g.drawImage(notVisible, 0, 0, img.getWidth(), img.getHeight(), null);
					img = img0;
				}
			}
			if (img.getWidth() != width || img.getHeight() != height) {
				icons[index] = new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_FAST));
			} else {
				icons[index] = new ImageIcon(img);
			}
		}
		return icons[index];
	}
	
	/**
	 * creates a copy of this tile
	 * 
	 * @return a copy of this tile
	 */
	public Tile copy() {
		return new Tile(this.ground, this.resource, this.visible, this.build == null ? null : this.build.copy(), this.unit == null ? null : this.unit.copy(),
				this.page, this.title);
	}
	
	/**
	 * returns <code>true</code> if this tile has a page associated with it
	 * 
	 * @return <code>true</code> if this tile has a page associated with it
	 */
	public boolean hasPage() { return this.page != null; }
	
	/**
	 * returns the page associated with the tile
	 * 
	 * @return the page associated with the tile
	 * 
	 * @throws IllegalStateException if this tile has no page
	 */
	public SCPage page() throws IllegalStateException {
		if (this.page == null) throw new IllegalStateException(I_HAVE_NO_PAGE);
		return this.page.get();
	}
	
	/**
	 * returns the title of the pages associated with the tile
	 * 
	 * @return the title of the pages associated with the tile
	 * 
	 * @throws IllegalStateException if this tile has no page
	 */
	public String pageTitle() throws IllegalStateException {
		if (this.page == null) throw new IllegalStateException(I_HAVE_NO_PAGE);
		return this.title;
	}
	
	/**
	 * returns the unit on this tile or <code>null</code> if there is no uni
	 * 
	 * @return the unit on this tile or <code>null</code> if there is no uni
	 */
	public Unit unit() { return this.unit; }
	
	/**
	 * returns the building on this tile or <code>null</code> if there is no uni
	 * 
	 * @return the building on this tile or <code>null</code> if there is no uni
	 */
	public Building building() { return this.build; }
	
	/**
	 * returns an list containing all entities on this tile
	 * <ol>
	 * <li>if there is a building:
	 * <ol>
	 * <li>the building</li>
	 * <li>all its units (sorted like {@link Entity#units()} method)</li>
	 * </ol>
	 * </li>
	 * <li>all units on the tile sorted like in the {@link Entity#units()} method:
	 * <ol>
	 * <li>the unit</li>
	 * <li>all its units (sorted like {@link Entity#units()} method)</li>
	 * </ol>
	 * </li>
	 * </ol>
	 * 
	 * @return an list containing all entities on this tile
	 */
	public List<Entity> entities() {
		Unit     u = this.unit;
		Building b = this.build;
		if (u == null) {
			if (b == null) return List.of();
			return List.of(b);
		} else if (b == null) return List.of(u);
		else return List.of(b, u);
	}
	
	/**
	 * returns <code>true</code> if this tile is currently visible and <code>false</code> if no
	 * 
	 * @return <code>true</code> if this tile is currently visible and <code>false</code> if no
	 */
	public boolean visible() { return this.visible; }
	
	private static final ScopedValue<Boolean> NO_CHECK = ScopedValue.newInstance();
	
	/**
	 * this is an intern method which is only allowed to be called from the {@link RootWorld}
	 * <p>
	 * runs the given runnable without checking the caller when {@link #unit(Unit)} or {@link #build(Building)} is called
	 * 
	 * @param r the runnable to be run without caller checks on {@link #unit(Unit)}/{@link #build(Building)}
	 */
	public static void noCheck(Runnable r) {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != RootWorld.class) {
			throw new IllegalCallerException(Messages.format(ILLEGAL_CALLER, caller));
		}
		ScopedValue.where(NO_CHECK, Boolean.TRUE, r);
	}
	
	/**
	 * executes the given runnable with caller checks on {@link #unit(Unit)}/{@link #build(Building)}
	 * <p>
	 * this method can be called when a {@link UserPlacer} executes untrusted code
	 * 
	 * @param r the runnable to be executed with checks
	 */
	@SuppressWarnings("removal")
	public static void withCheck(Runnable r) {
		Boolean f = Boolean.FALSE;
		if (f.booleanValue()) f = new Boolean(false);
		ScopedValue.where(NO_CHECK, f, r);
	}
	
	/**
	 * this method is only allowed to be called from a {@link UserPlacer} during world initialization or from {@link UserWorld}, {@link RootWorld} and
	 * {@link Builder}
	 * <p>
	 * sets the tiles unit
	 * 
	 * @param unit the new unit of this tile
	 */
	public void unit(Unit unit) {
		if (!NO_CHECK.isBound() || !NO_CHECK.get().booleanValue()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != UserWorld.class && caller != RootWorld.class && caller != RemoteWorld.class && caller != RootWorld.Builder.class) {
				throw new IllegalCallerException(Messages.format(ILLEGAL_CALLER, caller));
			}
		}
		this.unit = unit;
	}
	
	/**
	 * this method is only allowed to be called from a {@link UserPlacer} during world initialization or from {@link UserWorld}, {@link RootWorld} and
	 * {@link Builder}
	 * <p>
	 * sets the tiles building
	 * 
	 * @param build the new building of this tile
	 */
	public void build(Building build) {
		if (!NO_CHECK.isBound() || !NO_CHECK.get().booleanValue()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != UserWorld.class && caller != RootWorld.class && caller != RemoteWorld.class && caller != RootWorld.Builder.class) {
				throw new IllegalCallerException(Messages.format(ILLEGAL_CALLER, caller));
			}
		}
		this.build = build;
	}
	
	/**
	 * sets the page of this tile
	 * <p>
	 * calling this method from any class other than {@link PageWorld} will result in an {@link IllegalCallerException}
	 * 
	 * @param page  the new page
	 * @param title the pages title
	 * 
	 * @throws IllegalCallerException if called from a class other than {@link PageWorld}
	 */
	public void page(Supplier<SCPage> page, String title) throws IllegalCallerException {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != PageWorld.class) {
			throw new IllegalCallerException(Messages.format(ILLEGAL_CALLER, caller));
		}
		if (page != null && title == null) throw new NullPointerException(PAGE_BUT_NO_TILTLE);
		if (page == null && title != null) throw new NullPointerException(TILTLE_BUT_NO_PAGE);
		this.page  = page;
		this.title = title;
	}
	
	/**
	 * sets the visibility of this tile
	 * <p>
	 * calling this method from any class other than {@link PageWorld} will result in an {@link IllegalCallerException}
	 * 
	 * @param visible the new visibility of this tile
	 * 
	 * @throws IllegalCallerException if called from a class other than {@link PageWorld}
	 */
	public void visible(boolean visible) throws IllegalCallerException {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != UserWorld.class) {
			throw new IllegalCallerException(Messages.format(ILLEGAL_CALLER, caller));
		}
		this.visible = visible;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder()//
				.append("Tile [type=").append(this.ground) //$NON-NLS-1$
				.append(", resource=").append(this.resource) //$NON-NLS-1$
				.append(", visible=").append(this.visible) //$NON-NLS-1$
				.append(", build=").append(this.build) //$NON-NLS-1$
				.append(", unit=").append(this.unit) //$NON-NLS-1$
				.append(']').toString();
	}
	
}
