package de.hechler.patrick.sc.objects.players.botplayer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Player;
import de.hechler.patrick.sc.objects.Area;
import de.hechler.patrick.sc.objects.Building;
import de.hechler.patrick.sc.objects.HouseBuilding;
import de.hechler.patrick.sc.objects.Unit;
import de.hechler.patrick.sc.objects.World;

/**
 * <pre>
 * BOT:
 * 	\**
 * 	 * this area is owned completely by the bot
 * 	 * the bot will only built there
 * 	 * it will use the buildings inside
 * 	 * it will try to expand the borders
 * 	 *\
 * 	myArea
 * 	\**
 * 	 * this area contains all Fields, which are inside of 'myArea'
 * 	 * all Units of the Bot will be there
 * 	 * the Bot may use a Building, if it is inside of this area, but not inside of 'myArea'
 * 	 * it will protect the boarders
 * 	 *     but the Bots Units will still be inside of the area
 * 	 *     Especially the boarders, which are near to enemy boarders
 * 	 * it will try to expand the borders
 * 	 * if it can't hold the line it's Units will fall back
 * 	 *     it will eventually destroy buildings, so the enemy can not capture them
 * 	 *\
 * 	myOuterArea
 * 	myEntitys:
 * 		\**
 * 		 * here are all Units owned by the Bot
 * 		 *\
 * 		myUnits:
 * 			myFighters:
 * 				myBows
 * 				myMeeles
 * 				myWarBoats
 * 			myBoats
 * 			myCarriers
 * 			mySimples
 * 		\**
 * 		 * here are all buildings inside of 'myArea'
 * 		 * here are also all Houses, in which are Units of the Bot
 * 		 *\
 * 		myBuildings:
 * 			ports
 * 			simpleHouses
 * 			meeleHouses
 * 			bowHouses
 * 			storages
 * 			springs
 * 			farms
 * 			mines
 * 			woodFarms
 * 	\**
 * 	 * here are all buildings inside of no area
 * 	 * if a House is in no area, but inside of it are Units, it will not be here
 * 	 *\
 * 	unownedBuildings:
 * 		ports
 * 		simpleHouses
 * 		meeleHouses
 * 		bowHouses
 * 		storages
 * 		springs
 * 		farms
 * 		mines
 * 		woodFarms
 * 	\**
 * 	 * here is all the data from of the enemies, which the Bot knows
 * 	 *\
 * 	enemies:
 * 		area
 * 		myUnits:
 * 			myFighters:
 * 				myBows
 * 				myMeeles
 * 			myBoats
 * 			myCarriers
 * 			mySimples
 * 		myBuildings:
 * 			ports
 * 			simpleHouses
 * 			meeleHouses
 * 			bowHouses
 * 			storages
 * 			springs
 * 			farms
 * 			mines
 * 			woodFarms
 * 
 * </pre>
 * 
 * @author Patrick
 * @see <a href="https://github.com/PatrickHechler/SquareConquerer/blob/master/BotPlan.txt"> BotPlan</a>
 *
 */
public class BotPlayer implements Player {
	
	/**
	 * <pre>
	* this {@link Area} is owned completely by the bot
	* the bot will only built there
	* it will use the buildings inside
	* it will try to expand the borders
	 * </pre>
	 */
	private Area myArea;
	/**
	 * <pre>
	* this {@link Area} contains all Fields, which are inside of 'myArea'
	* all Units of the Bot will be there
	* the Bot may use a Building, if it is inside of this area, but not inside of 'myArea'
	* it will protect the boarders
	*     but the Bots Units will still be inside of the area
	*     Especially the boarders, which are near to enemy boarders
	* it will try to expand the borders
	* if it can't hold the line it's Units will fall back
	*     it will eventually destroy buildings, so the enemy can not capture them
	 * </pre>
	 */
	private Area myOuterArea;
	
	/**
	 * this is the bots {@link World}
	 */
	private World world;
	/**
	 * this is the bots player id
	 */
	private int   myID;
	
	/**
	 * here are all {@link Entity}s owned by the Bot
	 */
	private EntityPool           myEntitys;
	/**
	 * here are all {@link Building}s inside of no area if a {@link HouseBuilding} is in no {@link Area}, but inside of it are {@link Unit}s, it will not be here
	 */
	private BuildingPool         unownedBuildings;
	/**
	 * here is all the data from of the enemies, which the Bot knows
	 */
	private Map <Integer, Enemy> myEnemeys;
	
	private static class Enemy {
		
		Area       area;
		EntityPool units;
		
		
		private Enemy(int xCnt, int yCnt) {
			area = new Area(0, 0, xCnt, yCnt);
			units = new EntityPool();
		}
		
	}
	
	private static class EntityPool implements Set <Entity> {
		
		private final UnitPool myUnits = new UnitPool();
		
		private final BuildingPool myBuildings = new BuildingPool();
		
		private static class UnitPool implements Set <Unit> {
			
			private final FighterPool myFighters = new FighterPool();
			
			private final Set <Unit> myBoats    = new HashSet <Unit>();
			private final Set <Unit> myCarriers = new HashSet <Unit>();
			private final Set <Unit> myBuilders = new HashSet <Unit>();
			
			private final Set <Unit> mySimples = new HashSet <Unit>();
			
			private static class FighterPool implements Set <Unit> {
				
				private final Set <Unit> myBows     = new HashSet <Unit>();
				private final Set <Unit> myMeeles   = new HashSet <Unit>();
				private final Set <Unit> myWarBoats = new HashSet <Unit>();
				
				@Override
				public int size() {
					long s = (long) myBows.size() + (long) myMeeles.size() + (long) myWarBoats.size();
					return (int) Math.min(s, Integer.MAX_VALUE);
				}
				
				@Override
				public boolean isEmpty() {
					return myBows.isEmpty() && myMeeles.isEmpty() && myWarBoats.isEmpty();
				}
				
				@Override
				public boolean contains(Object o) {
					if ( ! (o instanceof Unit)) return false;
					Unit u = (Unit) o;
					switch (u.type) {
					case bow:
						return myBows.contains(u);
					case fightingBoat:
						return myWarBoats.contains(u);
					case meele:
						return myMeeles.contains(u);
					default:
						return false;
					}
				}
				
				public boolean contains(Unit u) {
					switch (u.type) {
					case bow:
						return myBows.contains(u);
					case fightingBoat:
						return myWarBoats.contains(u);
					case meele:
						return myMeeles.contains(u);
					default:
						return false;
					}
				}
				
				@Override
				public Iterator <Unit> iterator() {
					return new Iterator <Unit>() {
						
						int                       state = 0;
						Iterator <? extends Unit> i     = myBows.iterator();
						
						@Override
						public boolean hasNext() {
							boolean n = i.hasNext();
							if ( !n) switch (state) {
							case 0:
								state ++ ;
								i = myMeeles.iterator();
								n = i.hasNext();
								if (n) break;
							case 1:
								state ++ ;
								i = myWarBoats.iterator();
								n = i.hasNext();
								break;
							default:
								break;
							}
							return n;
						}
						
						@Override
						public Unit next() {
							if ( !i.hasNext()) switch (state) {
							case 0:
								state ++ ;
								i = myMeeles.iterator();
								if (i.hasNext()) break;
							case 1:
								state ++ ;
								i = myWarBoats.iterator();
								break;
							default:
								break;
							}
							return i.next();
						}
						
					};
				}
				
				@Override
				public Object[] toArray() {
					Object[] o = new Object[size()];
					int i = 0;
					for (Object n : this) {
						o[i ++ ] = n;
					}
					return o;
				}
				
				@Override
				@SuppressWarnings("unchecked")
				public <T> T[] toArray(T[] a) {
					int s = size();
					Class <?> comp = a.getClass().getComponentType();
					if (s != a.length) a = (T[]) Array.newInstance(comp, s);
					if (comp == Entity.class) {
						Entity[] o = (Entity[]) a;
						int i = 0;
						for (Entity n : this) {
							o[i ++ ] = n;
						}
					} else if (comp == Unit.class) {
						Unit[] o = (Unit[]) a;
						int i = 0;
						for (Unit n : this) {
							o[i ++ ] = n;
						}
					} else if (comp == MovableEntity.class) {
						MovableEntity[] o = (MovableEntity[]) a;
						int i = 0;
						for (Unit n : this) {
							o[i ++ ] = n;
						}
					} else if (comp == Object.class) {
						Object[] o = (Object[]) a;
						int i = 0;
						for (Unit n : this) {
							o[i ++ ] = n;
						}
					} else {
						int i = 0;
						for (Unit n : this) {
							a[i ++ ] = (T) n;
						}
					}
					return a;
				}
				
