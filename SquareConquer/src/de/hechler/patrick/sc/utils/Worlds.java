package de.hechler.patrick.sc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.UnmovableEntity;
import de.hechler.patrick.sc.objects.AbsoluteManipulablePosition;
import de.hechler.patrick.sc.objects.FieldImpl;
import de.hechler.patrick.sc.objects.HouseBuilding;
import de.hechler.patrick.sc.objects.ProducingBuilding;
import de.hechler.patrick.sc.objects.StorageBuilding;
import de.hechler.patrick.sc.objects.World;

public class Worlds {
	
	private static final String KW_FIELD                 = "field";
	private static final String KW_GROUND                = "ground";
	private static final String KW_TRUE                  = "true";
	private static final String KW_FALSE                 = "false";
	private static final String KW_NULL                  = "null";
	private static final String KW_ENTITY                = "entity";
	private static final String KW_ENTITY_CAN_EXSIST_ON  = "canExsistOn";
	private static final String KW_ENTITY_MOVABLE        = "movable";
	private static final String KW_ENTITY_TYPE           = "type";
	private static final String KW_ENTITY_ACTIONS_REMAIN = "remain";
	private static final String KW_ENTITY_ACTIONS        = "actions";
	private static final String KW_ENTITY_ACTIONS_TOTAL  = "total";
	private static final String KW_ENTITY_CONTAINS       = "contains";
	private static final String KW_ENTITY_INTERVAL       = "interval";
	private static final String KW_ENTITY_PRODUCING      = "producingcount";
	private static final String KW_ENTITY_TURN_CNT       = "turncount";
	private static final String KW_ENTITY_CAPACITY       = "capacity";
	
