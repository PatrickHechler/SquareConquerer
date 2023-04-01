package de.hechler.patrick.games.squareconqerer.world;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.EnumImages;
import de.hechler.patrick.games.squareconqerer.world.enums.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;

public sealed class Tile permits RemoteTile {
	
	public final TileType     type;
	public final OreResourceType resource;
	private Building          build;
	private Unit              unit;
	
	private Icon icon;
	
	public Tile(TileType type, OreResourceType resource) {
		if (type == null || resource == null) {
			throw new NullPointerException("type or resource is null");
		}
		this.type     = type;
		this.resource = resource;
	}
	
	private Tile(TileType type, OreResourceType resource, Building build, Unit unit, Icon icon) {
		this.type     = type;
		this.resource = resource;
		this.build    = build;
		this.unit     = unit;
		this.icon     = icon;
	}
	
	public Icon icon(int width, int height) {
		if (icon == null || icon.getIconWidth() != width || icon.getIconHeight() != height) {
			BufferedImage img = EnumImages.immage(type, resource);
			if (img.getWidth() != width || img.getHeight() != height) {
				icon = new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_FAST));
			} else {
				icon = new ImageIcon(img);
			}
		}
		return icon;
	}
	
	public Tile copy() {
		return new Tile(type, resource, build, unit, icon);
	}
	
	public Unit unit() { return unit; }
	
	public Building building() { return build; }
	
	void unit(Unit unit) { this.unit = unit; }
	
	void build(Building build) { this.build = build; }
	
}
