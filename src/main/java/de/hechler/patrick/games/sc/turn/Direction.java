// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.sc.turn;


@SuppressWarnings("javadoc")
public enum Direction implements MoveAct {
	
	X_ADD(1, 0), Y_ADD(0, 1), X_SUB(-1, 0), Y_SUB(0, -1);
	
	public final int xadd;
	public final int yadd;
	
	private Direction(int xadd, int yadd) { this.xadd = xadd; this.yadd = yadd; }
	
	public static Direction of(int ordinal) {
		switch (ordinal) {
		case 0:
			return X_ADD;
		case 1:
			return Y_ADD;
		case 2:
			return X_SUB;
		case 3:
			return Y_SUB;
		}
		throw new IllegalArgumentException("there is no Direction with the ordinal " + ordinal);
	}
	
	public static Direction of(int xadd, int yadd) {
		switch (xadd) {
		case 0:
			switch (yadd) {
			case 1:
				return Y_ADD;
			case -1:
				return Y_SUB;
			default:
				throw new IllegalArgumentException("xadd: " + xadd + " yadd: " + yadd);
			}
		case 1:
			if (yadd != 0) throw new IllegalArgumentException("xadd: " + xadd + " yadd: " + yadd);
			return X_ADD;
		case -1:
			if (yadd != 0) throw new IllegalArgumentException("xadd: " + xadd + " yadd: " + yadd);
			return X_SUB;
		default:
			throw new IllegalArgumentException("xadd: " + xadd + " yadd: " + yadd);
		}
	}
	
}
