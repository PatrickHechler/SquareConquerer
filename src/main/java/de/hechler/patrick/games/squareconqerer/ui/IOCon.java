package de.hechler.patrick.games.squareconqerer.ui;

import java.io.PrintStream;
import java.util.Scanner;

public class IOCon implements Con {
	
	private final Scanner     in;
	private final PrintStream out;
	
	public IOCon(Scanner in, PrintStream out) { this.in = in; this.out = out; }
	
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
