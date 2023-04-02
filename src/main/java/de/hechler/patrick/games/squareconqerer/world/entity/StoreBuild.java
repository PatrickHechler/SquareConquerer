package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.world.enums.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public final class StoreBuild extends BuildingImpl {
	
	public static final int NUMBER = 0x5A1C58D0;
	
	private final Resource resource;
	private int            amount;
	
	public StoreBuild(int x, int y, User usr, Resource resource) throws TurnExecutionException {
		super(x, y, usr, 5, neededRes(resource));
		this.resource = resource;
	}
	
	public StoreBuild(int x, int y, User usr, int lives, EnumIntMap<ProducableResourceType> neededBuildResources, int remainBuildTurns,
			Resource resource, int resourceAmount) {
		super(x, y, usr, 5, lives, neededBuildResources, remainBuildTurns);
		this.resource = resource;
		this.amount   = resourceAmount;
	}
	
	private static EnumIntMap<ProducableResourceType> neededRes(Resource resource) throws TurnExecutionException {
		if (resource == null) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		EnumIntMap<ProducableResourceType> res = new EnumIntMap<>(ProducableResourceType.class);
		if (resource instanceof OreResourceType ort) {
			if (ort == OreResourceType.NONE) {
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
			res.set(ProducableResourceType.WOOD, 2);
			res.set(ProducableResourceType.STONE, 1);
			res.set(ProducableResourceType.IRON, 1);
		} else if (resource instanceof ProducableResourceType prt) {
			switch (prt) {
			case GLASS -> {
				res.set(ProducableResourceType.STONE, 3);
				res.set(ProducableResourceType.WOOD, 3);
				res.set(ProducableResourceType.IRON, 1);
			}
			case GOLD, IRON -> {
				res.set(ProducableResourceType.IRON, 1);
				res.set(ProducableResourceType.STONE, 3);
			}
			case STEEL -> {
				res.set(ProducableResourceType.IRON, 3);
				res.set(ProducableResourceType.STONE, 3);
			}
			case STONE, WOOD -> {
				res.set(ProducableResourceType.WOOD, 2);
				res.set(ProducableResourceType.STONE, 2);
			}
			default -> throw new AssertionError("unknown resource type: " + resource.getClass());
			}
		} else {
			throw new AssertionError("unknown resource type: " + resource.getClass());
		}
		return res;
	}
	
	public Resource resource() { return resource; }
	
	public int amount() { return amount; }
	
	@Override
	protected void finishedBuildStore(Resource r, int amount) throws TurnExecutionException {
		if (r != this.resource) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		this.amount += amount;
	}
	
	@Override
	public void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException {
		if (amount <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (res != this.resource) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (amount > this.amount) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (!isFinishedBuild()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		u.carry(res, amount);
		this.amount -= amount;
	}
	
	@Override
	public StoreBuild copy() {
		return new StoreBuild(x, y, owner(), lives(), neededResources(), remainingBuildTurns(), resource, amount);
	}
	
}
