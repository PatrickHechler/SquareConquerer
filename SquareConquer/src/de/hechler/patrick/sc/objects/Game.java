package de.hechler.patrick.sc.objects;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.Player;
import de.hechler.patrick.sc.interfaces.Position;
import de.hechler.patrick.sc.utils.factory.EntityFactory;
import de.hechler.patrick.sc.utils.factory.EntityFactory.StorageCreateParam;

public class Game {
	
	private final Random     rnd;
	private volatile boolean init;
	private final Set <PAW>  players;
	private final World      theWorld;
	private final World      unknownWorld;
	
	
	
	public Game(World world) {
		this.rnd = new Random();
		this.players = new LinkedHashSet <Game.PAW>();
		this.init = true;
		this.theWorld = world;
		int xCnt = world.getXCnt();
		int yCnt = world.getYCnt();
		unknownWorld = new World(xCnt, yCnt);
		AbsoluteManipulablePosition amp = new AbsoluteManipulablePosition(0, 0);
		for (; amp.x < xCnt; amp.x ++ ) {
			for (amp.y = 0; amp.y < yCnt; amp.y ++ ) {
				Field f = new FieldImpl(amp, Grounds.unknown);
				unknownWorld.overrideField(f);
			}
		}
	}
	
	
	
	private final World unknownWorld(World world) {
		int xCnt = world.getXCnt();
		int yCnt = world.getYCnt();
		World res = new World(xCnt, yCnt);
		AbsoluteManipulablePosition amp = new AbsoluteManipulablePosition(0, 0);
		for (; amp.x < xCnt; amp.x ++ ) {
			for (amp.y = 0; amp.y < yCnt; amp.y ++ ) {
				res.overrideField(unknownWorld.getField(amp));
			}
		}
		return res;
	}
	
	public void addPlayer(Player player, Position startPos) throws IllegalStateException {
		if ( !this.init) throw new IllegalStateException("not in the init mode!");
		Objects.requireNonNull(player, "i will not add a null player!");
		Objects.requireNonNull(startPos, "i can not place a player on a null Positon!");
		for (PAW paw : players) {
			if (paw.player == player) {
				System.err.println("WARN: the player '" + player + "' is already added, i will continue adding the player.");// if you want to test an bot by fighting against itself (every information
																																// needed is given in the method makeTurn to the bot so i could be the
																																// same Object)
			}
		}
		int id = rnd.nextInt();// Random generation of the Player-ID
		PAW paw = new PAW(player, id);
		while (players.contains(paw)) {
			id ++ ;
			paw = new PAW(player, id);
		}
		paw.world = unknownWorld(theWorld);
		Field startField = theWorld.getField(startPos);
		if (startField.hasEntity()) throw new IllegalArgumentException("has olready an entity");
		assertFreeFields(startPos, 1);
		startField.setEntity(EntityFactory.create(id, startPos, Type.builder, null));
		Position pos = startPos.newCreateMove(Direction.up);
		StorageBuilding store = (StorageBuilding) EntityFactory.create(id, pos, Type.storage, new StorageCreateParam(50, Resources.wood));
		store.store(Resources.wood, 10);
		startField.setEntity(store);
		pos = startPos.newCreateMove(Direction.down);
		store = (StorageBuilding) EntityFactory.create(id, pos, Type.storage, new StorageCreateParam(25, Resources.eat));
		store.store(Resources.eat, 5);
		startField.setEntity(store);
		pos = startPos.newCreateMove(Direction.rigth);
		store = (StorageBuilding) EntityFactory.create(id, pos, Type.storage, new StorageCreateParam(75, Resources.drink));
		store.store(Resources.drink, 10);
		startField.setEntity(store);
		paw.world.overrideField(startField);
		this.players.add(paw);
	}
	
