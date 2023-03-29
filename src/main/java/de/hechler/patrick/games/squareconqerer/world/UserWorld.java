package de.hechler.patrick.games.squareconqerer.world;

import de.hechler.patrick.games.squareconqerer.User;

public class UserWorld implements World {
	
	private final World world;
	private final User  usr;
	public final int    modCnt;
	
	public UserWorld(World world, User usr, int modCnt) {
		this.world  = world;
		this.usr    = usr;
		this.modCnt = modCnt;
		usr.checkModCnt(modCnt);
	}
	
	@Override
	public User user() {
		usr.checkModCnt(modCnt);
		return usr;
	}
	
	@Override
	public int xlen() {
		usr.checkModCnt(modCnt);
		return world.xlen();
	}
	
	@Override
	public int ylen() {
		usr.checkModCnt(modCnt);
		return world.ylen();
	}
	
	@Override
	public Tile tile(int x, int y) {
		usr.checkModCnt(modCnt);
		return world.tile(x, y);
	}
	
}
