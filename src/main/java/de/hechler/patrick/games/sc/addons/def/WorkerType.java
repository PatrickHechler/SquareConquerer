package de.hechler.patrick.games.sc.addons.def;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.UnitType;
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.MapValue;
import de.hechler.patrick.games.sc.values.UserValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.values.spec.IntSpec;
import de.hechler.patrick.games.sc.values.spec.MapSpec;
import de.hechler.patrick.games.sc.values.spec.UserSpec;
import de.hechler.patrick.games.sc.values.spec.ValueSpec;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.utils.objects.Random2;

public class WorkerType extends UnitType {
	
	private static final String NAME       = "pat.simple.worker";
	private static final String NAME_LOC = "worker/builder";
	
	static final int MAX_LIVES = 20;
	
	private static final List<Map<String, Value>> START_VALUES = List.of(Map.of( //
			Unit.CARRY, new MapValue<>(Unit.CARRY, Map.of()), //
			Unit.MOVE_RANGE, new IntValue(Unit.MOVE_RANGE, 10), //
			Unit.WORK_EFFICIENCY, new IntValue(Unit.WORK_EFFICIENCY, 2), //
			Entity.LIVES, new IntValue(Entity.LIVES, MAX_LIVES), //
			Entity.OWNER, new UserValue(Entity.OWNER, null), //
			WorldThing.VIEW_BLOCK, new IntValue(WorldThing.VIEW_BLOCK, 0), //
			Entity.VIEW_RANGE, new IntValue(Entity.VIEW_RANGE, 5), //
			Entity.X, new IntValue(Entity.X, 0), //
			Entity.Y, new IntValue(Entity.Y, 0) //
	));
	
	private static final Map<String, ValueSpec> VALUES = Map.of( //
			Unit.CARRY, new MapSpec(Unit.CARRY, Unit.CARRY_LOC), //
			Unit.MOVE_RANGE, new IntSpec(Unit.MOVE_RANGE, Unit.MOVE_RANGE_LOC, 10, 10), //
			Unit.WORK_EFFICIENCY, new IntSpec(Unit.WORK_EFFICIENCY, Unit.WORK_EFFICIENCY, 5, 5), //
			Entity.LIVES, new IntSpec(Entity.LIVES, Entity.LIVES_LOC, 1, MAX_LIVES), //
			Entity.OWNER, new UserSpec(Entity.OWNER, Entity.OWNER_LOC), //
			WorldThing.VIEW_BLOCK, new IntSpec(WorldThing.VIEW_BLOCK, WorldThing.VIEW_BLOCK_LOC, 0, 0), //
			Entity.VIEW_RANGE, new IntSpec(Entity.VIEW_RANGE, Entity.VIEW_RANGE_LOC, 2, 5), //
			Entity.X, new IntSpec(Entity.X, Entity.X_LOC, 0, Integer.MAX_VALUE), //
			Entity.Y, new IntSpec(Entity.Y, Entity.Y_LOC, 0, Integer.MAX_VALUE) //
	);
	
	private static final long MAX_CARRY = 5L;
	
	public WorkerType() {
		super(NAME, NAME_LOC, VALUES);
	}
	
	@Override
	public List<Map<String, Value>> startEntities() {
		return START_VALUES;
	}
	
	@Override
	public Unit withValues(Map<String, Value> values, UUID uuid) throws TurnExecutionException {
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
		Map<String, ? extends Value> carry = ((MapValue<?>) values.get(Unit.CARRY)).value();
		if (carry.values().stream().mapToLong(v -> ((Resource) ((WorldThingValue) v).value()).amount()).sum() > MAX_CARRY) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		return new WorkerUnit(uuid, values);
	}
	
	@Override
	public Unit withDefaultValues(World w, Random2 r, int x, int y) {
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
	public Unit withRandomValues(World w, Random2 r, int x, int y) {
		return withDefaultValues(w, r, x, y);
	}
	
}
