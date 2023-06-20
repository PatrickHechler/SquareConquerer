package de.hechler.patrick.games.sc.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;

public class Tile {
	
	private Ground               ground;
	private final List<Resource> resources = new ArrayList<>();
	private Build                build;
	private final List<Unit>     units     = new ArrayList<>();
	
	public Tile(Ground ground) {
		this.ground = Objects.requireNonNull(ground, "ground is null");
	}
	
	public Ground ground() {
		return this.ground.unmodifiable();
	}
	
	public List<Resource> resourcesList() {
		return this.resources.stream().map(Resource::unmodifiable).toList();
	}
	
	public Stream<Resource> resourcesStream() {
		return this.resources.stream().map(Resource::unmodifiable);
	}
	
	public Build build() {
		return this.build != null ? this.build.unmodifiable() : null;
	}
	
	public List<Unit> unitsList() {
		return this.units.stream().map(Unit::unmodifiable).toList();
	}
	
	public Stream<Unit> unitsStream() {
		return this.units.stream().map(Unit::unmodifiable);
	}
	
}