				@Override
				public boolean add(Unit e) {
					switch (e.type) {
					case meele:
						return myMeeles.add(e);
					case bow:
						return myBows.add(e);
					case fightingBoat:
						return myWarBoats.add(e);
					default:
						throw new IllegalArgumentException("illegal unit type: " + e.type.name() + " of unit: '" + e + "'");
					}
				}
				
				@Override
				public boolean remove(Object o) {
					if ( ! (o instanceof Unit)) return false;
					switch ( ((Unit) o).type) {
					case meele:
						return myMeeles.remove(o);
					case bow:
						return myBows.remove(o);
					case fightingBoat:
						return myWarBoats.remove(o);
					default:
						throw new IllegalArgumentException("illegal unit type: " + ((Unit) o).type.name() + " of unit: '" + o + "'");
					}
				}
				
				public boolean remove(Unit o) {
					switch (o.type) {
					case meele:
						return myMeeles.remove(o);
					case bow:
						return myBows.remove(o);
					case fightingBoat:
						return myWarBoats.remove(o);
					default:
						throw new IllegalArgumentException("illegal unit type: " + o.type.name() + " of unit: '" + o + "'");
					}
				}
				
				public boolean containsAll(Collection <?> c) {
					if (c instanceof FighterPool) {
						FighterPool fp = (FighterPool) c;
						if ( !myMeeles.containsAll(fp.myMeeles)) return false;
						if ( !myBows.containsAll(fp.myBows)) return false;
						return myWarBoats.containsAll(fp.myWarBoats);
					} else if (c instanceof UnitPool) {
						UnitPool up = (UnitPool) c;
						if ( !myMeeles.containsAll(up.myFighters.myMeeles)) return false;
						if ( !myBows.containsAll(up.myFighters.myBows)) return false;
						return myWarBoats.containsAll(up.myFighters.myWarBoats);
					} else if (c instanceof EntityPool) {
						EntityPool ep = (EntityPool) c;
						if ( !myMeeles.containsAll(ep.myUnits.myFighters.myMeeles)) return false;
						if ( !myBows.containsAll(ep.myUnits.myFighters.myBows)) return false;
						return myWarBoats.containsAll(ep.myUnits.myFighters.myWarBoats);
					} else {
						for (Object o : c) {
							if ( !contains(o)) return false;
						}
						return true;
					}
				}
				
				public boolean containsAll(FighterPool fp) {
					if ( !myMeeles.containsAll(fp.myMeeles)) return false;
					if ( !myBows.containsAll(fp.myBows)) return false;
					return myWarBoats.containsAll(fp.myWarBoats);
				}
				
				public boolean containsAll(UnitPool up) {
					if ( !myMeeles.containsAll(up.myFighters.myMeeles)) return false;
					if ( !myBows.containsAll(up.myFighters.myBows)) return false;
					return myWarBoats.containsAll(up.myFighters.myWarBoats);
				}
				
				public boolean containsAll(EntityPool ep) {
					if ( !myMeeles.containsAll(ep.myUnits.myFighters.myMeeles)) return false;
					if ( !myBows.containsAll(ep.myUnits.myFighters.myBows)) return false;
					return myWarBoats.containsAll(ep.myUnits.myFighters.myWarBoats);
				}
				
				@Override
				public boolean addAll(Collection <? extends Unit> c) {
					boolean res = false;
					if (c instanceof FighterPool) {
						FighterPool fp = (FighterPool) c;
						res |= myMeeles.addAll(fp.myMeeles);
						res |= myBows.addAll(fp.myBows);
						res |= myWarBoats.addAll(fp.myWarBoats);
					} else {
						for (Unit o : c) {
							res |= add(o);
						}
					}
					return res;
				}
				
				public boolean addAll(FighterPool fp) {
					boolean res = myMeeles.addAll(fp.myMeeles);
					res |= myBows.addAll(fp.myBows);
					res |= myWarBoats.addAll(fp.myWarBoats);
					return res;
				}
				
				@Override
				public boolean retainAll(Collection <?> c) {
					boolean res;
					if (c instanceof FighterPool) {
						FighterPool p = (FighterPool) c;
						res = myMeeles.retainAll(p.myMeeles);
						res |= myBows.retainAll(p.myBows);
						res |= myWarBoats.retainAll(p.myWarBoats);
					} else if (c instanceof UnitPool) {
						UnitPool p = (UnitPool) c;
						res = myMeeles.retainAll(p.myFighters.myMeeles);
						res |= myBows.retainAll(p.myFighters.myBows);
						res |= myWarBoats.retainAll(p.myFighters.myWarBoats);
					} else if (c instanceof EntityPool) {
						EntityPool p = (EntityPool) c;
						res = myMeeles.retainAll(p.myUnits.myFighters.myMeeles);
						res |= myBows.retainAll(p.myUnits.myFighters.myBows);
						res |= myWarBoats.retainAll(p.myUnits.myFighters.myWarBoats);
					} else {
						if ( ! (c instanceof Set)) c = new HashSet <>(c);
						List <Unit> b = new ArrayList <Unit>(), m = new ArrayList <Unit>(), wb = new ArrayList <>();
						for (Unit unit : myBows) {
							if ( !c.contains(unit)) b.add(unit);
						}
						res = !b.isEmpty();
						for (Unit unit : myMeeles) {
							if ( !c.contains(unit)) m.add(unit);
						}
						res |= !m.isEmpty();
						for (Unit unit : myWarBoats) {
							if ( !c.contains(unit)) wb.add(unit);
						}
						res |= !wb.isEmpty();
						b.forEach(u -> myBows.remove(u));
						m.forEach(u -> myMeeles.remove(u));
						wb.forEach(u -> myWarBoats.remove(u));
					}
					return res;
				}
				
				public boolean retainAll(FighterPool p) {
					boolean res = myMeeles.retainAll(p.myMeeles);
					res |= myBows.retainAll(p.myBows);
					return res | myWarBoats.retainAll(p.myWarBoats);
				}
				
				public boolean retainAll(UnitPool p) {
					boolean res = myMeeles.retainAll(p.myFighters.myMeeles);
					res |= myBows.retainAll(p.myFighters.myBows);
					return res | myWarBoats.retainAll(p.myFighters.myWarBoats);
				}
				
				public boolean retainAll(EntityPool p) {
					boolean res = myMeeles.retainAll(p.myUnits.myFighters.myMeeles);
					res |= myBows.retainAll(p.myUnits.myFighters.myBows);
					return res | myWarBoats.retainAll(p.myUnits.myFighters.myWarBoats);
				}
				
				@Override
				public boolean removeAll(Collection <?> c) {
					boolean res;
					if (c instanceof FighterPool) {
						FighterPool p = (FighterPool) c;
						res = myBows.removeAll(p.myBows);
						res |= myMeeles.removeAll(p.myMeeles);
						res |= myWarBoats.removeAll(p.myWarBoats);
					} else if (c instanceof UnitPool) {
						UnitPool p = (UnitPool) c;
						res = myBows.removeAll(p.myFighters.myBows);
						res |= myMeeles.removeAll(p.myFighters.myMeeles);
						res |= myWarBoats.removeAll(p.myFighters.myWarBoats);
					} else if (c instanceof EntityPool) {
						EntityPool p = (EntityPool) c;
						res = myBows.removeAll(p.myUnits.myFighters.myBows);
						res |= myMeeles.removeAll(p.myUnits.myFighters.myMeeles);
						res |= myWarBoats.removeAll(p.myUnits.myFighters.myWarBoats);
					} else {
						res = false;
						for (Object o : c) {
							res |= remove(o);
						}
					}
					return res;
				}
				
				public boolean removeAll(FighterPool p) {
					boolean res = myBows.removeAll(p.myBows);
					res |= myMeeles.removeAll(p.myMeeles);
					return res | myWarBoats.removeAll(p.myWarBoats);
				}
				
				public boolean removeAll(UnitPool p) {
					boolean res = myBows.removeAll(p.myFighters.myBows);
					res |= myMeeles.removeAll(p.myFighters.myMeeles);
					return res | myWarBoats.removeAll(p.myFighters.myWarBoats);
				}
				
