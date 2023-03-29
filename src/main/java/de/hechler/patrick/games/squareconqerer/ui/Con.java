package de.hechler.patrick.games.squareconqerer.ui;


public interface Con {
	
	char[] readPassword();
	
	char[] readPassword(String prompt);

	String readLine();
	
	String readLine(String prompt);
	
	void writeLine(String line);
	
}
