package de.hechler.patrick.games.squareconqerer.world;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.hechler.patrick.games.squareconqerer.world.enums.EnumImages;
import de.hechler.patrick.games.squareconqerer.world.enums.ResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;

public sealed class Tile permits RemoteTile {
	
	public final TileType     type;
	public final ResourceType resource;
	
	private Icon icon;
	
	public Tile(TileType type, ResourceType resource) {
		if (type == null || resource == null) {
			throw new NullPointerException("type or resource is null");
		}
		this.type     = type;
		this.resource = resource;
	}
	
	public Icon icon(int width, int height) {
		if (icon == null || icon.getIconWidth() != width || icon.getIconHeight() != height) {
			BufferedImage img = EnumImages.immage(type, resource);
			icon = new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_FAST));
		}
		return icon;
	}
	
	public Tile copy() {
		return new Tile(type, resource);
	}
	
}
