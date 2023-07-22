package de.hechler.patrick.games.sc.world.def;

import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.WorldType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.spec.IntSpec;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.utils.objects.ACORNRandom;

public class DefWorldType extends WorldType {
	
	public static final String X_LEN = "X-LEN";
	public static final String Y_LEN = "Y-LEN";
	
	public static final DefWorldType WORLD_TYPE = new DefWorldType();
	
	private DefWorldType() {
		super("base:default:world", "Square Conquerer World", Map.of(//
				X_LEN, new IntSpec(X_LEN, "X Length/Width", 1, Integer.MAX_VALUE), //
				Y_LEN, new IntSpec(Y_LEN, "Y Length/Heigth", 1, Integer.MAX_VALUE)//
		));
	}
	
	@Override
	public World withValues(Map<String, Value> values, UUID uuid) throws TurnExecutionException {
		int xlen = ((IntValue)values.get(X_LEN)).value();
		int ylen = ((IntValue)values.get(Y_LEN)).value();
		return new DefWorld(uuid, xlen, ylen);
	}
	
	@Override
	public World withDefaultValues(World w, ACORNRandom r, int x, int y) {
		return new DefWorld(World.NULL_UUID, 128, 128);
	}
	
	@Override
	public World withRandomValues(World w, ACORNRandom r, int x, int y) {
		int xlen = r.nextInt(16, 128);
		int ylen = r.nextInt(16, 128);
		return new DefWorld(World.NULL_UUID, xlen, ylen);
	}
	
}
