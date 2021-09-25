package de.hechler.patrick.games.squareconqerer.objects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hechler.patrick.games.squareconqerer.enums.Direction;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.interfaces.Entety;
import de.hechler.patrick.games.squareconqerer.interfaces.Player;
import de.hechler.patrick.zeugs.interfaces.Position2D;
import de.hechler.patrick.zeugs.objects.Position2DImpl;

/**
 * {@link TheSquare} is the ultimate playground.
 * 
 * {@link TheSquare} knows everything, which happens on {@link TheSquare},
 * 
 * while {@link PlayersSquare} may know only a part of this
 * 
 * (for know the {@link PlayersSquare} delegates to {@link TheSquare})
 */
public class TheSquare {
	
	private final Tile[][] tiles;
	private final Random rnd;
	private final Map <Player, PlayerData> players;
	private final Map <Player, Integer> playernums;
	private final Map <Building, Position2D> buildings;
	
	public TheSquare(int xLen, int yLen) {
		this.tiles = new Tile[xLen][yLen];
		for (int xi = 0; xi < xLen; xi ++ ) {
			for (int yi = 0; yi < yLen; yi ++ ) {
				this.tiles[xi][yi] = new Tile(xi, yi);
			}
		}
		this.rnd = new Random();
		this.players = new HashMap <>();
		this.playernums = new LinkedHashMap <>();
		this.buildings = new HashMap <>();
	}
	
