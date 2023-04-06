package de.hechler.patrick.games.squareconqerer.world.tile;

import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;

public final class RemoteTile extends Tile {
	
	public final long created;
	
	public RemoteTile(TileType type, OreResourceType resource, boolean visible) {
		this(System.currentTimeMillis(), type, resource, visible);
	}
	
	public RemoteTile(long time, TileType type, OreResourceType resource, boolean visible) {
		super(type, resource, visible);
		created = time;
	}
	
}
