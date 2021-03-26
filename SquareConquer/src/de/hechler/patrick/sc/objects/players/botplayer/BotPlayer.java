package de.hechler.patrick.sc.objects.players.botplayer;

import de.hechler.patrick.sc.interfaces.Player;
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
	* this area is owned completely by the bot
	* the bot will only built there
	* it will use the buildings inside
	* it will try to expand the borders
	 * </pre>
	 */
	private Object myArea;
	/**
	 * <pre>
	* this area contains all Fields, which are inside of 'myArea'
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
	private Object myOuterArea;
	
	@Override
	public void makeTurn(World world, int myID) {
		
	}
	
}