				public boolean removeAll(EntityPool p) {
					boolean res = myBows.removeAll(p.myUnits.myFighters.myBows);
					res |= myMeeles.removeAll(p.myUnits.myFighters.myMeeles);
					return res | myWarBoats.removeAll(p.myUnits.myFighters.myWarBoats);
				}
				
				@Override
				public void clear() {
					myBows.clear();
					myMeeles.clear();
					myWarBoats.clear();
				}
				
			}
			
			@Override
			public int size() {
				long s = (long) myBoats.size() + (long) myCarriers.size() + (long) myFighters.size() + (long) mySimples.size() + (long) myBuilders.size();
				return (int) Math.min(s, Integer.MAX_VALUE);
			}
			
			@Override
			public boolean isEmpty() {
				return myBoats.isEmpty() && myCarriers.isEmpty() && myFighters.isEmpty() && mySimples.isEmpty() && myBuilders.isEmpty();
			}
			
			@Override
			public boolean contains(Object o) {
				if (myBoats.contains(o)) return true;
				else if (myCarriers.contains(o)) return true;
				else if (myFighters.contains(o)) return true;
				else if (myBuilders.contains(o)) return true;
				else if (mySimples.contains(o)) return true;
				else return false;
			}
			
			public boolean contains(Unit u) {
				if (myBoats.contains(u)) return true;
				else if (myCarriers.contains(u)) return true;
				else if (myFighters.contains(u)) return true;
				else if (myBuilders.contains(u)) return true;
				else if (mySimples.contains(u)) return true;
				else return false;
			}
			
			@Override
			public Iterator <Unit> iterator() {
				return new Iterator <Unit>() {
					
					int                       s = 0;
					Iterator <? extends Unit> i = mySimples.iterator();
					
					@Override
					public boolean hasNext() {
						boolean n = i.hasNext();
						if ( !n) switch (s) {
						case 0:
							s ++ ;
							i = myCarriers.iterator();
							n = i.hasNext();
							if (n) break;
						case 1:
							s ++ ;
							i = myBoats.iterator();
							n = i.hasNext();
							if (n) break;
						case 2:
							s ++ ;
							i = myBuilders.iterator();
							n = i.hasNext();
							if (n) break;
						case 3:
							s ++ ;
							i = myFighters.iterator();
							n = i.hasNext();
						}
						return n;
					}
					
					@Override
					public Unit next() {
						hasNext();
						return i.next();
					}
					
				};
			}
			
			@Override
			public Object[] toArray() {
				Object[] o = new Object[size()];
				int i = 0;
				for (Object obj : this) {
					o[i ++ ] = obj;
				}
				return o;
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public <T> T[] toArray(T[] a) {
				int s = size();
				Class <?> comp = a.getClass().getComponentType();
				if (s != a.length) a = (T[]) Array.newInstance(comp, s);
				int i = 0;
				if (Entity.class.isAssignableFrom(comp)) {
					Entity[] us = (Entity[]) a;
					for (Unit obj : this) {
						us[i ++ ] = obj;
					}
				} else if (comp == Object.class) {
					Object[] objs = (Object[]) a;
					for (Unit obj : this) {
						objs[i ++ ] = obj;
					}
				} else for (Unit un : this) {
					a[i ++ ] = (T) un;
				}
				return a;
			}
			
			@Override
			public boolean add(Unit e) {
				switch (e.type) {
				case bow:
				case meele:
				case fightingBoat:
					return myFighters.add(e);
				case boat:
					return myBoats.add(e);
				case builder:
					return myBuilders.add(e);
				case carrier:
					return myCarriers.add(e);
				case simple:
					return mySimples.add(e);
				default:
					throw new IllegalArgumentException(
							"the UnitPool can only contain units, which does not include types of non movable Entitys (even if they are an instance of the Unit class type=" + e.type.name() + " unit='" + e + "'");
				}
			}
			
			@Override
			public boolean remove(Object o) {
				if (o instanceof Unit) return remove((Unit) o);
				return false;
			}
			
			public boolean remove(Unit u) {
				switch (u.type) {
				case bow:
				case meele:
				case fightingBoat:
					return myFighters.remove(u);
				case boat:
					return myBoats.remove(u);
				case builder:
					return myBuilders.remove(u);
				case carrier:
					return myCarriers.remove(u);
				case simple:
					return mySimples.remove(u);
				default:
					return false;
				}
			}
			
			@Override
			public boolean containsAll(Collection <?> c) {
				if (c instanceof UnitPool) {
					UnitPool p = (UnitPool) c;
					if ( !myBoats.containsAll(p.myBoats)) return false;
					if ( !myBuilders.containsAll(p.myBuilders)) return false;
					if ( !myCarriers.containsAll(p.myCarriers)) return false;
					if ( !myFighters.containsAll(p.myFighters)) return false;
					return mySimples.containsAll(p.mySimples);
				} else if (c instanceof EntityPool) {
					EntityPool p = (EntityPool) c;
					if ( !myBoats.containsAll(p.myUnits.myBoats)) return false;
					if ( !myBuilders.containsAll(p.myUnits.myBuilders)) return false;
					if ( !myCarriers.containsAll(p.myUnits.myCarriers)) return false;
					if ( !myFighters.containsAll(p.myUnits.myFighters)) return false;
					return mySimples.containsAll(p.myUnits.mySimples);
				} else if (c instanceof FighterPool) {
					return myFighters.containsAll((FighterPool) c);
				} else {
					for (Object o : c) {
						if ( !contains(o)) return false;
					}
					return true;
				}
			}
			
			public boolean containsAll(UnitPool p) {
				if ( !myBoats.containsAll(p.myBoats)) return false;
				if ( !myBuilders.containsAll(p.myBuilders)) return false;
				if ( !myCarriers.containsAll(p.myCarriers)) return false;
				if ( !myFighters.containsAll(p.myFighters)) return false;
				return mySimples.containsAll(p.mySimples);
			}
			
			public boolean containsAll(EntityPool p) {
				if ( !myBoats.containsAll(p.myUnits.myBoats)) return false;
				if ( !myBuilders.containsAll(p.myUnits.myBuilders)) return false;
				if ( !myCarriers.containsAll(p.myUnits.myCarriers)) return false;
				if ( !myFighters.containsAll(p.myUnits.myFighters)) return false;
				return mySimples.containsAll(p.myUnits.mySimples);
			}
			
			public boolean containsAll(FighterPool c) {
				return myFighters.containsAll(c);
			}
			
			@Override
			public boolean addAll(Collection <? extends Unit> c) {
				boolean res;
				if (c instanceof UnitPool) {
					UnitPool p = (UnitPool) c;
					res = myBoats.addAll(p.myBoats);
					res |= myBuilders.addAll(p.myBuilders);
					res |= myCarriers.addAll(p.myCarriers);
					res |= myFighters.addAll(p.myFighters);
					res |= mySimples.addAll(p.mySimples);
				} else if (c instanceof FighterPool) {
					res = myFighters.addAll((FighterPool) c);
				} else {
					res = false;
					for (Unit unit : c) {
						res |= add(unit);
					}
				}
				return res;
			}
			
			@Override
			public boolean retainAll(Collection <?> c) {
				if (c instanceof UnitPool) {
					UnitPool p = (UnitPool) c;
					boolean res = myBoats.retainAll(p.myBoats);
					res |= myBuilders.retainAll(p.myBuilders);
					res |= myCarriers.retainAll(p.myCarriers);
					res |= myFighters.retainAll(p.myFighters);
					return res | mySimples.retainAll(p.mySimples);
				} else if (c instanceof EntityPool) {
					EntityPool p = (EntityPool) c;
					boolean res = myBoats.retainAll(p.myUnits.myBoats);
					res |= myBuilders.retainAll(p.myUnits.myBuilders);
					res |= myCarriers.retainAll(p.myUnits.myCarriers);
					res |= myFighters.retainAll(p.myUnits.myFighters);
					return res | mySimples.retainAll(p.myUnits.mySimples);
				} else if (c instanceof FighterPool) {
					boolean res = ! (myBoats.isEmpty() && myBuilders.isEmpty() && myCarriers.isEmpty() && mySimples.isEmpty());
					myBoats.clear();
					myBuilders.clear();
					myCarriers.clear();
					mySimples.clear();
					return res | myFighters.retainAll((FighterPool) c);
				} else {
					List <Unit> fight = new ArrayList <Unit>(), build = new ArrayList <Unit>(), boat = new ArrayList <Unit>(), carrie = new ArrayList <Unit>(), simple = new ArrayList <Unit>();
					myFighters.forEach(o -> {
						if ( !c.contains(o)) fight.add(o);
					});
					myBuilders.forEach(o -> {
						if ( !c.contains(o)) build.add(o);
					});
					myBoats.forEach(o -> {
						if ( !c.contains(o)) boat.add(o);
					});
					myCarriers.forEach(o -> {
						if ( !c.contains(o)) carrie.add(o);
					});
					mySimples.forEach(o -> {
						if ( !c.contains(o)) simple.add(o);
					});
					fight.forEach(o -> myFighters.remove(o));
					build.forEach(o -> myBoats.remove(o));
					boat.forEach(o -> myBuilders.remove(o));
					carrie.forEach(o -> myCarriers.remove(o));
					simple.forEach(o -> mySimples.remove(o));
					return ! (fight.isEmpty() && build.isEmpty() && boat.isEmpty() && carrie.isEmpty() && simple.isEmpty());
				}
			}
			
			public boolean retainAll(UnitPool p) {
				boolean res = myBoats.retainAll(p.myBoats);
				res |= myBuilders.retainAll(p.myBuilders);
				res |= myCarriers.retainAll(p.myCarriers);
				res |= myFighters.retainAll(p.myFighters);
				return res | mySimples.retainAll(p.mySimples);
			}
			
			public boolean retainAll(EntityPool p) {
				boolean res = myBoats.retainAll(p.myUnits.myBoats);
				res |= myBuilders.retainAll(p.myUnits.myBuilders);
				res |= myCarriers.retainAll(p.myUnits.myCarriers);
				res |= myFighters.retainAll(p.myUnits.myFighters);
				return res | mySimples.retainAll(p.myUnits.mySimples);
			}
			
			public boolean retainAll(FighterPool f) {
				boolean res = ! (myBoats.isEmpty() && myBuilders.isEmpty() && myCarriers.isEmpty() && mySimples.isEmpty());
				myBoats.clear();
				myBuilders.clear();
				myCarriers.clear();
				mySimples.clear();
				return res | myFighters.retainAll(f);
			}
			
			@Override
			public boolean removeAll(Collection <?> c) {
				if (c instanceof UnitPool) {
					UnitPool p = (UnitPool) c;
					boolean res = myBoats.removeAll(p.myBoats);
					res |= myBuilders.removeAll(p.myBuilders);
					res |= myCarriers.removeAll(p.myCarriers);
					res |= myFighters.removeAll(p.myFighters);
					return res | mySimples.removeAll(p.mySimples);
				} else if (c instanceof FighterPool) {
					return myFighters.removeAll((FighterPool) c);
				} else if (c instanceof EntityPool) {
					EntityPool p = (EntityPool) c;
					boolean res = myBoats.removeAll(p.myUnits.myBoats);
					res |= myBuilders.removeAll(p.myUnits.myBuilders);
					res |= myCarriers.removeAll(p.myUnits.myCarriers);
					res |= myFighters.removeAll(p.myUnits.myFighters);
					return res | mySimples.removeAll(p.myUnits.mySimples);
				} else {
					boolean res = false;
					for (Object object : c) {
						res |= remove(object);
					}
					return res;
				}
			}
			
			public boolean removeAll(UnitPool p) {
				boolean res = myBoats.removeAll(p.myBoats);
				res |= myBuilders.removeAll(p.myBuilders);
				res |= myCarriers.removeAll(p.myCarriers);
				res |= myFighters.removeAll(p.myFighters);
				return res | mySimples.removeAll(p.mySimples);
			}
			
			public boolean removeAll(FighterPool p) {
				return myFighters.removeAll(p);
			}
			
			public boolean removeAll(EntityPool p) {
				boolean res = myBoats.removeAll(p.myUnits.myBoats);
				res |= myBuilders.removeAll(p.myUnits.myBuilders);
				res |= myCarriers.removeAll(p.myUnits.myCarriers);
				res |= myFighters.removeAll(p.myUnits.myFighters);
				return res | mySimples.removeAll(p.myUnits.mySimples);
			}
			
			@Override
			public void clear() {
				myBoats.clear();
				myBuilders.clear();
				myCarriers.clear();
				myFighters.clear();
				mySimples.clear();
			}
			
		}
		
