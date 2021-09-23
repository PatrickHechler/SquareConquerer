package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.*;

public class Unit {
	
	public static final int START_LIVES = 5;
	
	public final Player owner;
	private int lives;
	private PlayersSquare ps;
	private int x;
	private int y;
	
	public Unit(Player owner, int x, int y, PlayersSquare ps) {
		this.owner = owner;
		this.lives = START_LIVES;
		this.x = x;
		this.y = y;
		this.ps = ps;
	}
	
	public boolean attacked(Unit ignore) {
		this.lives -- ;
		if (this.lives <= 0) {
			System.out.println("I died: I=" + this);
			this.ps.died(this);
		}
		if (this.lives >= ignore.lives + 2) {
			return true;
		} else {
			return false;
		}
	}
	
	public void defended(Unit ignore) {
		attacked(ignore);
	}
	
	public void selfkill() {
		System.out.println("I died: I=" + this);
		this.ps.died(this);
	}
	
	void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int lives() {
		return this.lives;
	}
	
	@Override
	public String toString() {
		return "Unit[owner=" + owner + ", lives=" + lives + ", (x=" + x + "|y=" + y + ")]";
	}
	
}
