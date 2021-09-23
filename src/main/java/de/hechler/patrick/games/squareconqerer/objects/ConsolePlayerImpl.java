package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.enums.*;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.interfaces.*;
import java.util.*;
import java.io.*;

public class ConsolePlayerImpl implements Player, Runnable {
	
	private final static Scanner scan = new Scanner(System.in);
	
	public final String name;
	public final Scanner sc;
	public final PrintStream out;
	private PlayersSquare ms;
	private volatile boolean onTurn = false;
	private volatile Turn turn = null;
	
	private ConsolePlayerImpl(String name, Scanner sc, PrintStream out) {
		this.name = name;
		this.sc = sc;
		this.out = out;
	}
	
	public static ConsolePlayerImpl create() {
		return create(ConsolePlayerImpl.scan, System.out);
	}
	
	public static ConsolePlayerImpl create(Scanner sc, PrintStream out) {
		out.print("enter your name: ");
		String name = ConsolePlayerImpl.scan.nextLine();
		out.println("your name is '" + name + "'");
		out.println(name + " wait until it is your turn.");
		return new ConsolePlayerImpl(name, scan, out);
	}
	
	
	
	@Override
	public void setMySquare(PlayersSquare mySquare) {
		this.ms = mySquare;
	}
	
	@Override
	public PlayersSquare getMySquare() {
		return this.ms;
	}
	
	@Override
	public void startTurn() {
		onTurn = true;
		new Thread(this).start();
	}
	
	@Override
	public boolean isOnTurn() {
		return onTurn;
	}
	
	@Override
	public Object getNotify() {
		return this;
	}
	
	@Override
	public void invalidTurn(TurnExecutionException tee) {
		out.println("this was an invalid turn: " + tee.getMessage());
	}
	