		@Override
		public int size() {
			int s = myUnits.size();
			s += myBuildings.size();
			return s;
		}
		
		@Override
		public boolean isEmpty() {
			return myUnits.isEmpty() && myUnits.isEmpty();
		}
		
		@Override
		public boolean contains(Object o) {
			if (o instanceof Entity) return contains((Entity) o);
			return false;
		}
		
		public boolean contains(Entity e) {
			if (e.isMovable()) {
				return myUnits.contains((Unit) e);
			} else {
				return myBuildings.contains((Building) e);
			}
		}
		
		@Override
		public Iterator <Entity> iterator() {
			return new Iterator <Entity>() {
				
				Iterator <? extends Entity> i;
				boolean                     s;
				
				@Override
				public boolean hasNext() {
					boolean ihn = i.hasNext();
					if ( !ihn && !s) {
						s = true;
						i = myBuildings.iterator();
						ihn = i.hasNext();
					}
					return ihn;
				}
				
				@Override
				public Entity next() {
					if ( !i.hasNext() && !s) {
						s = true;
						i = myBuildings.iterator();
					}
					return i.next();
				}
				
			};
		}
		
		@Override
		public Object[] toArray() {
			Object[] o = new Object[size()];
			int i = 0;
			for (Entity e : this) {
				o[i ++ ] = e;
			}
			return o;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			int s = size();
			Class <?> comp = a.getClass().getComponentType();
			if (a.length != s) a = (T[]) Array.newInstance(comp, s);
			if (comp == Entity.class) {
				Entity[] arr = (Entity[]) a;
				int i = 0;
				for (Entity e : this) {
					arr[i ++ ] = e;
				}
			} else if (comp == Object.class) {
				Object[] arr = (Object[]) a;
				int i = 0;
				for (Entity e : this) {
					arr[i ++ ] = e;
				}
			} else {
				int i = 0;
				for (Entity e : this) {
					a[i ++ ] = (T) e;
				}
			}
			return a;
		}
		
		@Override
		public boolean add(Entity e) {
			if (e.isMovable()) return myUnits.add((Unit) e);
			else return myBuildings.add((Building) e);
		}
		
		@Override
		public boolean remove(Object o) {
			if ( ! (o instanceof Entity)) return false;
			if ( ((Entity) o).isMovable()) return myUnits.remove((Unit) o);
			else return myBuildings.remove((Building) o);
		}
		
		public boolean remove(Entity o) {
			if (o.isMovable()) return myUnits.remove((Unit) o);
			else return myBuildings.remove((Building) o);
		}
		
		@Override
		public boolean containsAll(Collection <?> c) {
			if (c instanceof EntityPool) {
				EntityPool ep = (EntityPool) c;
				return myUnits.containsAll(ep.myUnits) && myBuildings.containsAll(ep.myBuildings);
			} else if (c instanceof UnitPool) {
				return myUnits.containsAll((UnitPool) c);
			} else if (c instanceof BuildingPool) {
				return myBuildings.containsAll((BuildingPool) c);
			} else {
				for (Object obj : c) {
					if ( !contains(obj)) return false;
				}
				return true;
			}
		}
		
		public boolean containsAll(EntityPool ep) {
			return myUnits.containsAll(ep.myUnits) && myBuildings.containsAll(ep.myBuildings);
		}
		
		public boolean containsAll(UnitPool c) {
			return myUnits.containsAll(c);
		}
		
		public boolean containsAll(BuildingPool c) {
			return myBuildings.containsAll(c);
		}
		
		@Override
		public boolean addAll(Collection <? extends Entity> c) {
			if (c instanceof EntityPool) {
				EntityPool ep = (EntityPool) c;
				boolean u = myUnits.addAll(ep.myUnits);
				return u | myBuildings.addAll(ep.myBuildings);
			} else if (c instanceof UnitPool) {
				return myUnits.addAll((UnitPool) c);
			} else if (c instanceof BuildingPool) {
				return myBuildings.addAll((BuildingPool) c);
			} else {
				boolean res = false;
				for (Entity e : c) {
					res |= add(e);
				}
				return res;
			}
		}
		
