package de.hechler.patrick.games.sc.world;

import java.util.Map;

import de.hechler.patrick.games.sc.Imagable;
import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.Value.BooleanValue;
import de.hechler.patrick.games.sc.values.Value.DoubleValue;
import de.hechler.patrick.games.sc.values.Value.EnumValue;
import de.hechler.patrick.games.sc.values.Value.IntValue;
import de.hechler.patrick.games.sc.values.Value.JustAValue;
import de.hechler.patrick.games.sc.values.Value.LongValue;
import de.hechler.patrick.games.sc.values.Value.StringValue;
import de.hechler.patrick.games.sc.values.Value.UserListValue;
import de.hechler.patrick.games.sc.values.Value.UserValue;

public interface WorldThing<T extends AddableType, M extends WorldThing<T, M>> extends Imagable {
	
	T type();
	
	M unmodifiable();
	
	Map<String, Value> values();
	
	Value value(String name);
	
	JustAValue justValue(String name);
	
	IntValue intValue(String name);
	
	LongValue longValue(String name);
	
	DoubleValue doubleValue(String name);
	
	BooleanValue booleanValue(String name);
	
	EnumValue<?> enumValue(String name);

	StringValue stringValue(String name);

	UserValue userValue(String name);

	UserListValue userListValue(String name);

	String X = "x";
	String Y = "y";
	
	default int x() {
		return intValue(X).value();
	}
	
	default int y() {
		return intValue(Y).value();
	}
	
}
