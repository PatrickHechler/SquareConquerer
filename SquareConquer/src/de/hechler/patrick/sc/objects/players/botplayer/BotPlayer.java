package de.hechler.patrick.sc.objects.players.botplayer;

import java.util.HashMap;
import java.util.Map;

import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Player;
import de.hechler.patrick.sc.objects.Area;
import de.hechler.patrick.sc.objects.Building;
import de.hechler.patrick.sc.objects.HouseBuilding;
import de.hechler.patrick.sc.objects.Unit;
import de.hechler.patrick.sc.objects.World;
import de.hechler.patrick.sc.objects.pools.BuildingPool;
import de.hechler.patrick.sc.objects.pools.EntityPool;

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
	
	private class Enemy {
		
		Area       area;
		EntityPool entitys;
		
		private Enemy(int xCnt, int yCnt) {
			area = new Area(0, 0, xCnt, yCnt);
			entitys = new EntityPool();
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
		//TODO continue here
	}
	
}
