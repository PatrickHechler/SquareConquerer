package de.hechler.patrick.sc.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.Player;
import de.hechler.patrick.sc.interfaces.Position;
import de.hechler.patrick.sc.utils.factory.EntityFactory;

public class Game {
	
	private final List <PWWAR> players;
	private final World        theWorld;
	private volatile boolean   init;
	
	
	
	public Game(World world) {
		this.players = new ArrayList <Game.PWWAR>();
		this.init = true;
		this.theWorld = world;
	}
	
	
	
	public void addPlayer(Player player, Position startPos) throws IllegalStateException {
		if ( !this.init) throw new IllegalStateException("not in the init mode!");
		Objects.requireNonNull(player, "i will not add a null player!");
		Objects.requireNonNull(startPos, "i can not place a player on a null Positon!");
		for (PWWAR pwwar : players) {
			if (pwwar.player == player) {
				System.err.println("WARN: the player '" + player + "' is already added, i will continue adding the player.");// if you want to test an bot by fighting against itself (every information
																																// needed is given in the method makeTurn to the bot so i could be the
																																// same Object)
			}
		}
		PWWAR pwwar = new PWWAR(player);
		pwwar.world = unknownWorld(theWorld);
		Field startField = theWorld.getField(startPos);
		if (startField.hasEntity()) throw new IllegalArgumentException("has olready an entity");
		startField.setEntity(EntityFactory.create(startPos, Type.builder, null));
		pwwar.world.overrideField(startField);
		this.players.add(pwwar);
	}
	
	private final World unknownWorld(World world) {
		int xCnt = world.getXCnt();
		int yCnt = world.getYCnt();
		World res = new World(xCnt, yCnt);
		AbsoluteManipulablePosition amp = new AbsoluteManipulablePosition(0, 0);
		for (; amp.x < xCnt; amp.x ++ ) {
			for (amp.y = 0; amp.y < yCnt; amp.y ++ ) {
				Field f = new FieldImpl(amp, Grounds.unknown);
				res.overrideField(f);
			}
		}
		return res;
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
		for (PWWAR pwwar : players) {
			rebuildWorld(pwwar);
			pwwar.player.makeTurn(pwwar.world, pwwar.resources);
		}
	}
	
	private void rebuildWorld(PWWAR pwwar) {
		
	}

	private static class PWWAR {
		
		private PWWAR(Player player) {
			this.player = player;
		}
		
		Map <Resources, Integer> resources;
		
		World  world;
		Player player;
		
	}
	
}
