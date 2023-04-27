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
package de.hechler.patrick.games.squareconqerer.ui;

import java.io.Console;

public class ConsoleCons implements Cons {
	
	private final Console c;
	
	private final Object[] singleVarArg = new Object[1];
	
	public ConsoleCons(Console c) { this.c = c; }
	
	@Override
	public char[] readPassword(String prompt) {
		singleVarArg[0] = prompt;
		return c.readPassword("%s", singleVarArg);
	}
	
	@Override
	public char[] readPassword() {
		return c.readPassword();
	}
	
	@Override
	public String readLine(String prompt) {
		singleVarArg[0] = prompt;
		return c.readLine("%s", singleVarArg);
	}
	
	@Override
	public String readLine() {
		return c.readLine();
	}
	
	@Override
	public void writeLine(String line) {
		singleVarArg[0] = line;
		c.format("%s%n", singleVarArg);
	}
	
}
