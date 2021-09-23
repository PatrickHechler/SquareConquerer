package de.hechler.patrick.games.squareconqerer.objects;

import java.util.*;
import de.hechler.patrick.games.squareconqerer.interfaces.*;
import de.hechler.patrick.games.squareconqerer.enums.*;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;

/**
 * {@link TheSquare} is the ultimate playground.
 * 
 * {@link TheSquare} knows everything, which happanes on {@link TheSquare},
 * 
 * while {@link PlayersSquare} may know only a part of this
 * 
 * <code>(for know the {@link PlayersSquare} delegates to {@link TheSquare})</code>
 */
public class TheSquare {
	
	private final Tile[][] tiles;
	private final Random rnd;
	private final Map <Player, PlayerData> players;
	private final Map <Player, Integer> playernums;
	private int playernum;
	
	public TheSquare(int xLen, int yLen) {
		this.tiles = new Tile[xLen][yLen];
		for (int xi = 0; xi < xLen; xi ++ ) {
			for (int yi = 0; yi < yLen; yi ++ ) {
				this.tiles[xi][yi] = new Tile();
			}
		}
		this.rnd = new Random();
		this.players = new HashMap <>();
		this.playernums = new LinkedHashMap <>();
		this.playernum = 0;
	}
	
	private TheSquare(Tile[][] tiles, Random rnd, Map <Player, PlayerData> players, Map <Player, Integer> playernums, int playernum) {
		this.tiles = tiles;
		this.rnd = rnd;
		this.players = players;
		this.playernums = playernums;
		this.playernum = playernum;
	}
	
	public void initPlayer(Player p) {
		if (this.players.containsKey(p)) {
			throw new IllegalArgumentException("I have alredy this player in my set!");
		}
		int x, y;
		do {
			// avoid the borders, so the player has some place
			x = rnd.nextInt(tiles.length - 2) + 1;
			y = rnd.nextInt(tiles[0].length - 2) + 1;
		} while (tiles[x][y].getUnit() != null);
		PlayersSquare ps = new PlayersSquare(this, p);
		Unit u = new Unit(p, x, y, ps);
		this.tiles[x][y].setUnit(u);
		this.players.put(p, new PlayerData(u, ps));
		p.setMySquare(ps);
		this.playernums.put(p, this.playernum ++ );
	}
	
	public Tile getTile(int x, int y) {
		return tiles[x][y];
	}
	
	public int getXLen() {
		return tiles.length;
	}
	
	public int getYLen() {
		return tiles[0].length;
	}
	
	void died(Unit u) {
		System.out.println("called on death");
		Tile t = tiles[u.getX()][u.getY()];
		if (t.getUnit() == u) {
			System.out.println("unit still there, will remove it");
			t.remUnit();
		} else {
			System.out.println("unit not there!");
		}
	}
	
	public TurnExecutionException isValid(Turn t) {
		Tile[][] tiles = new Tile[this.tiles.length][this.tiles[0].length];
		for (int i = 0; i < tiles.length; i ++ ) {
			for (int ii = 0; ii < tiles.length; ii ++ ) {
				tiles[i][ii] = new Tile();
				tiles[i][ii].copy(tiles[i][ii]);
			}
		}
		TheSquare copy = new TheSquare(tiles, rnd, players, playernums, playernum);
		try {
			copy.execute(t);
			return null;
		} catch (TurnExecutionException te) {
			return te;
		}
	}
	
	void execute(Turn turn) throws TurnExecutionException {
		Player player = turn.getPlayer();
		List <MoveUnitAction> actions = turn.getActions();
		Map <Unit, Action> unitacts = new HashMap <>();
		{// order actions
			for (Action action : actions) {
				if (action instanceof MoveUnitAction) {
					MoveUnitAction mov = (MoveUnitAction) action;
					if (unitacts.put(mov.u, mov) != null) {
						throw new TurnExecutionException("multiple actions for one unit are not allowed!");
					}
				} else {
					throw new InternalError("unknown action class: " + action.getClass().getName() + " of action: '" + action + "'");
				}
			}
		}
		{// execute actions
			for (Action action : unitacts.values()) {
				if (action instanceof MoveUnitAction) {
					MoveUnitAction mov = (MoveUnitAction) action;
					moveUnit(mov.u, mov.dir, player);
				} else {
					throw new InternalError("unknown action class: " + action.getClass().getName() + " of action: '" + action + "'");
				}
			}
		}
	}
	
	private boolean moveUnit(Unit u, Direction dir, Player p) throws TurnExecutionException {
		try {
			if (u.owner != p) {
				throw new TurnExecutionException("this is not your unit! you='" + p + "' owner='" + u.owner + "' unit='" + u + "' you='" + p + "'");
			}
			int x = u.getX(), y = u.getY();
			Tile src = tiles[x][y];
			if (src.getUnit() != u) {
				throw new TurnExecutionException("this is not the Unit you gave me! unit=" + src.getUnit() + " your=" + u + " x=" + x + " y=" + y);
			}
			switch (dir) {
			case xup:
				x ++ ;
				break;
			case xdown:
				x -- ;
				break;
			case yup:
				y ++ ;
				break;
			case ydown:
				y -- ;
				break;
			default:
				throw new InternalError("unknwon direction: " + dir.name());
			}
			Tile dst = tiles[x][y];
			Unit de = dst.getUnit();
			if (de != null) {
				if (u.owner == de.owner) {
					System.err.println("you attack your own unit!");
				}
				if (de.attacked(u)) {
					u.defended(de);
				}
				return false;
			} else {
				Unit mov = src.remUnit();
				dst.setUnit(mov);
				mov.setXY(x, y);
				return true;
			}
		} catch (RuntimeException re) {
			throw new TurnExecutionException("there was something wrong: '" + re.getMessage() + "' class: " + re.getClass().getName(), re);
		}
	}
	
	public String toString() {
		StringBuilder build = new StringBuilder();
		this.playernums.forEach((p, n) -> {
			build.append("player[").append(n).append("]='").append(p).append("'\n");
		});
		String maxLen = "Y=:" + String.valueOf(tiles.length);
		{
			char[] chars = maxLen.toCharArray();
			Arrays.fill(chars, ' ');
			maxLen = new String(chars);
		}
		build.append("X:   ").append(maxLen.substring(3/* len of 'X: ' */));
		for (int x = 0; x < this.tiles[0].length; x ++ ) {
			String xstr = String.valueOf(x);
			build.append(xstr).append("       ".substring(xstr.length()));
		}
		build.append('\n');
		for (int y = 0; y < this.tiles.length; y ++ ) {
			String ystr = String.valueOf(y);
			build.append("Y=").append(maxLen.substring(ystr.length() + 3)).append(ystr).append(':');
			for (int x = 0; x < this.tiles[0].length; x ++ ) {
				Building b = this.tiles[x][y].getBuild();
				build.append('[').append(b == null ? '-' : 'B').append('|');
				Unit u = this.tiles[x][y].getUnit();
				if (u == null) {
					build.append("---");
				} else {
					build.append(u.lives()).append('P').append(this.playernums.get(u.owner));
				}
				build.append(']');
			}
			build.append('\n');
		}
		return build.toString();
	}
	
	private static class PlayerData {
		
		private final Set <Unit> units;
		@SuppressWarnings("unused")
		private final Set <Building> builds;
		@SuppressWarnings("unused")
		private final PlayersSquare square;
		
		private PlayerData(Unit start, PlayersSquare ps) {
			this.units = new HashSet <>();
			this.units.add(start);
			this.builds = new HashSet <>();
			this.square = ps;
		}
		
	}
	
}
