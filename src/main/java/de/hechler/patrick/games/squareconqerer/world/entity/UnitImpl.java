package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public abstract sealed class UnitImpl extends EntityImpl implements Unit permits Carrier {
	
	protected Resource  carryResource;
	protected int       carryAmount;
	protected final int carryMaxAmount;
	
	protected UnitImpl(int x, int y, User usr, int maxlives, int carrymaxAmount) {
		super(x, y, usr, maxlives);
		this.carryMaxAmount = carrymaxAmount;
	}
	
	@Override
	public void xy(int newx, int newy) {
		super.x = newx;
		super.y = newy;
	}
	
	@Override
	public Resource carryRes() {
		return carryResource;
	}
	
	@Override
	public int carryAmount() {
		return carryAmount;
	}
	
	@Override
	public int carryMaxAmount() {
		return this.carryMaxAmount;
	}
	
	@Override
	public void carry(Resource res, int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("amount is negative");
		}
		if (res == null) {
			throw new NullPointerException("resource is null");
		}
		if (res != this.carryResource && this.carryResource != null) {
			throw new IllegalStateException("I currently carry a different resource");
		}
		this.carryResource = res;
		this.carryAmount += amount;
	}
	
	@Override
	public void uncarry(int amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("amount is not strict positive");
		}
		if (this.carryAmount < amount) {
			throw new IllegalArgumentException("I carry less, than I should uncarry");
		}
		this.carryAmount -= amount;
		if (this.carryAmount == 0) {
			this.carryResource = null;
		}
	}
	
}
