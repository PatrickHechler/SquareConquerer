package de.hechler.patrick.games.squareconqerer.ui;

import java.io.Console;

public class ConsoleCon implements Con {
	
	private final Console c;
	
	private final Object[] singleVarArg = new Object[1];
	
	public ConsoleCon(Console c) { this.c = c; }
	
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
