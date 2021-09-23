package de.hechler.patrick.games.squareconqerer;

import de.hechler.patrick.games.squareconqerer.objects.ConsolePlayerImpl;
import de.hechler.patrick.games.squareconqerer.objects.Controller;

public class Main {
	
	public static void main(String[] args) {
		Controller c = new Controller(5, 5);
		c.addPlayer(ConsolePlayerImpl.create());
		c.addPlayer(ConsolePlayerImpl.create());
		c.start();
	}
	
}
