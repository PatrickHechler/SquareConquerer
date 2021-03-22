package de.hechler.patrick.sc.objects.players;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Player;
import de.hechler.patrick.sc.interfaces.UnmovableEntity;
import de.hechler.patrick.sc.objects.AbsoluteManipulablePosition;
import de.hechler.patrick.sc.objects.HouseBuilding;
import de.hechler.patrick.sc.objects.World;

public class BotPlayer implements Player {
	
	private Set <MovableEntity>   myUnits;
	private Set <HouseBuilding>   myHouses;
	private Set <UnmovableEntity> buildings;
	
	private Map <Integer, Set <MovableEntity>> enemeyUnits;
	private Map <Integer, Set <HouseBuilding>> enemeyHouses;
	
	
	
	public BotPlayer() {
		myUnits = new HashSet <>();
		myHouses = new HashSet <>();
		buildings = new HashSet <>();
		enemeyUnits = new HashMap <>();
		enemeyHouses = new HashMap <>();
	}
	
	
	
	@Override
	public void makeTurn(World world, final int myID) {
		findEntitys(world, myID);
		// TODO Auto-generated method stub
	}
	
	private void findEntitys(World world, final int myID) {
		myUnits.clear();
		myHouses.clear();
		buildings.clear();
		enemeyUnits.clear();
		enemeyHouses.clear();
		
		final int xCnt = world.getXCnt(), yCnt = world.getYCnt();
		for (AbsoluteManipulablePosition amp = new AbsoluteManipulablePosition(0, 0); amp.x < xCnt; amp.x ++ ) {
			for (amp.y = 0; amp.y < yCnt; amp.y ++ ) {
				Field f = world.getField(amp);
				Entity e = f.getEntity();
				if (e == null) continue;
				if (e.isMovable()) {
					MovableEntity me = (MovableEntity) e;
					int ow = me.owner();
					if (ow == myID) {
						myUnits.add(me);
					} else {
						if ( !enemeyUnits.containsKey(ow)) {
							enemeyUnits.put(ow, new HashSet <>());
						}
						Set <MovableEntity> eme = enemeyUnits.get(ow);
						eme.add(me);
					}
				} else {
					if (e instanceof HouseBuilding) {
						HouseBuilding hb = (HouseBuilding) e;
						if ( !hb.inside().isEmpty()) {
							int ow = hb.inside().iterator().next().owner();
							if (ow == myID) {
								myHouses.add(hb);
							} else {
								if ( !enemeyHouses.containsKey(ow)) {
									enemeyHouses.put(ow, new HashSet <>());
								}
								Set <HouseBuilding> eue = enemeyHouses.get(ow);
								eue.add(hb);
							}
							return;
						}
					}
					UnmovableEntity ue = (UnmovableEntity) e;
					buildings.add(ue);
				}
			}
		}
	}
	
}
