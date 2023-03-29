package de.hechler.patrick.games.squareconqerer.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.world.enums.ResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;
import de.hechler.patrick.games.squareconqerer.world.interfaces.UserPlacer;
import de.hechler.patrick.games.squareconqerer.world.placer.DefaultUserPlacer;

public class RootWorld implements World {
	
	public static final String ROOT_NAME = "root";
	
	private final RootUser             root;
	private final Tile[][]             tiles;
	private final UserPlacer           placer;
	private final Map<User, UserWorld> subWorlds = new HashMap<>();
	
	private RootWorld(RootUser root, Tile[][] tiles, UserPlacer placer) {
		this.root   = root;
		this.tiles  = tiles;
		this.placer = placer == null ? new DefaultUserPlacer(this) : placer;
	}
	
	@Override
	public RootUser user() {
		return root;
	}
	
	@Override
	public int xlen() {
		return tiles.length;
	}
	
	@Override
	public int ylen() {
		return tiles[0].length;
	}
	
	@Override
	public Tile tile(int x, int y) {
		return tiles[x][y];
	}
	
	public synchronized UserWorld of(User usr, int usrModCnt) {
		if (usr == root) {
			throw new IllegalArgumentException("the root has no user world");
		} else if (usr != root.get(usr.name())) {
			throw new AssertionError("the user is not from my root");
		} else {
			return subWorlds.compute(usr, (u, uw) -> {
				if (uw != null && u.modifyCount() == uw.modCnt) {
					return uw;
				}
				return placer.apply(usr, usrModCnt);
			});
		}
	}
	
	public static class Builder implements World {
		
		private static final ResourceType[] RES = ResourceType.values();
		private static final TileType[]     TYPES;
		
		static {
			TileType[] v   = TileType.values();
			int        len = v.length - 1;
			TYPES = new TileType[len];
			System.arraycopy(v, 1, TYPES, 0, len);
		}
		
		private final Tile[][] tiles;
		private final Random   rnd;
		private final RootUser usr;
		
		private int resourceMask = 7;
		
		public Builder(RootUser usr, int xlen, int ylen) {
			this(usr, xlen, ylen, new Random());
		}
		
		public Builder(RootUser usr, int xlen, int ylen, Random rnd) {
			if (xlen <= 0 || ylen <= 0) {
				throw new IllegalArgumentException("xlen=" + xlen + " ylen=" + ylen);
			}
			if (rnd == null) {
				throw new NullPointerException("rnd is null");
			}
			if (usr == null) {
				throw new NullPointerException("usr is null");
			}
			this.usr   = usr;
			this.tiles = new Tile[xlen][ylen];
			this.rnd   = rnd;
		}
		
		private Builder(RootUser usr, Tile[][] tiles, Random rnd) {
			this.usr   = usr;
			this.tiles = tiles;
			this.rnd   = rnd;
		}
		
		public void fillRandom() {
			fillTotallyRandom();
		}
		
		public void fillTotallyRandom() {
			for (Tile[] ts : tiles) {
				for (int i = 0; i < ts.length; i++) {
					if (ts[i] != null) {
						continue;
					}
					TileType     t = TYPES[1 + rnd.nextInt(TYPES.length - 1)]; // skip not explored
					ResourceType r = ResourceType.NONE;
					if ((rnd.nextInt() & resourceMask) == 0) {
						r = RES[rnd.nextInt(RES.length)];
					}
					ts[i] = new Tile(t, r);
				}
			}
		}
		
		public void resourceMask(int resourceMask) { this.resourceMask = resourceMask; }
		
		public int resourceMask() { return this.resourceMask; }
		
		@Override
		public RootUser user() {
			return usr;
		}
		
		@Override
		public int xlen() {
			return tiles.length;
		}
		
		@Override
		public int ylen() {
			return tiles[0].length;
		}
		
		@Override
		public Tile tile(int x, int y) {
			if (tiles[x][y] == null) {
				tiles[x][y] = new Tile(TileType.NOT_EXPLORED, ResourceType.NONE);
			}
			return tiles[x][y];
		}
		
		/**
		 * returns the tile at the given position
		 * <p>
		 * the difference between {@link #tile(int, int)} and {@link #get(int, int)} is,
		 * that this method returns <code>null</code>, if the tile is not set, while
		 * {@link #tile(int, int)} creates a new non explored tile without resource in
		 * this case
		 * 
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * 
		 * @return
		 */
		public Tile get(int x, int y) {
			return tiles[x][y];
		}
		
		public void set(int x, int y, TileType t) {
			if (tiles[x][y] == null) {
				tiles[x][y] = new Tile(t, ResourceType.NONE);
			} else {
				tiles[x][y] = new Tile(t, tiles[x][y].resource);
			}
		}
		
		public void set(int x, int y, ResourceType r) {
			if (tiles[x][y] == null) {
				throw new NullPointerException("the tile does not already exist");
			} else {
				tiles[x][y] = new Tile(tiles[x][y].type, r);
			}
		}
		
		public void set(int x, int y, TileType t, ResourceType r) {
			if (t == null) {
				throw new NullPointerException("type is null");
			}
			if (t == TileType.NOT_EXPLORED) {
				throw new NullPointerException("NOT_EXPLORED is not allowed");
			}
			if (r == null) {
				throw new NullPointerException("resource is null");
			}
			tiles[x][y] = new Tile(t, r);
		}
		
		public void set(int x, int y, Tile t) {
			tiles[x][y] = t;
		}
		
		public RootWorld create() throws IllegalStateException, NullPointerException {
			return create(usr, this.tiles);
		}
		
		public static RootWorld create(RootUser root, Tile[][] tiles) throws IllegalStateException, NullPointerException {
			return create(root, tiles, null);
		}
		
		public static RootWorld create(RootUser root, Tile[][] tiles, UserPlacer placer) throws IllegalStateException, NullPointerException {
			Tile[][] copy = tiles.clone();
			for (int x = 0; x < copy.length; x++) {
				Tile[] ts = copy[x].clone();
				if (ts.length != copy[0].length) {
					throw new IllegalStateException("the world has the have an rectangular form!");
				}
				for (int y = 0; y < ts.length; y++) {
					Tile t = ts[y].copy();
					if (t == null) {
						throw new NullPointerException("no tile is allowed to be null");
					}
					if (t.type == null || t.resource == null) {
						throw new NullPointerException("a tile has a null type/resource");
					}
					if (t.type == TileType.NOT_EXPLORED) {
						throw new IllegalStateException("a tile with type NOT_EXPLORED was found");
					}
				}
			}
			return new RootWorld(root, copy, placer);
		}
		
		public static Builder createBuilder(RootUser root, Tile[][] tiles) {
			return createBuilder(root, tiles, new Random());
		}
		
		public static Builder createBuilder(RootUser root, Tile[][] tiles, Random rnd) {
			Tile[][] copy = tiles.clone();
			for (int x = 0; x < copy.length; x++) {
				if (copy[x].length != copy[0].length) {
					throw new IllegalStateException("the world has the have an rectangular form!");
				} // only enforce an rectangular form for the builder
			}
			return new Builder(root, tiles, rnd);
		}
		
	}
	
}