		public boolean addAll(EntityPool ep) {
			boolean u = myUnits.addAll(ep.myUnits);
			return u | myBuildings.addAll(ep.myBuildings);
		}
		
		public boolean addAll(UnitPool c) {
			return myUnits.addAll(c);
		}
		
		public boolean addAll(BuildingPool c) {
			return myBuildings.addAll(c);
		}
		
		@Override
		public boolean retainAll(Collection <?> c) {
			if (c instanceof EntityPool) {
				EntityPool ep = (EntityPool) c;
				boolean u = myUnits.retainAll(ep.myUnits);
				return u | myBuildings.retainAll(ep.myBuildings);
			} else if (c instanceof UnitPool) {
				return myUnits.retainAll((UnitPool) c);
			} else if (c instanceof BuildingPool) {
				return myBuildings.retainAll((BuildingPool) c);
			} else {
				List <Unit> u = new ArrayList <Unit>();
				List <Building> b = new ArrayList <Building>();
				for (Unit unit : myUnits) {
					if ( !c.contains(unit)) u.add(unit);
				}
				for (Building building : myBuildings) {
					if ( !c.contains(building)) b.add(building);
				}
				myUnits.removeAll(u);
				myBuildings.removeAll(b);
				return ! (u.isEmpty() && b.isEmpty());
			}
		}
		
		public boolean retainAll(EntityPool ep) {
			boolean u = myUnits.retainAll(ep.myUnits);
			return u | myBuildings.retainAll(ep.myBuildings);
		}
		
		public boolean retainAll(UnitPool c) {
			return myUnits.retainAll(c);
		}
		
		public boolean retainAll(BuildingPool c) {
			return myBuildings.retainAll(c);
		}
		
		@Override
		public boolean removeAll(Collection <?> c) {
			if (c instanceof EntityPool) {
				EntityPool ep = (EntityPool) c;
				boolean u = myUnits.removeAll(ep.myUnits);
				return u | myBuildings.removeAll(ep.myBuildings);
			} else if (c instanceof UnitPool) {
				return myUnits.removeAll((UnitPool) c);
			} else if (c instanceof BuildingPool) {
				return myBuildings.removeAll((BuildingPool) c);
			} else {
				List <Unit> u = new ArrayList <Unit>();
				List <Building> b = new ArrayList <Building>();
				for (Unit unit : myUnits) {
					if (c.contains(unit)) u.add(unit);
				}
				for (Building building : myBuildings) {
					if (c.contains(building)) b.add(building);
				}
				myUnits.removeAll(u);
				myBuildings.removeAll(b);
				return ! (u.isEmpty() && b.isEmpty());
			}
		}
		
		@Override
		public void clear() {
			myBuildings.clear();
			myUnits.clear();
		}
		
	}
	
	private static class BuildingPool implements Set <Building> {
		
		private HousPool myHouses = new HousPool();
		
		private Set <Building> myPorts          = new HashSet <Building>();
		private Set <Building> myStorages       = new HashSet <Building>();
		private Set <Building> mySprings        = new HashSet <Building>();
		private Set <Building> myFarms          = new HashSet <Building>();
		private Set <Building> myMines          = new HashSet <Building>();
		private Set <Building> myWoodFarms      = new HashSet <Building>();
		private Set <Building> myBuildingPlaces = new HashSet <Building>();
		
		private static class HousPool implements Set <HouseBuilding> {
			
			private Set <HouseBuilding> mySmpleHouses   = new HashSet <HouseBuilding>();
			private Set <HouseBuilding> myMeeleHouses   = new HashSet <HouseBuilding>();
			private Set <HouseBuilding> myBowHouses     = new HashSet <HouseBuilding>();
			private Set <HouseBuilding> myBuilderHouses = new HashSet <HouseBuilding>();
			
			
			
			@Override
			public int size() {
				long s = (long) mySmpleHouses.size() + (long) myMeeleHouses.size() + (long) myBowHouses.size() + (long) myBuilderHouses.size();
				return (int) Math.min(s, Integer.MAX_VALUE);
			}
			
			@Override
			public boolean isEmpty() {
				return mySmpleHouses.isEmpty() && myMeeleHouses.isEmpty() && myBowHouses.isEmpty() && myBuilderHouses.isEmpty();
			}
			
			@Override
			public boolean contains(Object o) {
				if (o instanceof HouseBuilding) return contains((HouseBuilding) o);
				else return false;
			}
			
			public boolean contains(Building b) {
				switch (b.type) {
				default:
					throw new IllegalArgumentException("unknown house type: " + b.type.name() + " of house: '" + b + "'");
				case house:
					return mySmpleHouses.contains(b);
				case houseBow:
					return myBowHouses.contains(b);
				case houseBuilder:
					return myBuilderHouses.contains(b);
				case houseMelee:
					return myMeeleHouses.contains(b);
				}
			}
			
			@Override
			public Iterator <HouseBuilding> iterator() {
				return new Iterator <HouseBuilding>() {
					
					int                      s = 0;
					Iterator <HouseBuilding> i = mySmpleHouses.iterator();
					
					@Override
					public boolean hasNext() {
						boolean n = i.hasNext();
						if ( !n) switch (s) {
						case 0:
							s ++ ;
							i = myBowHouses.iterator();
							n = i.hasNext();
						case 1:
							s ++ ;
							i = myBuilderHouses.iterator();
							n = i.hasNext();
						case 2:
							s ++ ;
							i = myMeeleHouses.iterator();
							n = i.hasNext();
						}
						return n;
					}
					
					@Override
					public HouseBuilding next() {
						hasNext();
						return i.next();
					}
					
				};
			}
			
