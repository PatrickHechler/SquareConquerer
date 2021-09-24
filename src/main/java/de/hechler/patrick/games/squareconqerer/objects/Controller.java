package de.hechler.patrick.games.squareconqerer.objects;

import java.util.ArrayList;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.interfaces.Player;

public class Controller {
	
	private final List <Player> players;
	private final TheSquare square;
	private boolean started;
	
	public Controller(int xLen, int yLen) {
		this.players = new ArrayList <>();
		this.square = new TheSquare(xLen, yLen);
		this.started = false;
	}
	
	public void addPlayer(Player p) {
		if (this.started) {
			throw new IllegalStateException("I am already running!");
		}
		this.players.add(p);
		this.square.initPlayer(p);
	}
	
	public void start() {
		synchronized (this) {
			if (this.started) {
				throw new IllegalStateException("I am already running!");
			}
			this.started = true;
		}
		new Thread(this::run).start();
	}
	
	private void run() {
		while (true) {
			for (Player player : players) {
				while (true) {
					player.startTurn();
					wait(player);
					Turn turn = player.getTurn();
					TurnExecutionException e = square.execute(turn);
					if (e != null) {
						player.invalidTurn(e);
						continue;
					}
				}
			}
		}
	}
	
	private void wait(Player player) {
		Object obj = player.getNotify();
		while (player.isOnTurn()) {
			synchronized (obj) {
				try {
					obj.wait();
				} catch (InterruptedException e) {
					System.err.println("ignored error:");
					e.printStackTrace();
				}
			}
		}
	}
	
}