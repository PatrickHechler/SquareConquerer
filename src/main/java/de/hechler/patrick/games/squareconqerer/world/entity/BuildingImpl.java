package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public abstract sealed class BuildingImpl extends EntityImpl implements Building permits StoreBuild {
	
	private EnumIntMap<ProducableResourceType> neededResources;
	private int                                neededBuildTurns;
	
	protected BuildingImpl(int x, int y, User usr, int maxlives, EnumIntMap<ProducableResourceType> neededResources) {
		super(x, y, usr, maxlives, maxlives, 0);
		this.neededResources = neededResources;
		for (int val : neededResources.array()) {
			this.neededBuildTurns += val;
		}
		this.neededBuildTurns = (this.neededBuildTurns >>> 1) + 1;
	}
	
	protected BuildingImpl(int x, int y, User usr, int maxlives, int lives, EnumIntMap<ProducableResourceType> neededResources,
			int remainingBuildTurns) {
		super(x, y, usr, maxlives, lives, 0);
		this.neededResources  = neededResources;
		this.neededBuildTurns = remainingBuildTurns;
	}
	
	@Override // check both if there is no build time
	public boolean isFinishedBuild() { return neededBuildTurns <= 0 && neededResources != null; }
	
	@Override
	public int remainingBuildTurns() { return neededBuildTurns; }
	
	@Override
	public EnumIntMap<ProducableResourceType> neededResources() { return neededResources == null ? null : neededResources.copy(); }
	
	@Override
	public void store(Unit u, int amount) throws TurnExecutionException {
		checkOwner(u);
		if (u.carryAmount() < amount) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (amount <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (neededResources == null) {
			finishedBuildStore(u.carryRes(), amount);
			u.uncarry(amount);
			return;
		}
		Resource r = u.carryRes();
		if (!(r instanceof ProducableResourceType prt)) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		int amt = neededResources.get(prt);
		if (amt == 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (amt < amount) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		amt -= amount;
		neededResources.set(prt, amt);
	}
	
	protected void finishedBuildStore(Resource r, int amount) throws TurnExecutionException {
		throw new TurnExecutionException(ErrorType.INVALID_TURN);
	}
	
	@Override
	public void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException {
		throw new TurnExecutionException(ErrorType.INVALID_TURN);
	}
	
	@Override
	public void build(Unit u) throws TurnExecutionException {
		checkOwner(u);
		if (neededResources != null) {
			for (int val : neededResources.array()) {
				if (val > 0) {
					throw new TurnExecutionException(ErrorType.INVALID_TURN);
				}
			}
			neededResources = null;
		}
		if (neededBuildTurns <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		neededBuildTurns--;
	}
	
	@Override
	public String cls() { return "Building"; }
	
}
