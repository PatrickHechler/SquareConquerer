package de.hechler.patrick.games.squareconqerer.world;

import de.hechler.patrick.games.squareconqerer.world.enums.ResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;

public final class RemoteTile extends Tile {
	
	public final long created;
	
	public RemoteTile(TileType type, ResourceType resource) {
		this(System.currentTimeMillis(), type, resource);
	}
	
	public RemoteTile(long time, TileType type, ResourceType resource) {
		super(type, resource);
		created = time;
	}
	
}
