package de.hechler.patrick.sc.objects;

import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.Position;

public class ProducingBuilding extends StorageBuilding {
	
	public final Resources producing;
	
	protected int interval;
	protected int count;
	protected int producingCnt;
	
	
	public ProducingBuilding(UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remainingActions, int interval, int count, Resources producing, int producingCnt, int capacity) {
		super(pos, canExsistOn, type, totalActions, remainingActions, map(new Resources[] {producing }), capacity);
		this.interval = interval;
		this.count = count;
		this.producing = producing;
	}
	
	public ProducingBuilding(Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remainingActions, int interval, int count, Resources producing, int producingCnt, int capacity) {
		this(new UnchangeablePosition(pos), canExsistOn, type, totalActions, remainingActions, interval, count, producing, capacity, producingCnt);
	}
	
	public ProducingBuilding(UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int interval, Resources producing, int producingCnt, int capacity) {
		this(pos, canExsistOn, type, totalActions, 0, interval, 0, producing, producingCnt, capacity);
	}
	
	public ProducingBuilding(Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int interval, Resources producing, int producingCnt, int capacity) {
		this(new UnchangeablePosition(pos), canExsistOn, type, totalActions, interval, producing, producingCnt, capacity);
	}
	
	@Override
	public void newTurn() {
		count ++ ;
		if (count >= interval) {
			count = 0;
			store.get(producing).value += producingCnt;
		}
		super.newTurn();
	}
	
}