			@Override
			public Object[] toArray() {
				Object[] o = new Object[size()];
				int i = 0;
				for (HouseBuilding object : this) {
					o[i ++ ] = object;
				}
				return o;
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public <T> T[] toArray(T[] a) {
				int s = size();
				if (s != a.length) a = (T[]) Array.newInstance(a.getClass().getComponentType(), s);
				Object[] arr = a;
				int i = 0;
				for (HouseBuilding object : this) {
					arr[i ++ ] = object;
				}
				return a;
			}
			
			@Override
			public boolean add(HouseBuilding e) {
				switch (e.type) {
				case house:
					return mySmpleHouses.add(e);
				case houseBow:
					return mySmpleHouses.add(e);
				case houseBuilder:
					return mySmpleHouses.add(e);
				case houseMelee:
					return mySmpleHouses.add(e);
				default:
					throw new IllegalArgumentException("unknown house type: " + e.type.name() + " of house: '" + e + "'");
				}
			}
			
			@Override
			public boolean remove(Object o) {
				if (o instanceof HouseBuilding) return remove((HouseBuilding) o);
				else return false;
			}
			
			public boolean remove(HouseBuilding h) {
				switch (h.type) {
				case house:
					return mySmpleHouses.remove(h);
				case houseBow:
					return mySmpleHouses.remove(h);
				case houseBuilder:
					return mySmpleHouses.remove(h);
				case houseMelee:
					return mySmpleHouses.remove(h);
				default:
					return false;
				}
			}
			
			@Override
			public boolean containsAll(Collection <?> c) {
				if (c instanceof HousPool) {
					HousPool p = (HousPool) c;
					if ( !myBowHouses.containsAll(p.myBowHouses)) return false;
					if ( !myBuilderHouses.containsAll(p.myBuilderHouses)) return false;
					if ( !myMeeleHouses.containsAll(p.myMeeleHouses)) return false;
					return mySmpleHouses.containsAll(p.mySmpleHouses);
				} else if (c instanceof BuildingPool) {
					BuildingPool p = (BuildingPool) c;
					if ( !myBowHouses.containsAll(p.myHouses.myBowHouses)) return false;
					if ( !myBuilderHouses.containsAll(p.myHouses.myBuilderHouses)) return false;
					if ( !myMeeleHouses.containsAll(p.myHouses.myMeeleHouses)) return false;
					return mySmpleHouses.containsAll(p.myHouses.mySmpleHouses);
				} else if (c instanceof EntityPool) {
					EntityPool p = (EntityPool) c;
					if ( !myBowHouses.containsAll(p.myBuildings.myHouses.myBowHouses)) return false;
					if ( !myBuilderHouses.containsAll(p.myBuildings.myHouses.myBuilderHouses)) return false;
					if ( !myMeeleHouses.containsAll(p.myBuildings.myHouses.myMeeleHouses)) return false;
					return mySmpleHouses.containsAll(p.myBuildings.myHouses.mySmpleHouses);
				} else {
					for (Object object : c) {
						if ( !contains(object)) return false;
					}
					return true;
				}
			}
			
			public boolean containsAll(HousPool p) {
				if ( !myBowHouses.containsAll(p.myBowHouses)) return false;
				if ( !myBuilderHouses.containsAll(p.myBuilderHouses)) return false;
				if ( !myMeeleHouses.containsAll(p.myMeeleHouses)) return false;
				return mySmpleHouses.containsAll(p.mySmpleHouses);
			}
			
			public boolean containsAll(BuildingPool p) {
				if ( !myBowHouses.containsAll(p.myHouses.myBowHouses)) return false;
				if ( !myBuilderHouses.containsAll(p.myHouses.myBuilderHouses)) return false;
				if ( !myMeeleHouses.containsAll(p.myHouses.myMeeleHouses)) return false;
				return mySmpleHouses.containsAll(p.myHouses.mySmpleHouses);
			}
			
			public boolean containsAll(EntityPool p) {
				if ( !myBowHouses.containsAll(p.myBuildings.myHouses.myBowHouses)) return false;
				if ( !myBuilderHouses.containsAll(p.myBuildings.myHouses.myBuilderHouses)) return false;
				if ( !myMeeleHouses.containsAll(p.myBuildings.myHouses.myMeeleHouses)) return false;
				return mySmpleHouses.containsAll(p.myBuildings.myHouses.mySmpleHouses);
			}
			
			@Override
			public boolean addAll(Collection <? extends HouseBuilding> c) {
				if (c instanceof HousPool) {
					HousPool p = (HousPool) c;
					boolean res = myBowHouses.addAll(p.myBowHouses);
					res |= myBuilderHouses.addAll(p.myBuilderHouses);
					res |= myMeeleHouses.addAll(p.myMeeleHouses);
					return res | mySmpleHouses.addAll(p.mySmpleHouses);
				} else {
					boolean res = false;
					for (HouseBuilding houseBuilding : c) {
						res |= add(houseBuilding);
					}
					return res;
				}
			}
			
			public boolean addAll(HousPool p) {
				boolean res = myBowHouses.addAll(p.myBowHouses);
				res |= myBuilderHouses.addAll(p.myBuilderHouses);
				res |= myMeeleHouses.addAll(p.myMeeleHouses);
				return res | mySmpleHouses.addAll(p.mySmpleHouses);
			}
			
			@Override
			public boolean retainAll(Collection <?> c) {
				boolean res;
				if (c instanceof HousPool) {
					HousPool p = (HousPool) c;
					res = myBowHouses.retainAll(p.myBowHouses);
					res |= myBuilderHouses.retainAll(p.myBuilderHouses);
					res |= myMeeleHouses.retainAll(p.myMeeleHouses);
					res |= mySmpleHouses.retainAll(p.mySmpleHouses);
				} else if (c instanceof BuildingPool) {
					BuildingPool p = (BuildingPool) c;
					res = myBowHouses.retainAll(p.myHouses.myBowHouses);
					res |= myBuilderHouses.retainAll(p.myHouses.myBuilderHouses);
					res |= myMeeleHouses.retainAll(p.myHouses.myMeeleHouses);
					res |= mySmpleHouses.retainAll(p.myHouses.mySmpleHouses);
				} else if (c instanceof EntityPool) {
					EntityPool p = (EntityPool) c;
					res = myBowHouses.retainAll(p.myBuildings.myHouses.myBowHouses);
					res |= myBuilderHouses.retainAll(p.myBuildings.myHouses.myBuilderHouses);
					res |= myMeeleHouses.retainAll(p.myBuildings.myHouses.myMeeleHouses);
					res |= mySmpleHouses.retainAll(p.myBuildings.myHouses.mySmpleHouses);
				} else {
					if ( ! (c instanceof Set)) c = new HashSet <>(c);
					List <HouseBuilding> bow = new ArrayList <HouseBuilding>(), build = new ArrayList <HouseBuilding>(), meele = new ArrayList <HouseBuilding>(), simple = new ArrayList <HouseBuilding>();
					final Collection <?> col = c;
					myBowHouses.forEach(h -> {
						if ( !col.contains(h)) bow.add(h);
					});
					myBuilderHouses.forEach(h -> {
						if ( !col.contains(h)) build.add(h);
					});
					myMeeleHouses.forEach(h -> {
						if ( !col.contains(h)) meele.add(h);
					});
					mySmpleHouses.forEach(h -> {
						if ( !col.contains(h)) simple.add(h);
					});
					bow.forEach(h -> remove(h));
					build.forEach(h -> remove(h));
					meele.forEach(h -> remove(h));
					simple.forEach(h -> remove(h));
					res = bow.isEmpty();
					res |= build.isEmpty();
					res |= meele.isEmpty();
					res |= simple.isEmpty();
				}
				return res;
			}
			
			public boolean retainAll(HousPool p) {
				boolean res = myBowHouses.retainAll(p.myBowHouses);
				res |= myBuilderHouses.retainAll(p.myBuilderHouses);
				res |= myMeeleHouses.retainAll(p.myMeeleHouses);
				return res | mySmpleHouses.retainAll(p.mySmpleHouses);
			}
			
			public boolean retainAll(BuildingPool p) {
				boolean res = myBowHouses.retainAll(p.myHouses.myBowHouses);
				res |= myBuilderHouses.retainAll(p.myHouses.myBuilderHouses);
				res |= myMeeleHouses.retainAll(p.myHouses.myMeeleHouses);
				return res | mySmpleHouses.retainAll(p.myHouses.mySmpleHouses);
			}
			
			public boolean retainAll(EntityPool p) {
				boolean res = myBowHouses.retainAll(p.myBuildings.myHouses.myBowHouses);
				res |= myBuilderHouses.retainAll(p.myBuildings.myHouses.myBuilderHouses);
				res |= myMeeleHouses.retainAll(p.myBuildings.myHouses.myMeeleHouses);
				return res | mySmpleHouses.retainAll(p.myBuildings.myHouses.mySmpleHouses);
			}
			
			@Override
			public boolean removeAll(Collection <?> c) {
				boolean res;
				if (c instanceof HousPool) {
					HousPool p = (HousPool) c;
					res = myBowHouses.removeAll(p.myBowHouses);
					res |= myBuilderHouses.removeAll(p.myBuilderHouses);
					res |= myMeeleHouses.removeAll(p.myMeeleHouses);
					res |= mySmpleHouses.removeAll(p.mySmpleHouses);
				} else if (c instanceof BuildingPool) {
					BuildingPool p = (BuildingPool) c;
					res = myBowHouses.removeAll(p.myHouses.myBowHouses);
					res |= myBuilderHouses.removeAll(p.myHouses.myBuilderHouses);
					res |= myMeeleHouses.removeAll(p.myHouses.myMeeleHouses);
					res |= mySmpleHouses.removeAll(p.myHouses.mySmpleHouses);
				} else if (c instanceof EntityPool) {
					EntityPool p = (EntityPool) c;
					res = myBowHouses.removeAll(p.myBuildings.myHouses.myBowHouses);
					res |= myBuilderHouses.removeAll(p.myBuildings.myHouses.myBuilderHouses);
					res |= myMeeleHouses.removeAll(p.myBuildings.myHouses.myMeeleHouses);
					res |= mySmpleHouses.removeAll(p.myBuildings.myHouses.mySmpleHouses);
				} else {
					res = false;
					for (Object object : c) {
						res |= remove(object);
					}
				}
				return res;
			}
			
			@Override
			public void clear() {
				myBowHouses.clear();
				myBuilderHouses.clear();
				myMeeleHouses.clear();
				mySmpleHouses.clear();
			}
			
		}
		
		@Override
		public int size() {
			long s = (long) myFarms.size() + (long) myHouses.size() + (long) myMines.size() + (long) myPorts.size() + (long) mySprings.size() + (long) myStorages.size() + (long) myWoodFarms.size()
					+ (long) myBuildingPlaces.size();
			return (int) Math.min(s, Integer.MAX_VALUE);
		}
		
