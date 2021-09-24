package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.*;

public class Unit implements Entety {
	
	public static final int MAX_LIVES = 5;
	
	public final Player owner;
	private int lives;
	private PlayersSquare ps;
	private int x;
	private int y;
	
	public Unit(Player owner, int x, int y, PlayersSquare ps) {
		this.owner = owner;
		this.lives = MAX_LIVES;
		this.x = x;
		this.y = y;
		this.ps = ps;
	}
	
	
	
	@Override
	public Player owner() {
		return owner;
	}
	
	@Override
	public boolean attacked(Entety e) {
		this.lives -- ;
		if (this.lives <= 0) {
			System.out.println("I died: I=" + this);
			this.ps.died(this);
		}
		if (this.lives >= e.lives() + 2) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void defended(Entety ignore) {
		attacked(ignore);
	}
	
	@Override
	public void selfkill() {
		System.out.println("I killed myself: I=" + this);
		this.lives = -1;
		this.ps.died(this);
	}
	
	@Override
	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int getX() {
		return this.x;
	}
	
	@Override
	public int getY() {
		return this.y;
	}
	
	@Override
	public int lives() {
		return this.lives;
	}
	
	@Override
	public void heal(int strenght) {
		if (strenght > 0) {
			this.lives = Math.min(strenght + this.lives, MAX_LIVES);
		}
	}
	
	@Override
	public String toString() {
		return "Unit[owner=" + owner + ", lives=" + lives + ", (x=" + x + "|y=" + y + ")]";
	}
	
	@Override
	public Object snapshot() {
		return new USnapshot(this.lives, this.ps, this.x, this.y);
	}
	
	@Override
	public void rollback(Object sn) {
		USnapshot s = (USnapshot) sn;
		this.lives = s.lives;
		this.ps = s.ps;
		this.x = s.x;
		this.y = s.y;
	}
	
	private static final class USnapshot {
		
		private final int lives;
		private final PlayersSquare ps;
		private final int x;
		private final int y;
		
		public USnapshot(int lives, PlayersSquare ps, int x, int y) {
			this.lives = lives;
			this.ps = ps;
			this.x = x;
			this.y = y;
		}
		
	}
	
}
