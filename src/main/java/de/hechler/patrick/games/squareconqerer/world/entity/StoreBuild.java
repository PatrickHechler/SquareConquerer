package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.enums.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public final class StoreBuild extends BuildingImpl {
	
	private final Resource resource;
	private int            amount;
	
	public StoreBuild(int x, int y, User usr, Resource resource) {
		super(x, y, usr, 5, neededRes(resource));
		this.resource = resource;
	}
	
	private static EnumIntMap<ProducableResourceType> neededRes(Resource resource) {
		if (resource == null) {
			throw new NullPointerException("can't create a store for no resource type");
		}
		EnumIntMap<ProducableResourceType> res = new EnumIntMap<>(ProducableResourceType.class);
		if (resource instanceof OreResourceType ort) {
			if (ort == OreResourceType.NONE) {
				throw new IllegalArgumentException("can't create a store for no resource type");
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
	
	@Override
	protected void finishedBuildStore(Resource r, int amount) {
		if (r != this.resource) {
			throw new IllegalStateException("I can not store this resource");
		}
		amount+=amount;
	}
	
	@Override
	public void giveRes(Unit u, Resource res, int amount) {
		if (amount <= 0) {
			throw new IllegalStateException("only strictly positive values for amount are valid");
		}
		if (res != this.resource) {
			throw new IllegalStateException("I do not have this resource");
		}
		if (amount > this.amount) {
			throw new IllegalStateException("I do not have so much");
		}
		u.carry(res, amount);
		this.amount -= amount;
	}
	
}