		@Override
		public boolean isEmpty() {
			return myFarms.isEmpty() && myHouses.isEmpty() && myMines.isEmpty() && myPorts.isEmpty() && mySprings.isEmpty() && myStorages.isEmpty() && myWoodFarms.isEmpty() && myBuildingPlaces.isEmpty();
		}
		
		@Override
		public boolean contains(Object o) {
			if (o instanceof Building) return contains((Building) o);
			else return false;
		}
		
		public boolean contains(Building o) {
			switch (o.type) {
			case buildplace:
				return myBuildingPlaces.contains(o);
			case farm:
				return myFarms.contains(o);
			case house:
			case houseBow:
			case houseBuilder:
			case houseMelee:
				return myHouses.contains(o);
			case mine:
				return myMines.contains(o);
			case spring:
				return mySprings.contains(o);
			case storage:
				return myStorages.contains(o);
			case woodFarm:
				return myWoodFarms.contains(o);
			default:
				return false;
			}
		}
		
		@Override
		public Iterator <Building> iterator() {
			return new Iterator <Building>() {
				
				int                           s = 0;
				Iterator <? extends Building> i = myBuildingPlaces.iterator();
				
				@Override
				public boolean hasNext() {
					boolean n = i.hasNext();
					if ( !n) switch (s) {
					case 0:
						s ++ ;
						i = myFarms.iterator();
						n = i.hasNext();
						if (n) break;
					case 1:
						s ++ ;
						i = myHouses.iterator();
						n = i.hasNext();
						if (n) break;
					case 2:
						s ++ ;
						i = myMines.iterator();
						n = i.hasNext();
						if (n) break;
					case 3:
						s ++ ;
						i = mySprings.iterator();
						n = i.hasNext();
						if (n) break;
					case 4:
						s ++ ;
						i = myStorages.iterator();
						n = i.hasNext();
						if (n) break;
					case 5:
						s ++ ;
						i = myWoodFarms.iterator();
						n = i.hasNext();
					}
					return n;
				}
				
				@Override
				public Building next() {
					hasNext();
					return i.next();
				}
				
			};
		}
		
		@Override
		public Object[] toArray() {
			Object[] o = new Object[size()];
			int i = 0;
			for (Building object : this) {
				o[i ++ ] = object;
			}
			return o;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			int s = size();
			if (s != a.length) a = (T[]) Array.newInstance(a.getClass().getComponentType(), s);
			Object[] o = a;
			int i = 0;
			for (Building object : this) {
				o[i ++ ] = object;
			}
			return (T[]) a;
		}
		
		@Override
		public boolean add(Building e) {
			switch (e.type) {
			case buildplace:
				return myBuildingPlaces.add(e);
			case farm:
				return myFarms.add(e);
			case house:
			case houseBow:
			case houseBuilder:
			case houseMelee:
				return myHouses.add((HouseBuilding) e);
			case mine:
				return myMines.add(e);
			case spring:
				return mySprings.add(e);
			case storage:
				return myStorages.add(e);
			case woodFarm:
				return myWoodFarms.add(e);
			default:
				throw new IllegalArgumentException("illegal build type");
			}
		}
		
		@Override
		public boolean remove(Object o) {
			if (o instanceof Building) return remove((Building) o);
			return false;
		}
		
		public boolean remove(Building b) {
			switch (b.type) {
			case buildplace:
				return myBuildingPlaces.remove(b);
			case farm:
				return myFarms.remove(b);
			case house:
			case houseBow:
			case houseBuilder:
			case houseMelee:
				return myHouses.remove(b);
			case mine:
				return myMines.remove(b);
			case spring:
				return mySprings.remove(b);
			case storage:
				return myStorages.remove(b);
			case woodFarm:
				return myWoodFarms.remove(b);
			default:
				return false;
			}
		}
		
		@Override
		public boolean containsAll(Collection <?> c) {
			if (c instanceof BuildingPool) {
				BuildingPool p = (BuildingPool) c;
				if ( !myBuildingPlaces.containsAll(p.myBuildingPlaces)) return false;
				if ( !myFarms.containsAll(p.myFarms)) return false;
				if ( !myHouses.containsAll(p.myHouses)) return false;
				if ( !myMines.containsAll(p.myMines)) return false;
				if ( !myPorts.containsAll(p.myPorts)) return false;
				if ( !mySprings.containsAll(p.mySprings)) return false;
				if ( !myStorages.containsAll(p.myStorages)) return false;
				return myWoodFarms.containsAll(p.myWoodFarms);
			} else if (c instanceof HousPool) {
				return myHouses.containsAll((HousPool) c);
			} else if (c instanceof EntityPool) {
				EntityPool p = (EntityPool) c;
				if ( !myBuildingPlaces.containsAll(p.myBuildings.myBuildingPlaces)) return false;
				if ( !myFarms.containsAll(p.myBuildings.myFarms)) return false;
				if ( !myHouses.containsAll(p.myBuildings.myHouses)) return false;
				if ( !myMines.containsAll(p.myBuildings.myMines)) return false;
				if ( !myPorts.containsAll(p.myBuildings.myPorts)) return false;
				if ( !mySprings.containsAll(p.myBuildings.mySprings)) return false;
				if ( !myStorages.containsAll(p.myBuildings.myStorages)) return false;
				return myWoodFarms.containsAll(p.myBuildings.myWoodFarms);
			} else {
				for (Object object : c) {
					if ( !contains(object)) return false;
				}
				return true;
			}
		}
		
		public boolean containsAll(BuildingPool p) {
			if ( !myBuildingPlaces.containsAll(p.myBuildingPlaces)) return false;
			if ( !myFarms.containsAll(p.myFarms)) return false;
			if ( !myHouses.containsAll(p.myHouses)) return false;
			if ( !myMines.containsAll(p.myMines)) return false;
			if ( !myPorts.containsAll(p.myPorts)) return false;
			if ( !mySprings.containsAll(p.mySprings)) return false;
			if ( !myStorages.containsAll(p.myStorages)) return false;
			return myWoodFarms.containsAll(p.myWoodFarms);
		}
		
		public boolean containsAll(HousPool c) {
			return myHouses.containsAll(c);
		}
		
		public boolean containsAll(EntityPool p) {
			if ( !myBuildingPlaces.containsAll(p.myBuildings.myBuildingPlaces)) return false;
			if ( !myFarms.containsAll(p.myBuildings.myFarms)) return false;
			if ( !myHouses.containsAll(p.myBuildings.myHouses)) return false;
			if ( !myMines.containsAll(p.myBuildings.myMines)) return false;
			if ( !myPorts.containsAll(p.myBuildings.myPorts)) return false;
			if ( !mySprings.containsAll(p.myBuildings.mySprings)) return false;
			if ( !myStorages.containsAll(p.myBuildings.myStorages)) return false;
			return myWoodFarms.containsAll(p.myBuildings.myWoodFarms);
		}
		
		@Override
		public boolean addAll(Collection <? extends Building> c) {
			if (c instanceof BuildingPool) {
				BuildingPool p = (BuildingPool) c;
				boolean res = myBuildingPlaces.addAll(p.myBuildingPlaces);
				res |= myFarms.addAll(p.myFarms);
				res |= myHouses.addAll(p.myHouses);
				res |= myMines.addAll(p.myMines);
				res |= myPorts.addAll(p.myPorts);
				res |= mySprings.addAll(p.mySprings);
				res |= myStorages.addAll(p.myStorages);
				return res | myWoodFarms.addAll(p.myWoodFarms);
			} else if (c instanceof HousPool) {
				return myHouses.addAll((HousPool) c);
			} else {
				boolean res = false;
				for (Building building : c) {
					res |= add(building);
				}
				return res;
			}
		}
		
		public boolean addAll(BuildingPool p) {
			boolean res = myBuildingPlaces.addAll(p.myBuildingPlaces);
			res |= myFarms.addAll(p.myFarms);
			res |= myHouses.addAll(p.myHouses);
			res |= myMines.addAll(p.myMines);
			res |= myPorts.addAll(p.myPorts);
			res |= mySprings.addAll(p.mySprings);
			res |= myStorages.addAll(p.myStorages);
			return res | myWoodFarms.addAll(p.myWoodFarms);
		}
		
		public boolean addAll(HousPool p) {
			return myHouses.addAll(p);
		}
		
