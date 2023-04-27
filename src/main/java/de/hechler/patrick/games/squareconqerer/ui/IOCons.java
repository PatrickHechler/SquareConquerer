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

import java.io.PrintStream;
import java.util.Scanner;

public class IOCons implements Cons {
	
	private final Scanner     in;
	private final PrintStream out;
	
	public IOCons(Scanner in, PrintStream out) { this.in = in; this.out = out; }
	
	@Override
	public char[] readPassword() {
		return readLine().toCharArray();
	}
	
	@Override
	public char[] readPassword(String prompt) {
		return readLine(prompt).toCharArray();
	}
	
	@Override
	public String readLine() {
		return in.nextLine();
	}
	
	@Override
	public String readLine(String prompt) {
		out.print(prompt);
		return in.nextLine();
	}
	
	@Override
	public void writeLine(String line) {
		out.println(line);
	}
	
}
