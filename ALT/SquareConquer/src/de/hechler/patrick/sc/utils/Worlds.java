package de.hechler.patrick.sc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashSet;
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
import de.hechler.patrick.sc.objects.AbsoluteMegaManipulablePosition;
import de.hechler.patrick.sc.objects.FieldImpl;
import de.hechler.patrick.sc.objects.HouseBuilding;
import de.hechler.patrick.sc.objects.ProducingBuilding;
import de.hechler.patrick.sc.objects.StorageBuilding;
import de.hechler.patrick.sc.objects.World;
import de.hechler.patrick.sc.utils.objects.EnumSet;

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
	private static final String KW_ENTITY_PRODUCING_CNT  = "producingcount";
	private static final String KW_ENTITY_TURN_CNT       = "turncount";
	private static final String KW_ENTITY_CAPACITY       = "capacity";
	private static final String KW_ENTITY_INSIDE_START   = "inside";
	
	public static World createFromPlan(InputStream in, Charset charset) throws IOException {
		return createFromPlan(new Scanner(in, charset));
	}
	
	private static World createFromPlan(Scanner sc) {
		World map;
		final int xCnt, yCnt;
		{
			String head = sc.nextLine();
			String number = head.replaceFirst("^\\sMAP\\s\\(\\s([0-9]+)\\s,\\s[0-9]+\\s\\)\\s$", "$1");
			xCnt = Integer.parseInt(number);
			number = head.replaceFirst("^\\sMAP\\s\\(\\s[0-9]+\\s,\\s([0-9]+)\\s\\)\\s$", "$1");
			yCnt = Integer.parseInt(number);
			map = new World(xCnt, yCnt);
			String save = sc.next();
			if ( !"SMALL_SAVE".equalsIgnoreCase(save)) throw new RuntimeException("WRONG SAVE: expected 'SMALL_SAVE', but got '" + save + "'");
		}
		AbsoluteMegaManipulablePosition acp = new AbsoluteMegaManipulablePosition(0, 0);
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
		AbsoluteMegaManipulablePosition acp = new AbsoluteMegaManipulablePosition(0, 0);
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
		World world;
		final int xCnt, yCnt;
		{
			String head = sc.nextLine();
			String number = head.replaceFirst("^\\sMAP\\s\\(\\s([0-9]+)\\s,\\s[0-9]+\\s\\)\\s$", "$1");
			xCnt = Integer.parseInt(number);
			number = head.replaceFirst("^\\sMAP\\s\\(\\s[0-9]+\\s,\\s([0-9]+)\\s\\)\\s$", "$1");
			yCnt = Integer.parseInt(number);
			world = new World(xCnt, yCnt);
		}
		Map <String, Grounds> grounds = Grounds.names();
		Map <String, Type> types = Type.names();
		AbsoluteMegaManipulablePosition amp = new AbsoluteMegaManipulablePosition( -1, -1);
		for (int y = 0; y < yCnt; y ++ ) {
			for (int x = 0; x < xCnt; x ++ ) {
				amp.x = -1;
				amp.y = -1;
				String key = sc.next();
				Grounds ground = null;
				Entity e = null;
				switch (key.toLowerCase()) {
				case KW_FIELD:
					if ( !"=".equals(sc.next())) throw new InputMismatchException("expected '=' after FieldKeyWord"); {
					String zw = sc.next();
					amp.x = Integer.parseInt(zw.replaceFirst("^[ \t]*\\([ \\t]*([0-9]+)[ \\t]*\\|[ \\t]*([0-9]+)[ \\t]*\\)[ \\t]*\\)$", "$1"));
					amp.y = Integer.parseInt(zw.replaceFirst("^[ \t]*\\([ \\t]*([0-9]+)[ \\t]*\\|[ \\t]*([0-9]+)[ \\t]*\\)[ \\t]*\\)$", "$2"));
				}
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
								Integer actionsRemain = null;
								Integer actionsTotal = null;
								Integer capacity = null;
								Integer interval = null;
								Integer turnCnt = null;
								Integer producingCnt = null;
								Set <Grounds> ceo = null;
								Set <MovableEntity> containsME = null;
								Map <Resources, Integer> containsR = null;
								Type type = null;
								Resources producing = null;
								while (condition(movable, actionsRemain, actionsTotal, capacity, interval, turnCnt, containsME, containsR, type, producing, ceo, producingCnt)) {
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
										if (uedp) uedp = false;
										key = sc.next();
										if ( !"=".equals(sc.next())) throw new InputMismatchException("unexpected key after entity Actinos, expected '='");
										switch (key) {
										case KW_ENTITY_ACTIONS_REMAIN:
											actionsRemain = sc.nextInt();
											break;
										case KW_ENTITY_ACTIONS_TOTAL:
											actionsTotal = sc.nextInt();
											break;
										default:
											throw new InputMismatchException("unexpected key: '" + key + "'");
										}
										break;
									case KW_ENTITY_CAN_EXSIST_ON:
										ceo = new EnumSet <Grounds>(Grounds.class);
										if (uedp) uedp = false;
										for (int ceoI = sc.nextInt(); ceoI > 0; ceoI -- ) {
											ceo.add(grounds.get(sc.next()));
										}
										break;
									case KW_ENTITY_CAPACITY:
										key = sc.next();
										if ( !"=".equals(key)) throw new InputMismatchException("expected '=', but got: '" + key + "'");
										capacity = sc.nextInt();
										break;
									case KW_ENTITY_CONTAINS:
										if (uedp) uedp = false;
										containsME = new HashSet <MovableEntity>();
										for (int insideI = sc.nextInt(); insideI > 0; insideI -- ) {
											Type t = null;
											Integer remActions = null;
											Integer totalActions = null;
											Set <Grounds> canExsistOn = null;
											while (t == null || remActions == null || totalActions == null || canExsistOn == null) {
												key = sc.next();
												if (key.endsWith(":")) {
													uedp = true;
													key = key.substring(0, key.length() - 1);
												}
												switch (key) {
												case KW_ENTITY_TYPE:
													if (t != null) throw new InputMismatchException("double write of type is forbidden!");
													if ( !"=".equals(sc.next())) throw new InputMismatchException("expected '='");
													t = types.get(sc.next());
													break;
												case KW_ENTITY_ACTIONS:
													if (uedp) uedp = false;
													key = sc.next();
													if ( !"=".equals(sc.next())) throw new InputMismatchException("unexpected key after entity Actinos, expected '='");
													switch (key) {
													case KW_ENTITY_ACTIONS_REMAIN:
														actionsRemain = sc.nextInt();
														break;
													case KW_ENTITY_ACTIONS_TOTAL:
														actionsTotal = sc.nextInt();
														break;
													default:
														throw new InputMismatchException("unexpected key: '" + key + "'");
													}
													break;
												case KW_ENTITY_CAN_EXSIST_ON:
													if (uedp) uedp = false;
													canExsistOn = new EnumSet <Grounds>(Grounds.class);
													for (int insideCeoI = sc.nextInt(); insideCeoI > 0; insideCeoI -- ) {
														canExsistOn.add(grounds.get(sc.next()));
													}
												default:
													throw new InputMismatchException("unexpected key: '" + key + "' inside of entityContains");
												}
											}
										}
										break;
									case KW_ENTITY_INTERVAL:
										if ( !"=".equals(sc.next())) throw new InputMismatchException("expected '=' after intervaelKW");
										interval = sc.nextInt();
										break;
									case KW_ENTITY_PRODUCING_CNT:
										if ( !"=".equals(sc.next())) throw new InputMismatchException("expected '=' after producingCntKW");
										producingCnt = sc.nextInt();
										break;
									case KW_ENTITY_TURN_CNT:
										if ( !"=".equals(sc.next())) throw new InputMismatchException("expected '=' after producingCntKW");
										turnCnt = sc.nextInt();
										break;
									case KW_ENTITY_TYPE:
										if ( !"=".equals(sc.next())) throw new InputMismatchException("expected '=' after producingCntKW");
										type = types.get(sc.next());
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
					break;
				default:
					throw new RuntimeException("unknown key: '" + key + "'");
				}
				FieldImpl field = new FieldImpl(amp, ground);
				world.overrideField(field);
			}
		}
		return world;
	}
	
	private static boolean condition(Boolean movable, Integer actions, Integer actions2, Integer capacity, Integer interval, Integer turnCnt, Set <MovableEntity> containsME, Map <Resources, Integer> containsR, Type type,
			Resources producing, Set <Grounds> ceo, Integer producingCnt) {
		if (movable == null || actions == null || actions2 == null || type == null || ceo == null) return true;
		switch (type) {
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
			return containsME != null;
		case farm:
		case mine:
		case spring:
			return producing != null && turnCnt != null && interval != null && capacity != null && containsR != null && producingCnt != null;
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
		AbsoluteMegaManipulablePosition acp = new AbsoluteMegaManipulablePosition(0, 0);
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
						out.println("          " + KW_ENTITY_PRODUCING_CNT + " = " + pb.getProducingCnt());
						out.println("          " + KW_ENTITY_TURN_CNT + " = " + pb.getTurnCount());
					}
					case storage: {
						StorageBuilding sb = (StorageBuilding) ue;
						out.println("          " + KW_ENTITY_CAPACITY + " = " + sb.capacity());
						Map <Resources, ? extends Number> store = sb.getStore();
						out.println("          " + KW_ENTITY_INSIDE_START);
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
	
	public void print(World world, PrintStream out) {
		final int xc = world.getXCnt(), yc = world.getYCnt();
		for (AbsoluteMegaManipulablePosition pos = new AbsoluteMegaManipulablePosition(0, 0); pos.y < yc; pos.y ++ ) {
			StringBuilder str = new StringBuilder(xc);
			for (pos.x = 0; pos.x < xc; pos.x ++ ) {
				Field f = world.getField(pos);
				if (f.ground() == Grounds.unknown) {
					str.append('U');
					continue;
				}
				Entity e = f.getEntity();
				if (e == null) {
					str.append(f.ground().character());
					continue;
				}
				str.append(e.type().name().toLowerCase().charAt(0));
			}
			out.println(str);
		}
	}
	
}
