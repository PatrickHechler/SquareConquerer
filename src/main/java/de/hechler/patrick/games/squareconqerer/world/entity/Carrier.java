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
package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public final class Carrier extends UnitImpl {
	
	public static final String NAME   = "Carrier";
	public static final int    NUMBER = 0x925D9B86;
	
	private static final int MAX_LIVES  = 3;
	private static final int VIEW_RANGE = 4;
	private static final int MAX_CARRY  = 5;
	
	private static final int ORIDINAL_BASE_VALUE = 1;
	private static int       oridinal;
	
	public Carrier(int x, int y, User usr) {
		super(x, y, usr, MAX_LIVES, VIEW_RANGE, MAX_CARRY);
	}
	
	public Carrier(int x, int y, User usr, int lives, Resource res, int carryAmount) {
		super(x, y, usr, MAX_LIVES, lives, VIEW_RANGE, MAX_CARRY, carryAmount, res);
	}
	
	@Override
	public int moveRange() {
		return 3 - (super.carryAmount >>> 1);
	}
	
	@Override
	public Carrier copy() {
		return new Carrier(super.x, super.y, owner(), lives(), super.carryResource, super.carryAmount);
	}
	
	@Override
	public int ordinal() {
		if (oridinal == 0) {
			oridinal = ORIDINAL_BASE_VALUE + SquareConquererAddon.theGame().oridinalOffsetUnit();
		}
		return oridinal;
	}
	
}
