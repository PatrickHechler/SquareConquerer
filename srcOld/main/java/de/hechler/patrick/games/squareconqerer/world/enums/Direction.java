//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.world.enums;

/**
 * this class is used to define the four directions (x+-, y+- (or left/right, up/down))
 * 
 * @author Patrick Hechler
 */
public enum Direction {
	
	/**
	 * the <code>x</code> increasing direction<br>
	 * also known as right
	 */
	X_INC(1, 0),
	/**
	 * the <code>x</code> decreasing direction<br>
	 * also known as left
	 */
	X_DEC(-1, 0),
	/**
	 * the <code>y</code> increasing direction<br>
	 * also known as down
	 */
	Y_INC(0, 1),
	/**
	 * the <code>y</code> decreasing direction<br>
	 * also known as up
	 */
	Y_DEC(0, -1);
	
	private static final Direction[] VALS = values();
	
	/**
	 * returns the direction from its {@link Enum#ordinal() ordinal}
	 * 
	 * @param ordinal the {@link Enum#ordinal() ordinal}
	 * @return the {@link Direction} with the given {@link Enum#ordinal() ordinal}
	 */
	public static Direction of(int ordinal) {
		return VALS[ordinal];
	}
	
	/**
	 * returns the amount of directions (<code>4</code>)
	 * 
	 * @return the amount of directions (<code>4</code>)
	 */
	public static int count() {
		return VALS.length;
	}
	
	/**
	 * the x increasing/decreasing value of this direction<br>
	 * the value is either <code>1</code>, <code>0</code> or <code>-1</code><br>
	 * if <code>{@link #yadd} == 0</code>, this value is not <code>0</code><br>
	 * if <code>{@link #yadd} != 0</code>, this value is <code>0</code><br>
	 */
	public final int xadd;
	/**
	 * the y increasing/decreasing value of this direction<br>
	 * the value is either <code>1</code>, <code>0</code> or <code>-1</code><br>
	 * if <code>{@link #xadd} == 0</code>, this value is not <code>0</code><br>
	 * if <code>{@link #xadd} != 0</code>, this value is <code>0</code><br>
	 */
	public final int yadd;
	
	private Direction(int xadd, int yadd) { this.xadd = xadd; this.yadd = yadd; }
	
}