	private void assertFreeFields(Position pos, int radius) throws IllegalStateException {
		final int x = pos.getX(), y = pos.getY(), xCnt = theWorld.getXCnt(), yCnt = theWorld.getYCnt();
		AbsoluteManipulablePosition amp = new AbsoluteManipulablePosition(x, y);
		for (; amp.x <= radius && amp.x < xCnt; amp.x ++ ) {
			for (amp.y = y; amp.y + amp.x <= radius && amp.y < yCnt; amp.y ++ ) {
				if (theWorld.getField(amp).hasEntity()) throw new IllegalStateException("not free: " + amp.toPosStr() + " asserted the fields around " + pos.toPosStr() + " with the radius " + radius
						+ " (pos exclusive) would be without an entity (entity='" + theWorld.getField(amp).getEntity() + "', field='" + theWorld.getField(amp) + "')");
			}
			for (amp.y = y; amp.y - amp.x <= radius && amp.y >= 0; amp.y -- ) {
				if (theWorld.getField(amp).hasEntity()) throw new IllegalStateException("not free: " + amp.toPosStr() + " asserted the fields around " + pos.toPosStr() + " with the radius " + radius
						+ " (pos exclusive) would be without an entity (entity='" + theWorld.getField(amp).getEntity() + "', field='" + theWorld.getField(amp) + "')");
			}
		}
		for (amp.x = x; -amp.x <= radius && amp.x >= 0; amp.x -- ) {
			for (amp.y = y; -amp.y + amp.x <= radius && amp.y < yCnt; amp.y ++ ) {
				if (theWorld.getField(amp).hasEntity()) throw new IllegalStateException("not free: " + amp.toPosStr() + " asserted the fields around " + pos.toPosStr() + " with the radius " + radius
						+ " (pos exclusive) would be without an entity (entity='" + theWorld.getField(amp).getEntity() + "', field='" + theWorld.getField(amp) + "')");
			}
			for (amp.y = y; -amp.y - amp.x <= radius && amp.y >= 0; amp.y -- ) {
				if (theWorld.getField(amp).hasEntity()) throw new IllegalStateException("not free: " + amp.toPosStr() + " asserted the fields around " + pos.toPosStr() + " with the radius " + radius
						+ " (pos exclusive) would be without an entity (entity='" + theWorld.getField(amp).getEntity() + "', field='" + theWorld.getField(amp) + "')");
			}
		}
	}
	
	
	
	public void finishInit() {
		if ( !init) throw new IllegalStateException("already finished the init mode!");
		if (players.isEmpty()) throw new IllegalStateException("no players!");
		this.init = false;
	}
	
	public int playerCount() {
		return players.size();
	}
	
	public void makeTurn() {
		newTurnSetup();
		for (PAW paw : players) {
			rebuildWorld(paw);
			paw.player.makeTurn(paw.world, paw.id);
		}
	}
	
	private void newTurnSetup() {
		AbsoluteManipulablePosition amp;
		final int xCnt = theWorld.getXCnt(), yCnt = theWorld.getYCnt();
		for (amp = new AbsoluteManipulablePosition(0, 0); amp.x < xCnt; amp.x ++ ) {
			for (amp.y = 0; amp.y < yCnt; amp.y ++ ) {
				Field f = theWorld.getField(amp);
				if (f.hasEntity()) {
					f.getEntity().newTurn();
				}
			}
		}
	}
	
	private void rebuildWorld(PAW paw) {
		final int yStart = paw.world.getYCnt() - 1;
		AbsoluteManipulablePosition amp;
		List <Entity> entities = new ArrayList <Entity>();
		for (amp = new AbsoluteManipulablePosition(paw.world.getXCnt() - 1, 0); amp.x >= 0; amp.x -- ) {
			for (amp.y = yStart; amp.y >= 0; amp.y -- ) {
				Field f = paw.world.getField(amp);
				Entity e = f.getEntity();
				if (e != null) {
					entities.add(e);
				} else {
					f = new FieldImpl(f.position(), f.ground(), true);
				}
			}
		}
		for (Entity e : entities) {
			makeVisible(paw.world, e.position(), e.sight());
		}
	}
	
	private void makeVisible(World target, Position pos, int sight) {
		final int x = pos.getX(), y = pos.getY(), xCnt = target.getXCnt(), yCnt = target.getYCnt();
		AbsoluteManipulablePosition amp = new AbsoluteManipulablePosition(x, y);
		for (; amp.x <= sight && amp.x < xCnt; amp.x ++ ) {
			for (amp.y = y; amp.y + amp.x <= sight && amp.y < yCnt; amp.y ++ ) {
				target.overrideField(theWorld.getField(amp));
			}
			for (amp.y = y; amp.y - amp.x <= sight && amp.y >= 0; amp.y -- ) {
				target.overrideField(theWorld.getField(amp));
			}
		}
		for (amp.x = x; -amp.x <= sight && amp.x >= 0; amp.x -- ) {
			for (amp.y = y; -amp.y + amp.x <= sight && amp.y < yCnt; amp.y ++ ) {
				target.overrideField(theWorld.getField(amp));
			}
			for (amp.y = y; -amp.y - amp.x <= sight && amp.y >= 0; amp.y -- ) {
				target.overrideField(theWorld.getField(amp));
			}
		}
	}
	
	private static class PAW {
		
		private PAW(Player player, int id) {
			this.player = player;
			this.id = id;
		}
		
		final int id;
		World     world;
		Player    player;
		
		@Override
		public int hashCode() {
			return id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() == getClass()) {
				return ((PAW) obj).id == id;
			}
			return false;
		}
		
	}
	
}
