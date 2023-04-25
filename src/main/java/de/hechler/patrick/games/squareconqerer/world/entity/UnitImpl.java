package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

public abstract sealed class UnitImpl extends EntityImpl implements Unit permits Carrier, MyUnit {
	
	public static final int MY_COUNT_NO_NULL = 1;

	protected Resource  carryResource;
	protected int       carryAmount;
	protected final int carryMaxAmount;
	
	protected UnitImpl(int x, int y, User usr, int maxlives, int viewRange, int carryMaxAmount) {
		super(x, y, usr, maxlives, maxlives, viewRange);
		this.carryMaxAmount = carryMaxAmount;
	}
	
	protected UnitImpl(int x, int y, User usr, int maxlives, int lives, int viewRange, int carryMaxAmount, int carryAmount, Resource res) {
		super(x, y, usr, maxlives, lives, viewRange);
		this.carryMaxAmount = carryMaxAmount;
		this.carryAmount    = carryAmount;
		this.carryResource  = res;
	}
	
	@Override
	public void changePos(int newx, int newy, Tile checkCanTile) throws TurnExecutionException {
		checkValid(checkCanTile);
		super.x = newx;
		super.y = newy;
	}
	
	protected void checkValid(Tile checkCanTile) throws TurnExecutionException {
		if (!checkCanTile.type.isLand()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
	}
	
	@Override
	public Resource carryRes() {
		return this.carryResource;
	}
	
	@Override
	public int carryAmount() {
		return this.carryAmount;
	}
	
	@Override
	public int carryMaxAmount() {
		return this.carryMaxAmount;
	}
	
	@Override
	public void carry(Resource res, int amount) throws TurnExecutionException {
		if (amount < 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (res == null) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (res != this.carryResource && this.carryResource != null) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		this.carryResource  = res;
		this.carryAmount   += amount;
	}
	
	@Override
	public void uncarry(int amount) throws TurnExecutionException {
		if (amount <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (this.carryAmount < amount) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		this.carryAmount -= amount;
		if (this.carryAmount == 0) {
			this.carryResource = null;
		}
	}
	
	@Override
	public final String cls() { return "Unit"; }
	
}