	@Override
	public void run() {
		out.println("it is your turn: " + name);
		out.print(ms.squareString());
		help();
		this.turn = null;
		Turn t = new Turn(this);
		while (true) {
			try {
				String cmd = sc.next();
				switch (cmd.toLowerCase()) {
				case "square":
					out.print(ms.squareString());
					break;
				case "end":
					ms.isValid(t);
					synchronized (this) {
						this.onTurn = false;
						this.turn = t.makeTurn();
						this.notifyAll();
					}
					return;
				case "help":
					help();
					break;
				case "actions": {
					List <Action> acts = t.getActions();
					for (int i = 0; i < acts.size(); i ++ ) {
						out.println("act[" + i + "]=" + acts.get(i));
					}
					break;
				}
				case "remact": {
					String str = sc.next();
					try {
						int num = Integer.parseInt(str);
						t.remove(num);
					} catch (Exception e) {
						out.println("number err: " + e.getMessage());
					}
					break;
				}
				case "move": {
					try {
						String str = sc.next();
						int x = Integer.parseInt(str);
						str = sc.next();
						int y = Integer.parseInt(str);
						Tile tile = ms.getTile(x, y);
						Entety u = tile.getUnit();
						str = sc.next();
						if (u == null) {
							out.println("there is no unit (x=" + x + ", y=" + y + ", tile=" + tile + ")");
							break;
						}
						if (u.owner() != this) {
							out.println("you do not own this unit: owner='" + u.owner() + "' you='" + name + "' unit='" + u + "'");
							break;
						}
						Direction dir = Direction.valueOf(str);
						MoveEntetyAction mov = new MoveEntetyAction(u, dir);
						t.addAction(mov);
					} catch (Exception e) {
						out.println("number err: " + e.getMessage());
					}
					break;
				}
				case "selfkill": {
					String str = sc.next();
					int x = Integer.parseInt(str);
					str = sc.next();
					int y = Integer.parseInt(str);
					Tile tile = ms.getTile(x, y);
					Entety u = tile.getUnit();
					if (u == null) {
						out.println("there is no unit (x=" + x + ", y=" + y + ", tile=" + tile + ")");
						break;
					}
					if (u.owner() != this) {
						out.println("you do not own this unit: owner='" + u.owner() + "' you='" + name + "' unit='" + u + "'");
						break;
					}
					SelfKillEntetyAction selfkill = new SelfKillEntetyAction(u);
					t.addAction(selfkill);
					break;
				}
				case "use": {
					String str = sc.next();
					int x = Integer.parseInt(str);
					str = sc.next();
					int y = Integer.parseInt(str);
					Tile tile = ms.getTile(x, y);
					Entety u = tile.getUnit();
					Building b = tile.getBuild();
					if (b == null) {
						out.println("there is no building (x=" + x + ", y=" + y + ", tile=" + tile + ")");
						break;
					}
					if (u == null) {
						out.println("there is no unit (x=" + x + ", y=" + y + ", tile=" + tile + ")");
						break;
					}
					if (u.owner() != this) {
						out.println("you do not own this unit: owner='" + u.owner() + "' you='" + name + "' unit='" + u + "'");
						break;
					}
					if (!b.usable(u)) {
						out.println("the building says, that it is not usable under the current conditions or with the given entety!");
						out.println("I will ignore this, because the conditions may change when the other actions are executed.");
						out.println("note, that the first actions also get as fist executed (fist in first out).");
						out.println("you should check your turn using 'valid'!");
						out.println("if you don't want to do this you can remove this action using 'actions' and 'remact'.");
					}
					UsingEntetyAction act = new UsingEntetyAction(u);
					t.addAction(act);
					break;
				}
				case "build": {
					String str = sc.next();
					int x = Integer.parseInt(str);
					str = sc.next();
					int y = Integer.parseInt(str);
					Tile tile = ms.getTile(x, y);
					Entety u = tile.getUnit();
					str = sc.next();
					if (u == null) {
						out.println("there is no unit (x=" + x + ", y=" + y + ", tile=" + tile + ")");
						break;
					}
					if (u.owner() != this) {
						out.println("you do not own this unit: owner='" + u.owner() + "' you='" + name + "' unit='" + u + "'");
						break;
					}
					BuildingFactory build = BuildingFactory.valueOf(str.toLowerCase());
					BuildingEntetyAction act = new BuildingEntetyAction(u, build);
					t.addAction(act);
					break;
				}
				case "act": {
					String str = sc.next();
					int x = Integer.parseInt(str);
					str = sc.next();
					int y = Integer.parseInt(str);
					Tile tile = ms.getTile(x, y);
					Building u = tile.getBuild();
					if (u == null) {
						out.println("there is no building (x=" + x + ", y=" + y + ", tile=" + tile + ")");
						break;
					}
					if (!u.actable(tile)) {
						out.println("the building says, that it is not actable under the current conditions!");
						out.println("I will ignore this, because the conditions may change when the other actions are executed.");
						out.println("note, that the first actions also get as fist executed (fist in first out).");
						out.println("you should check your turn using 'valid'!");
						out.println("if you don't want to do this you can remove this action using 'actions' and 'remact'.");
					}
					Action act = new ActingBuildingAction(u, x, y);
					t.addAction(act);
					break;
				}
				case "buildable": {
					out.println("buildable buildings are:");
					for (BuildingFactory bf : BuildingFactory.values()) {
						out.println(bf.name());
					}
					break;
				}
				case "valid": {
					TurnExecutionException exep = ms.isValid(t);
					if (exep == null) {
						out.println("the turn is valid!");
					} else {
						out.println("this turn is not valid: " + exep.getMessage());
					}
					break;
				}
				default:
					out.println("unknown command: " + cmd);
					help();
					break;
				}
			} catch (RuntimeException e) {
				out.println("suprising exception: " + e);
				e.printStackTrace();
				out.println("I will ignore the old command and continue with new commands");
			}
		}
	}
	
	@Override
	public Turn getTurn() {
		if (this.onTurn || (this.turn == null)) {
			throw new IllegalStateException("onTurn=" + onTurn + " turn=" + turn);
		}
		Turn t = this.turn;
		this.turn = null;
		return t;
	}
	
	private void help() {
		out.println("cmds: ");
		out.println("\t'help' to print this");
		out.println("\t'end' to end the turn");
		out.println("\t'valid' to check if the current turn is valid (without executing it)");
		out.println("\t'square' to print the square");
		out.println("\t'actions' to print the actions");
		out.println("\t'remact' [ACTION-NUMBER] to remove the action with the given number (get by 'actions')");
		out.println("\t'move' [X-COORDINATE] [Y-COORDINATE] [xup | xdown | yup | ydown] to move the entety of the coordinates to the given direction");
		out.println("\t'use' [X-COORDINATE] [Y-COORDINATE] to use with the entety of the given coordinates the building at the same tile");
		out.println("\t'build' [X-COORDINATE] [Y-COORDINATE] [BUILDING_NAME] to build with the entety of the given coordinates a building at the same tile");
		out.println("\t'act' [X-COORDINATE] [Y-COORDINATE] to let a building act");
		out.println("\t\t(the building will be created, but most buildings will need to be finished with the 'use' operation)");
		out.println("\t'buildable' to print a list of all buildable buildings");
		out.println("\t'selfkill' [X-COORDINATE] [Y-COORDINATE] to kill the entety of the coordinates. (only works on your own entetis)");
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
