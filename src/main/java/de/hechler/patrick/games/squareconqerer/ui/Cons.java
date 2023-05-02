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


public interface Cons {
	
	char[] readPassword();
	
	char[] readPassword(String prompt);
	
	String readLine();
	
	String readLine(String prompt);
	
	void writeLine(String line);
	
}

class Test {
	private int name;
	
	int getName() {
		System.out.println("getName");
		return name;
	}
	
	int name() {
		System.out.println("name");
		return name;
	}
}