		@Override
		public boolean retainAll(Collection <?> c) {
			if (c instanceof BuildingPool) {
				BuildingPool p = (BuildingPool) c;
				boolean res = myBuildingPlaces.retainAll(p.myBuildingPlaces);
				res |= myFarms.retainAll(p.myFarms);
				res |= myHouses.retainAll(p.myHouses);
				res |= myMines.retainAll(p.myMines);
				res |= myPorts.retainAll(p.myPorts);
				res |= mySprings.retainAll(p.mySprings);
				res |= myStorages.retainAll(p.myStorages);
				return res | myWoodFarms.retainAll(p.myWoodFarms);
			} else if (c instanceof HousPool) {
				return myHouses.retainAll((HousPool) c);
			} else if (c instanceof EntityPool) {
				EntityPool p = (EntityPool) c;
				boolean res = myBuildingPlaces.retainAll(p.myBuildings.myBuildingPlaces);
				res |= myFarms.retainAll(p.myBuildings.myFarms);
				res |= myHouses.retainAll(p.myBuildings.myHouses);
				res |= myMines.retainAll(p.myBuildings.myMines);
				res |= myPorts.retainAll(p.myBuildings.myPorts);
				res |= mySprings.retainAll(p.myBuildings.mySprings);
				res |= myStorages.retainAll(p.myBuildings.myStorages);
				return res | myWoodFarms.retainAll(p.myBuildings.myWoodFarms);
			} else {
				List <Object> builldingPlaces = new ArrayList <>(), farms = new ArrayList <>(), houses = new ArrayList <>(), mines = new ArrayList <>(), ports = new ArrayList <>(), springs = new ArrayList <>(),
						storages = new ArrayList <>(), woodFarms = new ArrayList <>();
				myBuildingPlaces.forEach(o -> {
					if ( !c.contains(o)) builldingPlaces.add(o);
				});
				myFarms.forEach(o -> {
					if ( !c.contains(o)) farms.add(o);
				});
				myHouses.forEach(o -> {
					if ( !c.contains(o)) houses.add(o);
				});
				myMines.forEach(o -> {
					if ( !c.contains(o)) mines.add(o);
				});
				myPorts.forEach(o -> {
					if ( !c.contains(o)) ports.add(o);
				});
				mySprings.forEach(o -> {
					if ( !c.contains(o)) springs.add(o);
				});
				myStorages.forEach(o -> {
					if ( !c.contains(o)) storages.add(o);
				});
				myWoodFarms.forEach(o -> {
					if ( !c.contains(o)) woodFarms.add(o);
				});
				builldingPlaces.forEach(o -> myBuildingPlaces.remove(o));
				farms.forEach(o -> myFarms.remove(o));
				houses.forEach(o -> myHouses.remove(o));
				mines.forEach(o -> myMines.remove(o));
				ports.forEach(o -> myPorts.remove(o));
				springs.forEach(o -> mySprings.remove(o));
				storages.forEach(o -> myStorages.remove(o));
				woodFarms.forEach(o -> myWoodFarms.remove(o));
				return ! (builldingPlaces.isEmpty() && farms.isEmpty() && houses.isEmpty() && mines.isEmpty() && ports.isEmpty() && springs.isEmpty() && storages.isEmpty() && woodFarms.isEmpty());
			}
		}
		
		public boolean retainAll(BuildingPool p) {
			boolean res = myBuildingPlaces.retainAll(p.myBuildingPlaces);
			res |= myFarms.retainAll(p.myFarms);
			res |= myHouses.retainAll(p.myHouses);
			res |= myMines.retainAll(p.myMines);
			res |= myPorts.retainAll(p.myPorts);
			res |= mySprings.retainAll(p.mySprings);
			res |= myStorages.retainAll(p.myStorages);
			return res | myWoodFarms.retainAll(p.myWoodFarms);
		}
		
		public boolean retainAll(HousPool c) {
			return myHouses.retainAll(c);
		}
		
		public boolean retainAll(EntityPool p) {
			boolean res = myBuildingPlaces.retainAll(p.myBuildings.myBuildingPlaces);
			res |= myFarms.retainAll(p.myBuildings.myFarms);
			res |= myHouses.retainAll(p.myBuildings.myHouses);
			res |= myMines.retainAll(p.myBuildings.myMines);
			res |= myPorts.retainAll(p.myBuildings.myPorts);
			res |= mySprings.retainAll(p.myBuildings.mySprings);
			res |= myStorages.retainAll(p.myBuildings.myStorages);
			return res | myWoodFarms.retainAll(p.myBuildings.myWoodFarms);
		}
		
		@Override
		public boolean removeAll(Collection <?> c) {
			if (c instanceof BuildingPool) {
				BuildingPool p = (BuildingPool) c;
				boolean res = myBuildingPlaces.removeAll(p.myBuildingPlaces);
				res |= myFarms.removeAll(p.myFarms);
				res |= myHouses.removeAll(p.myHouses);
				res |= myMines.removeAll(p.myMines);
				res |= myPorts.removeAll(p.myPorts);
				res |= mySprings.removeAll(p.mySprings);
				res |= myStorages.removeAll(p.myStorages);
				return res | myWoodFarms.removeAll(p.myWoodFarms);
			} else if (c instanceof HousPool) {
				return myHouses.removeAll((HousPool) c);
			} else if (c instanceof EntityPool) {
				EntityPool p = (EntityPool) c;
				boolean res = myBuildingPlaces.removeAll(p.myBuildings.myBuildingPlaces);
				res |= myFarms.removeAll(p.myBuildings.myFarms);
				res |= myHouses.removeAll(p.myBuildings.myHouses);
				res |= myMines.removeAll(p.myBuildings.myMines);
				res |= myPorts.removeAll(p.myBuildings.myPorts);
				res |= mySprings.removeAll(p.myBuildings.mySprings);
				res |= myStorages.removeAll(p.myBuildings.myStorages);
				return res | myWoodFarms.removeAll(p.myBuildings.myWoodFarms);
			} else {
				boolean res = false;
				for (Object object : c) {
					res |= remove(object);
				}
				return res;
			}
		}
		
		public boolean removeAll(BuildingPool p) {
			boolean res = myBuildingPlaces.removeAll(p.myBuildingPlaces);
			res |= myFarms.removeAll(p.myFarms);
			res |= myHouses.removeAll(p.myHouses);
			res |= myMines.removeAll(p.myMines);
			res |= myPorts.removeAll(p.myPorts);
			res |= mySprings.removeAll(p.mySprings);
			res |= myStorages.removeAll(p.myStorages);
			return res | myWoodFarms.removeAll(p.myWoodFarms);
		}
		
		public boolean removeAll(HousPool c) {
			return myHouses.removeAll(c);
		}
		
		public boolean removeAll(EntityPool p) {
			boolean res = myBuildingPlaces.removeAll(p.myBuildings.myBuildingPlaces);
			res |= myFarms.removeAll(p.myBuildings.myFarms);
			res |= myHouses.removeAll(p.myBuildings.myHouses);
			res |= myMines.removeAll(p.myBuildings.myMines);
			res |= myPorts.removeAll(p.myBuildings.myPorts);
			res |= mySprings.removeAll(p.myBuildings.mySprings);
			res |= myStorages.removeAll(p.myBuildings.myStorages);
			return res | myWoodFarms.removeAll(p.myBuildings.myWoodFarms);
		}
		
		@Override
		public void clear() {
			myBuildingPlaces.clear();
			myFarms.clear();
			myHouses.clear();
			myMines.clear();
			myPorts.clear();
			mySprings.clear();
			myStorages.clear();
			myWoodFarms.clear();
		}
		
	}
	// TODO continue here
	
	public BotPlayer() {
	}
	
	@Override
	public void init(final World world, final int myID) {
		if (this.world != null) throw new IllegalStateException("already initializised!");
		this.world = world;
		this.myID = myID;
		this.myArea = new Area(0, 0, world.getXCnt(), world.getYCnt());
		this.myOuterArea = new Area(0, 0, world.getXCnt(), world.getYCnt());
		this.myEntitys = new EntityPool();
		this.myEnemeys = new HashMap <Integer, Enemy>();
		this.unownedBuildings = new BuildingPool();
	}
	
	
	
	@Override
	public void makeTurn() {
		rebuild();
		
	}
	
	private void rebuild() {
		this.myArea.clear();
		this.myOuterArea.clear();
		this.myEntitys.clear();
		this.myEnemeys.clear();
		this.unownedBuildings.clear();
	}
	
}
