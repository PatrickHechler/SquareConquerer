package de.hechler.patrick.games.squareconqerer.world;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.EnumImages;
import de.hechler.patrick.games.squareconqerer.world.enums.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;

public sealed class Tile permits RemoteTile {
	
	public final TileType        type;
	public final OreResourceType resource;
	private boolean              visible;
	private Building             build;
	private Unit                 unit;
	
	private Icon icon;
	
	public Tile(TileType type, OreResourceType resource, boolean visible) {
		if (type == null || resource == null) {
			throw new NullPointerException("type or resource is null");
		}
		this.type     = type;
		this.resource = resource;
		this.visible  = visible;
	}
	
	private Tile(TileType type, OreResourceType resource, boolean visible, Building build, Unit unit, Icon icon) {
		this.type     = type;
		this.resource = resource;
		this.visible  = visible;
		this.build    = build;
		this.unit     = unit;
		this.icon     = icon;
	}
	
	private static BufferedImage notVisible;
	private static boolean       triedLoading;
	
	public Icon icon(int width, int height) {
		if (icon == null || icon.getIconWidth() != width || icon.getIconHeight() != height) {
			BufferedImage img = EnumImages.immage(type, resource);
			if (!visible) {
				if (notVisible == null && !triedLoading) {
					try {
						notVisible = ImageIO.read(getClass().getResource("/img/not-visible.png"));
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
				icon = new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_FAST));
			} else {
				icon = new ImageIcon(img);
			}
		}
		return icon;
	}
	
	public Tile copy() {
		return new Tile(type, resource, visible, build == null ? null : build.copy(), unit == null ? null : unit.copy(), icon);
	}
	
	public Unit unit() { return unit; }
	
	public Building building() { return build; }
	
	public boolean visible() { return visible; }
	
	void unit(Unit unit) { this.unit = unit; }
	
	void build(Building build) { this.build = build; }
	
	public void visible(boolean visible) {
		if (visible != this.visible) {
			this.icon    = null;
			this.visible = visible;
		}
	}
	
}
