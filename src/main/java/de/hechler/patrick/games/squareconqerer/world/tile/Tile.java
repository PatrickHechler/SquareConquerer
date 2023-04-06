package de.hechler.patrick.games.squareconqerer.world.tile;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.StackWalker.Option;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObjs;

public sealed class Tile permits RemoteTile {
	
	private static Icon[] icons;
	
	public final TileType        type;
	public final OreResourceType resource;
	private boolean              visible;
	private Building             build;
	private Unit                 unit;
	
	private static int created;
	
	public Tile(TileType type, OreResourceType resource, boolean visible) {
		if (type == null || resource == null) {
			throw new NullPointerException("type or resource is null");
		}
		this.type     = type;
		this.resource = resource;
		this.visible  = visible;
		if ((created++ & 0x1FF) == 0) {
			System.out.println("created " + created + " tile instances");
		}
	}
	
	private Tile(TileType type, OreResourceType resource, boolean visible, Building build, Unit unit) {
		this.type     = type;
		this.resource = resource;
		this.visible  = visible;
		this.build    = build;
		this.unit     = unit;
		if ((created++ & 0x1FF) == 0) {
			System.out.println("created " + created + " tile instances");
		}
	}
	
	private static BufferedImage notVisible;
	private static boolean       triedLoading;
	
	public Icon icon(int width, int height) {
		return icon(this, width, height);
	}
	
	private static Icon icon(Tile t, int width, int height) {
		if (icons == null) {
			icons = new Icon[(OreResourceType.count() * TileType.count()) << 1];
		}
		int index = (t.resource.ordinal() + OreResourceType.count() * t.type.ordinal()) << 1;
		if (t.visible) index++;
		if (icons[index] == null || icons[index].getIconWidth() != width || icons[index].getIconHeight() != height) {
			System.out.println("create icon: " + t);
			BufferedImage img = ImageableObjs.immage(t.type, t.resource, t.build, t.unit);
			if (!t.visible) {
				if (notVisible == null && !triedLoading) {
					try {
						notVisible = ImageIO.read(t.getClass().getResource("/img/not-visible.png"));
					} catch (IOException e) {
						System.err.println("could not load the not visible hover:");
						e.printStackTrace();
						triedLoading = true;
					}
				}
				if (notVisible != null) {
					BufferedImage img0 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
					Graphics2D    g    = img0.createGraphics();
					g.drawImage(img, 0, 0, null);
					g.drawImage(notVisible, 0, 0, width, height, null);
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
	
	public Tile copy() {
		return new Tile(type, resource, visible, build == null ? null : build.copy(), unit == null ? null : unit.copy());
	}
	
	public Unit unit() { return unit; }
	
	public Building building() { return build; }
	
	public boolean visible() { return visible; }
	
	public void unit(Unit unit) { this.unit = unit; }
	
	public void build(Building build) {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != UserWorld.class && caller != RootWorld.class) {
			throw new IllegalCallerException("illegal caller: " + caller);
		}
		this.build = build;
	}
	
	public void visible(boolean visible) { this.visible = visible; }
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Tile [type=");
		builder.append(type);
		builder.append(", resource=");
		builder.append(resource);
		builder.append(", visible=");
		builder.append(visible);
		builder.append(", build=");
		builder.append(build);
		builder.append(", unit=");
		builder.append(unit);
		builder.append("]");
		return builder.toString();
	}
	
}