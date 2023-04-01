package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.enums.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public abstract sealed class BuildingImpl extends EntityImpl implements Building permits StoreBuild {
	
	private EnumIntMap<ProducableResourceType> neededResources;
	private int                                neededBuildTurns;
	
	protected BuildingImpl(int x, int y, User usr, int maxlives, EnumIntMap<ProducableResourceType> neededResources) {
		super(x, y, usr, maxlives);
		this.neededResources = neededResources;
		for (int val : neededResources.array()) {
			neededBuildTurns += val;
		}
		neededBuildTurns = (neededBuildTurns >>> 1) + 1;
	}
	
	@Override
	public boolean isFinishedBuild() { return neededBuildTurns <= 0; }
	
	@Override
	public void store(Unit u, int amount) {
		checkOwner(u);
		if (u.carryAmount() < amount) {
			throw new IllegalStateException("the unit carries less resources, than I should take");
		}
		if (amount <= 0) {
			throw new IllegalArgumentException("negative amount");
		}
		if (neededResources == null) {
			finishedBuildStore(u.carryRes(), amount);
			u.uncarry(amount);
			return;
		}
		Resource r = u.carryRes();
		if (!(r instanceof ProducableResourceType prt)) {
			throw new IllegalStateException("I do not need this resource for my build");
		}
		int amt = neededResources.get(prt);
		if (amt == 0) {
			throw new IllegalStateException("I do not need this resource for my build");
		}
		amt -= amt > u.carryAmount() ? u.carryAmount() : amt;
		neededResources.set(prt, amt);
	}
	
	protected void finishedBuildStore(Resource r, int amount) {
		throw new IllegalStateException("I am finished build and I do not support storing resources");
	}
	
	@Override
	public void giveRes(Unit u, Resource res, int amount) {
		throw new IllegalStateException("I do not support this action");
	}
	
	@Override
	public void build(Unit u) {
		checkOwner(u);
		if (neededResources != null) {
			for (int val : neededResources.array()) {
				if (val > 0) {
					throw new IllegalStateException("I still need resources for my build");
				}
			}
			neededResources = null;
		}
		if (neededBuildTurns <= 0) {
			throw new IllegalStateException("I am already finished build");
		}
		neededBuildTurns--;
	}
	
}
