package de.hechler.patrick.sc.objects;

import java.util.Collection;
import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Position;

public class CarriearUnit extends Unit {
	
	private Resources carries;
	private int       count;
	public final int  maxCount;
	
	
	
	public CarriearUnit(int owner, int maxCount, Position pos, Collection <Grounds> canExsistOn, int totalActions, Type type, int sight) {
		super(owner, pos, canExsistOn, totalActions, type, sight);
		this.maxCount = maxCount;
	}
	
	public CarriearUnit(int owner, int maxCount, int x, int y, Collection <Grounds> canExsistOn, int totalActions, int remainingActions, Type type, int sight) {
		this(owner, maxCount, new NicePosition(x, y), canExsistOn, totalActions, remainingActions, type, sight);
	}
	
	public CarriearUnit(int owner, Position pos, Set <Grounds> canExistOn, int carrierActions, Type type, int carrierSight) {
		this(owner, 2, pos, canExistOn, carrierActions, type, carrierSight);
	}
	
	public CarriearUnit(int owner, Position pos, Collection <Grounds> canExsistOn, int totalActions, Type type, int sight) {
		this(owner, 2, pos, canExsistOn, totalActions, type, sight);
	}
	
	public CarriearUnit(int owner, int x, int y, Collection <Grounds> canExsistOn, int totalActions, int remainingActions, Type type, int sight) {
		this(owner, 2, x, y, canExsistOn, totalActions, remainingActions, type, sight);
	}
	
	
	public CarriearUnit(MovableEntity copyExeptType, int maxCount, Type type) {
		super(copyExeptType, type);
		this.maxCount = maxCount;
	}
	
	public CarriearUnit(MovableEntity copyExeptType, Type type) {
		this(copyExeptType, 2, type);
	}
	
	
	protected CarriearUnit(int owner, int maxCount, NicePosition pos, Collection <Grounds> canExsistOn, int totalActions, int remainingActions, Type type, int sight) {
		super(owner, pos, canExsistOn, totalActions, remainingActions, type, sight);
		this.maxCount = maxCount;
	}
	
	protected CarriearUnit(int owner, NicePosition pos, Collection <Grounds> canExsistOn, int totalActions, int remainingActions, Type type, int sight) {
		this(owner, 2, pos, canExsistOn, totalActions, remainingActions, type, sight);
	}
	
	
	
	public Resources carries() {
		return carries;
	}
	
	public void carrie(Resources resource) throws IllegalStateException {
		if (count > 0) throw new IllegalStateException("can only change when not carrieing anything!");
		carries = resource;
	}
	
	public int count() {
		return count;
	}
	
	public void remove(int remCnt) throws IllegalStateException {
		if (count < remCnt) throw new IllegalStateException("i do not have so much resources!");
		count -= remCnt;
	}
	
	public void add(int addCnt) throws IllegalStateException {
		if (count + addCnt > maxCount) throw new IllegalStateException("i can not carrie so much!");
		count += addCnt;
	}
	
	public boolean isFull() {
		return maxCount >= count;
	}
	
	public boolean isEmpty() {
		return count <= 0;
	}
	
	public int freeCount() {
		return maxCount - count;
	}
	
}

