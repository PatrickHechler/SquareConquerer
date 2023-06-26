package de.hechler.patrick.games.sc.addons.def;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.addons.addable.BuildType;
import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.MapValue;
import de.hechler.patrick.games.sc.values.TypeValue;
import de.hechler.patrick.games.sc.values.UserValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.spec.IntSpec;
import de.hechler.patrick.games.sc.values.spec.MapSpec;
import de.hechler.patrick.games.sc.values.spec.TypeSpec;
import de.hechler.patrick.games.sc.values.spec.UserSpec;
import de.hechler.patrick.games.sc.values.spec.ValueSpec;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.utils.objects.Random2;


@SuppressWarnings({ "rawtypes", "unchecked" })
public class CloneFactoryType extends BuildType {
	
	public static final String NAME     = "pat.fighter.clone-factory";
	public static final String NAME_LOC = "clone factory";
	
	static final int MAX_LIVES = 50;
	static final int MAX_TIME  = 10;
	
	static final String CLONE_STATS     = "clone.stats";
	static final String CLONE_STATS_LOC = "clone values";
	static final String CLONE_TYPE      = "clone.type";
	static final String CLONE_TYPE_LOC  = "clone type";
	static final String CLONE_TIME      = "clone.time";
	static final String CLONE_TIME_LOC  = "clone work time";
	
	private static final List<Map<String, Value>> START_VALUES;
	static {
		Map<String, Value> vals = new HashMap<>();
		vals.put(CLONE_STATS, new MapValue(CLONE_STATS, Map.of()));
		vals.put(CLONE_TYPE, new TypeValue(CLONE_TYPE, GroundType.NOT_EXPLORED_TYPE));
		vals.put(CLONE_TIME, new IntValue(CLONE_TIME, 10));
		vals.put(Build.NEEDED_RESOURCES, new MapValue(Build.NEEDED_RESOURCES, Map.of()));
		vals.put(Build.NEEDED_WORK_TURNS, new IntValue(Build.NEEDED_WORK_TURNS, 0));
		vals.put(Build.STORE, new MapValue(Build.STORE, Map.of()));
		vals.put(Entity.LIVES, new IntValue(Entity.LIVES, MAX_LIVES));
		vals.put(Entity.OWNER, new UserValue(Entity.OWNER, null));
		vals.put(WorldThing.VIEW_BLOCK, new IntValue(WorldThing.VIEW_BLOCK, 1));
		vals.put(Entity.VIEW_RANGE, new IntValue(Entity.VIEW_RANGE, 1));
		vals.put(Entity.X, new IntValue(Entity.X, 0));
		vals.put(Entity.Y, new IntValue(Entity.Y, 0));
		START_VALUES = List.of(Map.copyOf(vals));
	}
	
	private static final Map<String, ValueSpec> VALUES;
	static {
		Map<String, ValueSpec> specs = new HashMap<>();
		specs.put(CLONE_STATS, new MapSpec(CLONE_STATS, CLONE_STATS_LOC));
		specs.put(CLONE_TYPE, new TypeSpec(CLONE_TYPE, CLONE_TYPE_LOC, AddableType.class));
		specs.put(CLONE_TIME, new IntSpec(CLONE_TIME, CLONE_TIME_LOC, 0, 10));
		specs.put(Build.NEEDED_RESOURCES, new MapSpec(Build.NEEDED_RESOURCES, Build.NEEDED_RESOURCES_LOC));
		specs.put(Build.NEEDED_WORK_TURNS, new IntSpec(Build.NEEDED_WORK_TURNS, Build.NEEDED_RESOURCES_LOC, 0, 10));
		specs.put(Build.STORE, new MapSpec(Build.STORE, Build.STORE_LOC));
		specs.put(Entity.LIVES, new IntSpec(Entity.LIVES, Entity.LIVES_LOC, 1, MAX_LIVES));
		specs.put(Entity.OWNER, new UserSpec(Entity.OWNER, Entity.OWNER_LOC));
		specs.put(WorldThing.VIEW_BLOCK, new IntSpec(WorldThing.VIEW_BLOCK, WorldThing.VIEW_BLOCK_LOC, 0, 0));
		specs.put(Entity.VIEW_RANGE, new IntSpec(Entity.VIEW_RANGE, Entity.VIEW_RANGE_LOC, 2, 5));
		specs.put(Entity.X, new IntSpec(Entity.X, Entity.X_LOC, 0, Integer.MAX_VALUE));
		specs.put(Entity.Y, new IntSpec(Entity.Y, Entity.Y_LOC, 0, Integer.MAX_VALUE));
		VALUES = Map.copyOf(specs);
	}
	
	public CloneFactoryType() {
		super(NAME, NAME_LOC, VALUES);
	}
	
	@Override
	public List<Map<String, Value>> startEntities() {
		return START_VALUES;
	}
	
	@Override
	public Build withValues(Map<String, Value> values, UUID uuid) throws TurnExecutionException {
		if (values.size() != VALUES.size()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		try {
			for (ValueSpec spec : VALUES.values()) {
				spec.validate(values.get(spec.name()));
			}
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (!((MapValue<?>) values.get(Build.STORE)).value().isEmpty()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (!((MapValue<?>) values.get(Build.NEEDED_RESOURCES)).value().isEmpty()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		return new CloneFactoryBuild(uuid, values);
	}
	
	@Override
	public Build withDefaultValues(World w, Random2 r, int x, int y) {
		if (x < 0 || y < 0 || x >= w.xlen() || y >= w.ylen()) {
			throw new AssertionError();
		}
		Map<String, Value> map = new HashMap<>(START_VALUES.get(0));
		map.put(Entity.X, new IntValue(Entity.X, x));
		map.put(Entity.Y, new IntValue(Entity.Y, y));
		try {
			return withValues(map, r.nextUUID());
		} catch (TurnExecutionException e) {
			throw new AssertionError();
		}
	}
	
	@Override
	public Build withRandomValues(World w, Random2 r, int x, int y) {
		return withDefaultValues(w, r, x, y);
	}
	
}
