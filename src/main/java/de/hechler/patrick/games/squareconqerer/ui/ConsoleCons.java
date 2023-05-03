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

/**
 * this implementation of the {@link Cons} interface delegates to the {@link Console}
 * <p>
 * this class does not support Multithreading
 * 
 * @author Patrick Hechler
 */
public class ConsoleCons implements Cons {
	
	private final Console c;
	
	private final Object[] singleVarArg = new Object[1];
	
	/**
	 * create a new {@link ConsoleCons} instance
	 * 
	 * @param c the console of the instance
	 */
	public ConsoleCons(Console c) { this.c = c; }
	
	/** {@inheritDoc} */
	@Override
	public char[] readPassword(String prompt) {
		this.singleVarArg[0] = prompt;
		return this.c.readPassword("%s", this.singleVarArg); //$NON-NLS-1$
	}
	
	/** {@inheritDoc} */
	@Override
	public char[] readPassword() {
		return this.c.readPassword();
	}
	
	/** {@inheritDoc} */
	@Override
	public String readLine(String prompt) {
		this.singleVarArg[0] = prompt;
		return this.c.readLine("%s", this.singleVarArg); //$NON-NLS-1$
	}
	
	/** {@inheritDoc} */
	@Override
	public String readLine() {
		return this.c.readLine();
	}
	
	/** {@inheritDoc} */
	@Override
	public void writeLine(String line) {
		this.singleVarArg[0] = line;
		this.c.format("%s%n", this.singleVarArg); //$NON-NLS-1$
	}
	
}
