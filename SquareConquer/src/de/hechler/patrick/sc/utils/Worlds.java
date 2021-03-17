package de.hechler.patrick.sc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Scanner;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.UnmovableEntity;
import de.hechler.patrick.sc.objects.AbsoluteManipulablePosition;
import de.hechler.patrick.sc.objects.FieldImpl;
import de.hechler.patrick.sc.objects.World;

public class Worlds {
	
	public static World createFromPlan(InputStream in, Charset charset) throws IOException {
		try (Scanner sc = new Scanner(in, charset)) {
			return createFromPlan(sc);
		}
	}
	
	private static World createFromPlan(Scanner sc) {
		World map;
		final int xCnt, yCnt;
		{
			String head = sc.nextLine();
			String number = head.replaceFirst("^\\sMAP\\s\\(\\s([0-9]+)\\s,\\s[0-9]+\\s\\)\\s$", "§1");
			xCnt = Integer.parseInt(number);
			number = head.replaceFirst("^\\sMAP\\s\\(\\s[0-9]+\\s,\\s([0-9]+)\\s\\)\\s$", "§1");
			yCnt = Integer.parseInt(number);
			map = new World(xCnt, yCnt);
		}
		AbsoluteManipulablePosition acp = new AbsoluteManipulablePosition(0, 0);
		for (; acp.y < yCnt; acp.y ++ ) {
			char[] chars = sc.nextLine().replaceAll("\\s", "").toCharArray();
			for (acp.x = 0; acp.x < xCnt; acp.x ++ ) {
				Grounds ground = Grounds.fromCharacter(chars[acp.x]);
				Field f = new FieldImpl(acp, ground);
				map.overrideField(f);
			}
		}
		return map;
	}
	
	public static void saveToPlan(OutputStream out, Charset charset, World map) throws IOException {
		try (PrintStream out2 = new PrintStream(out, false, charset)) {
			saveToPlan(out2, map);
			out2.flush();
		}
	}
	
	public static void saveToPlan(PrintStream out, World map) throws IOException {
		int xCnt = map.getXCnt();
		int yCnt = map.getYCnt();
		out.println("MAP(" + xCnt + "|" + yCnt + ")");
		AbsoluteManipulablePosition acp = new AbsoluteManipulablePosition(0, 0);
		for (; acp.y < yCnt; acp.y ++ ) {
			char[] chars = new char[xCnt];
			for (acp.x = 0; acp.x < xCnt; acp.x ++ ) {
				Field f = map.getField(acp);
				chars[acp.x] = f.ground().character();
			}
			out.println(new String(chars));
		}
	}
	
	public static void load(InputStream in, Charset charset) {
		try (Scanner sc = new Scanner(in, charset)) {
			load(sc);
		}
	}
	
	public static World load(Scanner sc) {
		World map;
		final int xCnt, yCnt;
		{
			String head = sc.nextLine();
			String number = head.replaceFirst("^\\sMAP\\s\\(\\s([0-9]+)\\s,\\s[0-9]+\\s\\)\\s$", "§1");
			xCnt = Integer.parseInt(number);
			number = head.replaceFirst("^\\sMAP\\s\\(\\s[0-9]+\\s,\\s([0-9]+)\\s\\)\\s$", "§1");
			yCnt = Integer.parseInt(number);
			map = new World(xCnt, yCnt);
		}
		Map <String, Grounds> grounds = Grounds.names();
		for (int y = 0; y < yCnt; y ++ ) {
			for (int x = 0; x < xCnt; x ++ ) {
				// TODO make
			}
		}
		return map;
	}
	
	public static void save(PrintStream out, World map) {
		int xCnt = map.getXCnt();
		int yCnt = map.getYCnt();
		out.println("MAP (" + xCnt + "|" + yCnt + ")");
		AbsoluteManipulablePosition acp = new AbsoluteManipulablePosition(0, 0);
		for (; acp.y < yCnt; acp.y ++ ) {
			for (acp.x = 0; acp.x < xCnt; acp.x ++ ) {
				Field f = map.getField(acp);
				out.println("FIELD" + f.position().toPosStr());
				out.println("    ground = " + f.ground().name());
				Entity e = f.getEntity();
				if (e == null) {
					out.println("  entity = null");
				} else if (e.isMovable()) {
					MovableEntity me = (MovableEntity) e;
					out.println("  entity: movable = true");
					out.println("          type = " + e.type().name());
					//TODO make
					
				} else {
					UnmovableEntity ue = (UnmovableEntity) e;
					out.println("  entity: movable = false");
					out.println("          type = " + e.type().name());
					//TODO make
					
				}
			}
		}
	}
	
}
