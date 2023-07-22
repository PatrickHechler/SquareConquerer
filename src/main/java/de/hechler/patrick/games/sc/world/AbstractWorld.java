package de.hechler.patrick.games.sc.world;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.WorldType;
import de.hechler.patrick.games.sc.turn.NextTurnListener;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.tile.Tile;

public class AbstractWorld extends World {
	
	protected final User usr;
	
	@Override
	public User user() {
		return this.usr;
	}
	
	@Override
	public int turn() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int xlen() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int ylen() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public Tile tile(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addNextTurnListener(NextTurnListener listener) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void removeNextTurnListener(NextTurnListener listener) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void finish(Turn t) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public Map<User, List<Entity<?, ?>>> entities() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public WorldThing<?, ?> get(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public WorldType type() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Map<String, Value> values() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Value value(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
