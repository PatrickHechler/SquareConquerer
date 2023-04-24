package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public final class StoreBuild extends BuildingImpl {
	
	public static final String NAME = "Storage";

	public static final int NUMBER = 0x5A1C58D0;
	
	private final EnumIntMap<OreResourceType>        ores       = new EnumIntMap<>(OreResourceType.class);
	private final EnumIntMap<ProducableResourceType> producable = new EnumIntMap<>(ProducableResourceType.class);
	
	public StoreBuild(int x, int y, User usr) {
		super(x, y, usr, 5, neededRes());
	}
	
	public StoreBuild(int x, int y, User usr, int lives, EnumIntMap<ProducableResourceType> neededBuildResources, int remainBuildTurns,
			EnumIntMap<OreResourceType> ores, EnumIntMap<ProducableResourceType> producable) {
		super(x, y, usr, 5, lives, neededBuildResources, remainBuildTurns);
		this.ores.putAll(ores);
		this.producable.putAll(producable);
	}
	
	private static EnumIntMap<ProducableResourceType> neededRes() {
		EnumIntMap<ProducableResourceType> res = new EnumIntMap<>(ProducableResourceType.class);
		res.set(ProducableResourceType.WOOD, 6);
		res.set(ProducableResourceType.STONE, 3);
		return res;
	}
	
	@Override
	protected void finishedBuildStore(Resource r, int amount) throws TurnExecutionException {
		if (r instanceof ProducableResourceType prt) {
			producable.add(prt, amount);
		} else if (r instanceof OreResourceType ort) {
			ores.add(ort, amount);
		} else {
			throw new AssertionError("unknown resource type: " + r.getClass());
		}
	}
	
	@Override
	public void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException {
		if (amount <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (!isFinishedBuild()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (res instanceof ProducableResourceType prt) {
			if (producable.sub(prt, amount) < 0) {
				producable.add(prt, amount);
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
		} else if (res instanceof OreResourceType ort) {
			if (ores.sub(ort, amount) < 0) {
				ores.add(ort, amount);
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
		} else {
			throw new AssertionError("unknown resource type: " + res.getClass());
		}
		u.carry(res, amount);
	}
	
	@Override
	public StoreBuild copy() {
		return new StoreBuild(x, y, owner(), lives(), neededResources(), remainingBuildTurns(), ores, producable);
	}
	
	public EnumIntMap<ProducableResourceType> producable() {
		EnumIntMap<ProducableResourceType> res = new EnumIntMap<>(ProducableResourceType.class);
		res.putAll(producable);
		return res;
	}
	
	public EnumIntMap<OreResourceType> ores() {
		EnumIntMap<OreResourceType> res = new EnumIntMap<>(OreResourceType.class);
		res.putAll(ores);
		return res;
	}
	
	@Override
	public int ordinal() { return 1; }
	
}
