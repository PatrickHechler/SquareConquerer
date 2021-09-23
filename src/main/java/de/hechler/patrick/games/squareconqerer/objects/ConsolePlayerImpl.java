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
				List <MoveUnitAction> acts = t.getActions();
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
					Unit u = tile.getUnit();
					str = sc.next();
					if (u == null) {
						out.println("there is no unit (x=" + x + ", y=" + y + ", tile=" + tile + ")");
						break;
					}
					if (u.owner != this) {
						out.println("you do not own this unit: owner='" + u.owner + "' you='" + name + "' unit='" + u + "'");
						break;
					}
					Direction dir = Direction.forName(str);
					MoveUnitAction mov = new MoveUnitAction(dir, u);
					t.addAction(mov);
				} catch (Exception e) {
					out.println("number err: " + e.getMessage());
				}
				break;
			}
			case "valid": {
				TurnExecutionException exep = ms.isValid(t);
				if (exep == null) {
					out.println("the turn is valid!");
				} else {
					out.println("this turn is not valid:" + exep.getMessage());
				}
				break;
			}
			default:
				out.println("unknown command: " + cmd);
				help();
				break;
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
		out.println("\t'move' [X-COORDINATE] [Y-COORDINATE] [xup | xdown | yup | ydown] to move the unit of the coordinates to the given direction");
		// TODO out.println("\t'build' [X-COORDINATE] [Y-COORDINATE] to build with the unit on the coordinates a building");
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