	public static World createFromPlan(InputStream in, Charset charset) throws IOException {
		return createFromPlan(new Scanner(in, charset));
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
			String save = sc.next();
			if ( !"SMALL_SAVE".equalsIgnoreCase(save)) throw new RuntimeException("WRONG SAVE: expected 'SMALL_SAVE', but got '" + save + "'");
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
		out.println("SMALL_SAVE");
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
	
	public static void loadHugeSave(InputStream in, Charset charset) {
		try (Scanner sc = new Scanner(in, charset)) {
			loadHugeSave(sc);
		}
	}
	
	public static World loadHugeSave(Scanner sc) {
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
				String key = sc.next();
				Grounds ground = null;
				Entity e = null;
				switch (key.toLowerCase()) {
				case KW_FIELD:
					for (int i = 0; i < 2; i ++ ) {
						key = sc.next();
						boolean uedp = false;
						if (key.endsWith(":")) {
							uedp = true;
							key = key.substring(0, key.length() - 1);
						}
						switch (key.toLowerCase()) {
						case KW_GROUND: {
							if ("=".equals(sc.next())) throw new InputMismatchException("expected '='");
							ground = grounds.get(sc.next());
							break;
						}
						case KW_ENTITY:
							key = sc.next();
							if ("=".equals(key)) {
								key = sc.next();
								if (KW_NULL.equalsIgnoreCase(key)) {
									if (e != null) {
										throw new InputMismatchException("dobble entity write");
									}
								}
							}
							if (uedp) {
								uedp = false;
								Boolean movable = null;
								Integer actions = null;
								Integer capacity = null;
								Integer interval = null;
								Integer turnCnt = null;
								Set <Grounds> ceo = null;
								Set <MovableEntity> containsME = null;
								Map <Resources, Integer> containsR = null;
								Type type = null;
								Resources producing = null;
								while (condition(movable, actions, capacity, interval, turnCnt, containsME, containsR, type, producing, ceo)) {
									key = sc.next();
									if (key.endsWith(":")) {
										uedp = true;
										key = key.substring(0, key.length() - 1);
									}
									switch (key.toLowerCase()) {
									case KW_ENTITY_MOVABLE:
										if (movable != null) throw new InputMismatchException("double write of movable");
										key = sc.next();
										if ( !"=".equals(key)) throw new InputMismatchException("expected '=' unexpected key is: '" + key + "'");
										key = sc.next();
										switch (key.toLowerCase()) {
										case KW_FALSE:
											movable = false;
											break;
										case KW_TRUE:
											movable = true;
											break;
										default:
											throw new InputMismatchException("expected booleanKey but got: '" + key + "'");
										}
										break;
									case KW_ENTITY_ACTIONS:
										// TODO make
										break;
									case KW_ENTITY_CAN_EXSIST_ON:
										// TODO make
										break;
									case KW_ENTITY_CAPACITY:
										// TODO make
										break;
									case KW_ENTITY_CONTAINS:
										// TODO make
										break;
									case KW_ENTITY_INTERVAL:
										// TODO make
										break;
									case KW_ENTITY_PRODUCING:
										// TODO make
										break;
									case KW_ENTITY_TURN_CNT:
										// TODO make
										break;
									case KW_ENTITY_TYPE:
										// TODO make
										break;
									default:
										throw new InputMismatchException("unexpected key: '" + key + "' inside of entityKey");
									}
									if (uedp) throw new InputMismatchException("unexpected ':' after key: '" + key + "' inside of entityKey");
								}
							}
						default:
							throw new RuntimeException("unknown key: '" + key + "'");
						}
						if (uedp) throw new InputMismatchException("unexpected ':' after key: '" + key + "'");
					}
				default:
					throw new RuntimeException("unknown key: '" + key + "'");
				}
			}
		}
		return map;
	}
	
	private static boolean condition(Boolean movable, Integer actions, Integer capacity, Integer interval, Integer turnCnt, Set <MovableEntity> containsME, Map <Resources, Integer> containsR, Type type, Resources producing,
			Set <Grounds> ceo) {
		if (movable == null || actions == null || type == null || ceo == null) return true;
		switch (type) {
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
			return containsME != null;
		case farm:
		case mine:
		case spring:
			return producing != null && turnCnt != null && interval != null && capacity != null && containsR != null;
		case storage:
			return capacity != null && containsR != null;
		default:
			break;
		}
		return false;
	}
	
	public static void saveHuge(PrintStream out, World map) {
		int xCnt = map.getXCnt();
		int yCnt = map.getYCnt();
		out.println("MAP (" + xCnt + "|" + yCnt + ")");
		out.println("HUGE_SAVE");
		AbsoluteManipulablePosition acp = new AbsoluteManipulablePosition(0, 0);
		for (; acp.y < yCnt; acp.y ++ ) {
			for (acp.x = 0; acp.x < xCnt; acp.x ++ ) {
				Field f = map.getField(acp);
				out.println(KW_FIELD + "=" + f.position().toPosStr());
				out.println("    " + KW_GROUND + " = " + f.ground().name());
				Entity e = f.getEntity();
				if (e == null) {
					out.println("    " + KW_ENTITY + " = " + KW_NULL);
				} else if (e.isMovable()) {
					MovableEntity me = (MovableEntity) e;
					out.println("    " + KW_ENTITY + ": " + KW_ENTITY_MOVABLE + " = " + KW_TRUE);
					out.println("          " + KW_ENTITY_TYPE + " = " + me.type().name());
					out.println("          " + KW_ENTITY_ACTIONS + ":     " + KW_ENTITY_ACTIONS_REMAIN + " = " + me.remainingActions());
					out.println("                       " + KW_ENTITY_ACTIONS_TOTAL + "  = " + me.totalActions());
					Set <Grounds> ceo = me.canExsitOn();
					out.println("          " + KW_ENTITY_CAN_EXSIST_ON + ": " + ceo.size());
					for (Grounds grnd : ceo) {
						out.println("                       " + grnd);
					}
				} else {
					UnmovableEntity ue = (UnmovableEntity) e;
					out.println("    " + KW_ENTITY + ": " + KW_ENTITY_MOVABLE + " = " + KW_FALSE);
					Type type = ue.type();
					out.println("          " + KW_ENTITY_TYPE + " = " + type.name());
					out.println("          " + KW_ENTITY_ACTIONS + ":      " + KW_ENTITY_ACTIONS_REMAIN + " = " + ue.remainingActions());
					out.println("                       " + KW_ENTITY_ACTIONS_TOTAL + "  = " + ue.totalActions());
					Set <Grounds> ceo = ue.canExsitOn();
					out.println("          " + KW_ENTITY_CAN_EXSIST_ON + ": " + ceo.size());
					for (Grounds grnd : ceo) {
						out.println("                       " + grnd);
					}
					switch (type) {
					case house:
					case houseBow:
					case houseBuilder:
					case houseMelee: {
						HouseBuilding hb = (HouseBuilding) ue;
						Set <MovableEntity> inside = hb.inside();
						out.print("          " + KW_ENTITY_CONTAINS + ": " + inside.size());
						for (MovableEntity me : inside) {
							out.println("                    " + KW_ENTITY_TYPE + " = " + me.type().name());
							out.println("                    " + KW_ENTITY_ACTIONS + ":     " + KW_ENTITY_ACTIONS_REMAIN + " = " + me.remainingActions());
							out.println("                                 " + KW_ENTITY_ACTIONS_TOTAL + "  = " + me.totalActions());
							Set <Grounds> meCeo = me.canExsitOn();
							out.println("                    " + KW_ENTITY_CAN_EXSIST_ON + ": " + meCeo.size());
							for (Grounds grnd : meCeo) {
								out.println("                                 " + grnd);
							}
						}
						break;
					}
					case farm:
					case mine:
					case spring: {
						ProducingBuilding pb = (ProducingBuilding) ue;
						out.println("          " + KW_ENTITY_INTERVAL + " = " + pb.getInterval());
						out.println("          " + KW_ENTITY_PRODUCING + " = " + pb.getProducingCnt());
						out.println("          " + KW_ENTITY_TURN_CNT + " = " + pb.getTurnCount());
					}
					case storage: {
						StorageBuilding sb = (StorageBuilding) ue;
						out.println("          " + KW_ENTITY_CAPACITY + " = " + sb.capacity());
						Map <Resources, ? extends Number> store = sb.getStore();
						store.forEach((resoure, count) -> {
							out.println("                    " + KW_ENTITY_CONTAINS + " " + resoure + ": " + count.intValue());
						});
						break;
					}
					default:
						break;
					}
				}
			}
		}
	}
	
}
