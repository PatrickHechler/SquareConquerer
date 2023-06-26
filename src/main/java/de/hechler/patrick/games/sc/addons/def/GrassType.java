package de.hechler.patrick.games.sc.addons.def;

import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.tile.NeigbourTiles;
import de.hechler.patrick.utils.objects.Random2;


public class GrassType extends GroundType {
	
	private static final String NAME     = "pat.fighter.grass";
	private static final String NAME_LOC = "grass land";
	
	public GrassType() {
		super(NAME, NAME_LOC, Map.of());
	}
	
	@Override
	public int propability(World world, int x, int y, NeigbourTiles neigbours) {
		return 1;
	}
	
	@Override
	public Ground withNeigbours(World world, int x, int y, NeigbourTiles neigbours) {
		return new GrassGround(UUID.randomUUID());
	}
	
	@Override
	public Ground withValues(Map<String, Value> values, UUID uuid) throws TurnExecutionException {
		if (!values.isEmpty()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		return new GrassGround(uuid);
	}
	
	@Override
	public Ground withDefaultValues(World w, Random2 r, int x, int y) {
		return new GrassGround(r.nextUUID());
	}
	
	@Override
	public Ground withRandomValues(World w, Random2 r, int x, int y) {
		return new GrassGround(r.nextUUID());
	}
	
}