	public void initPlayer(Player p) {
		if (this.players.containsKey(p)) {
			throw new IllegalArgumentException("I know alredy this player!");
		}
		int x, y;
		do {
			// avoid the borders, so the player has some place
			x = rnd.nextInt(tiles.length - 2) + 1;
			y = rnd.nextInt(tiles[0].length - 2) + 1;
			//@formatter:off
			if (tiles[x][y].getEntety() == null
				// avoid other players, so the players has some place
				&& tiles[x + 1][y]    .getEntety() == null
				&& tiles[x]    [y + 1].getEntety() == null
				&& tiles[x - 1][y]    .getEntety() == null
				&& tiles[x]    [y - 1].getEntety() == null
				) {
				//@formatter:on
				break;
			}
		} while (true);
		PlayersSquare ps = new PlayersSquare(this, p);
		Unit u = new Unit(p, x, y);
		this.tiles[x][y].setEntety(u);
		this.players.put(p, new PlayerData(u, ps));
		String pi = Integer.toString(this.playernums.size(), 36);
		if (pi.length() != 1) {
			throw new IndexOutOfBoundsException("too many players!");
		}
		p.letter(pi.charAt(0));
		p.setMySquare(ps);
		this.playernums.put(p, this.playernums.size());
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
	
	void died(Entety u) {
		System.out.println("called on death");
		Tile t = tiles[u.getX()][u.getY()];
		if (t.getEntety() != null) {
			System.out.println("unit still there, will remove it");
			t.remEntety(u);
		} else {
			System.out.println("unit not there!");
		}
	}
	
	public TurnExecutionException isValid(Turn t) {
		return simulate(t, () -> {
		});
	}
	
	TurnExecutionException simulate(Turn t, Runnable r) {
		Snapshot sn = snapshot();
		TurnExecutionException te;
		try {
			execute0(t);
			r.run();
			te = null;
		} catch (TurnExecutionException e) {
			te = e;
		}
		rollback(sn);
		return te;
	}
	
	private void rollback(Snapshot sn) {
		this.buildings.forEach((b, p) -> {
			this.tiles[p.getX()][p.getY()].remBuild(b);
		});
		this.buildings.clear();
		sn.buildings.forEach((b, s) -> {
			this.buildings.put(b, s.pos);
			this.tiles[s.pos.getX()][s.pos.getY()].setBuild(b);
			b.rollback(s.sn);
		});
		this.players.forEach((p, pd) -> {
			pd.units.forEach(e -> {
				this.tiles[e.getX()][e.getY()].remEntety(e);
			});
			pd.units.clear();
		});
		sn.entetys.forEach((p, d) -> {
			PlayerData pd = this.players.get(p);
			d.forEach((e, s) -> {
				pd.units.add(e);
				this.tiles[s.pos.getX()][s.pos.getY()].setEntety(e);
				e.rollback(s.sn);
			});
		});
	}
	
	private Snapshot snapshot() {
		Snapshot sn = new Snapshot();
		this.buildings.forEach((b, p) -> {
			sn.buildings.put(b, new Snapshot.SNObj(p, b.snapshot()));
		});
		this.players.forEach((p, d) -> {
			Map <Entety, Snapshot.SNObj> m = new HashMap <>();
			d.units.forEach(u -> {
				Position2DImpl pos = new Position2DImpl(u.getX(), u.getY());
				m.put(u, new Snapshot.SNObj(pos, u.snapshot()));
			});
			sn.entetys.put(p, m);
		});
		return sn;
	}
	
	private static class Snapshot {
		
		final Map <Player, Map <Entety, SNObj>> entetys = new HashMap <>();
		final Map <Building, SNObj> buildings = new HashMap <>();
		
		private static class SNObj {
			
			final Position2D pos;
			final Object sn;
			
			
			public SNObj(Position2D pos, Object sn) {
				this.pos = pos;
				this.sn = sn;
			}
			
		}
		
	}
	
	TurnExecutionException execute(Turn turn) {
		Snapshot sn = snapshot();
		try {
			execute0(turn);
			return null;
		} catch (TurnExecutionException e) {
			rollback(sn);
			return e;
		}
	}
	
	private void execute0(Turn turn) throws TurnExecutionException {
		try {
			Player player = turn.getPlayer();
			List <Action> actions = turn.getActions();
			{// check actions
				Set <Object> acts = new LinkedHashSet <>();
				for (Action action : actions) {
					if (action instanceof EntetyAction) {
						EntetyAction ea = (EntetyAction) action;
						if ( !acts.add(ea.e)) {
							throw new TurnExecutionException("multiple actions for one entity are not allowed!");
						}
					} else if (action instanceof BuildingAction) {
						BuildingAction ba = (BuildingAction) action;
						if ( !acts.add(ba.b)) {
							throw new TurnExecutionException("multiple actions for one building are not allowed!");
						}
					} else {
						throw new InternalError("unknown action class: " + action.getClass().getName() + " of action: '" + action + "'");
					}
				}
			}
			{// execute actions
				synchronized (Tile.class) {
					Tile.bc = (t, n) -> {
						int x = t.getX(), y = t.getY();
						buildings.put(n, new Position2DImpl(x, y));
					};
					Tile.ec = (t, n) -> {
						Player o = n.owner();
						PlayerData pd = players.get(o);
						pd.units.add(n);
					};
					for (Action action : actions) {
						if (action instanceof EntetyAction) {
							if (action instanceof MoveEntetyAction) {
								MoveEntetyAction mov = (MoveEntetyAction) action;
								moveUnit(mov.e, mov.dir, player);
							} else if (action instanceof AttackEntetyAction) {
								AttackEntetyAction attack = (AttackEntetyAction) action;
								attack(attack.e, attack.defender, player);
							} else if (action instanceof SelfKillEntetyAction) {
								SelfKillEntetyAction kill = (SelfKillEntetyAction) action;
								kill.e.selfkill();
							} else if (action instanceof UsingEntetyAction) {
								UsingEntetyAction use = (UsingEntetyAction) action;
								Tile t = this.tiles[use.e.getX()][use.e.getY()];
								Building build = t.getBuild();
								try {
									build.use(use.e);
								} catch (TurnExecutionException e1) {
									throw new TurnExecutionException(e1.getMessage(), e1);
								}
							} else if (action instanceof BuildingEntetyAction) {
								BuildingEntetyAction build = (BuildingEntetyAction) action;
								Tile t = this.tiles[build.e.getX()][build.e.getY()];
								if (t.getBuild() != null) {
									throw new TurnExecutionException("there is already a building, you can't buid two buildings in one place!");
								}
								Building b = build.build.create();
								this.buildings.put(b, new Position2DImpl(t.getX(), t.getY()));
								t.setBuild(b);
							} else {
								throw new InternalError("unknown action class: " + action.getClass().getName() + " of action: '" + action + "'");
							}
						} else if (action instanceof BuildingAction) {
							if (action instanceof ActingBuildingAction) {
								ActingBuildingAction act = (ActingBuildingAction) action;
								Tile t = tiles[act.x][act.y];
								if (t.getBuild() != act.b) {
									throw new TurnExecutionException("the building is not on my tile[x=" + act.x + ",y" + act.y + "]! actionbuilding='" + act.b + "' mybuilding='"
											+ t.getBuild() + "' mytile='" + t + "'");
								}
								act.b.act(t);
							} else {
								throw new InternalError("unknown action class: " + action.getClass().getName() + " of action: '" + action + "'");
							}
						} else {
							throw new InternalError("unknown action class: " + action.getClass().getName() + " of action: '" + action + "'");
						}
					}
					//@formatter:off
					Tile.bc = (a, b) -> {};
					Tile.ec = (a, b) -> {};
					//@formatter:on
				}
			}
		} catch (RuntimeException re) {
			throw new TurnExecutionException(re.getMessage(), re);
		}
	}
	
	private void attack(Entety attack, Entety defend, Player p) throws TurnExecutionException {
		try {
			if (attack.owner() != p) {
				throw new TurnExecutionException("this is not your unit! you='" + p + "' owner='" + attack.owner() + "' unit='" + attack + "'");
			}
			if (p == defend.owner()) {
				System.err.println("you attack your own unit!");
			}
			int ax = attack.getX(), ay = attack.getY(), dx = defend.getX(), dy = defend.getY();
			int distance = Math.abs(ax - dx) + Math.abs(ay - dy);
			int maxDistance = 1;
			boolean meele = true;
			Building ab = tiles[ax][ay].getBuild();
			Building db = tiles[dx][dy].getBuild();
			int attackStrenght = 1;
			int defendStrenght = 0;
			int al = attack.lives();
			int dl = defend.lives();
			if (dl + 2 < al) {
				attackStrenght ++ ;
			} else if (al + 2 < dl) {
				defendStrenght ++ ;
			}
			if (ab != null && ab.buildLen() > 0 && ab instanceof ArrowTower) {
				meele = false;
				maxDistance ++ ;
				attackStrenght += maxDistance - distance;// more attack when near, because of bad targeting in the distance
			}
			if (db != null && db.buildLen() > 0 && db instanceof DefenceTower) {
				defendStrenght ++ ;
			}
			if (distance > maxDistance) {
				throw new TurnExecutionException(
						"too large distance: distance=" + distance + " maxDist=" + maxDistance + " attack='" + attack + "' defend='" + defend + "' player='" + p + "'");
			}
			int strengh = attackStrenght - (defendStrenght / 2);
			if (meele) {
				// both can get damage
				if (strengh > 0) {
					defend.damage(strengh);
				}
				strengh = defendStrenght - (attackStrenght / 2);
				if (strengh > 0) {
					attack.damage(strengh);
				}
			} else {
				// only defender can get damage
				if (strengh > 0) {
					defend.damage(strengh);
				}
			}
		} catch (RuntimeException re) {
			throw new TurnExecutionException("message: '" + re.getMessage() + "' class: " + re.getClass().getName(), re);
		}
	}
	
	private void moveUnit(Entety u, Direction dir, Player p) throws TurnExecutionException {
		try {
			if (u.owner() != p) {
				throw new TurnExecutionException("this is not your unit! you='" + p + "' owner='" + u.owner() + "' unit='" + u + "'");
			}
			int x = u.getX(), y = u.getY();
			Tile src = tiles[x][y];
			if (src.getEntety() != u) {
				throw new TurnExecutionException("this is not the Unit you gave me! unit=" + src.getEntety() + " your=" + u + " x=" + x + " y=" + y);
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
			Entety de = dst.getEntety();
			if (de != null) {
				throw new TurnExecutionException("there is a entity on the target field!");
			} else {
				src.remEntety(u);
				dst.setEntety(u);
				u.setXY(x, y);
			}
		} catch (RuntimeException re) {
			throw new TurnExecutionException("there was something wrong: '" + re.getMessage() + "' class: " + re.getClass().getName(), re);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder build = new StringBuilder();
		this.playernums.forEach((p, n) -> {
			build.append("player[").append(Integer.toString(n.intValue(), 36)).append("]='").append(p).append("'\n");
		});
		String maxLen = "Y=:" + String.valueOf(tiles.length);
		{
			char[] chars = maxLen.toCharArray();
			Arrays.fill(chars, ' ');
			maxLen = new String(chars);
		}
		for (int y = this.tiles[0].length - 1; y >= 0; y -- ) {
			String ystr = String.valueOf(y);
			build.append("Y=").append(maxLen.substring(ystr.length() + 3)).append(ystr).append(':');
			for (int x = 0; x < this.tiles.length; x ++ ) {
				Building b = this.tiles[x][y].getBuild();
				if (b == null) {
					build.append("[---|");
				} else {
					build.append('[').append(b.buildLen()).append(b.factory().letter()).append(b.infoLetter()).append('|');
				}
				Entety u = this.tiles[x][y].getEntety();
				if (u == null) {
					build.append("---");
				} else {
					build.append(u.lives()).append('P').append(this.playernums.get(u.owner()));
				}
				build.append(']');
			}
			build.append('\n');
		}
		build.append("X: ").append(maxLen.substring(3/* len of 'X: ' */));
		for (int x = 0; x < this.tiles[0].length; x ++ ) {
			String xstr = String.valueOf(x);
			build.append("     ".substring(xstr.length())).append(xstr).append("    ");
		}
		build.append('\n');
		return build.toString();
	}
	
	private static class PlayerData {
		
		private final Set <Entety> units;
		@SuppressWarnings("unused") // may be used in later versions
		private final PlayersSquare square;
		
		private PlayerData(Unit start, PlayersSquare ps) {
			this.units = new HashSet <>();
			this.units.add(start);
			this.square = ps;
		}
		
	}
	
}
