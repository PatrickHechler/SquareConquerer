// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.addons.entities;

import java.io.IOException;
import java.io.StreamCorruptedException;

import de.hechler.patrick.games.squareconqerer.connect.Connection;

/**
 * this interface is used to represent a single trait/ability together with the
 * value of an entity
 * 
 * @author pat
 */
@SuppressWarnings("preview")
public sealed interface EntityTraitWithVal {
	
	/**
	 * returns the name of the trait
	 * 
	 * @return the name of the trait
	 */
	default String name() {
		return trait().name();
	}
	
	/**
	 * returns the trait
	 * 
	 * @return the trait
	 */
	EntityTrait trait();
	
	/**
	 * a number trait<br>
	 * for example something like max lives for example
	 */
	public record NumberTrait(EntityTrait.NumberTrait trait, int value) implements EntityTraitWithVal {
		
		public NumberTrait {
			if (trait == null) throw new NullPointerException("trait is null");
			if (value < trait.minValue()) throw new IllegalArgumentException("the given value is too small");
			if (value > trait.maxValue()) throw new IllegalArgumentException("the given value is too large");
		}
		
	}
	
	public record EnumTrait<E extends Enum<?>>(EntityTrait.EnumTrait<E> trait, E value) implements EntityTraitWithVal {
		
		public EnumTrait {
			if (trait == null) throw new NullPointerException("trait is null");
			if (value == null) throw new NullPointerException("value is null");
		}
		
	}
	
	/**
	 * a boolean trait<br>
	 * for example something like has the trait or not
	 */
	public record BooleanTrait(EntityTrait.BooleanTrait trait, boolean value) implements EntityTraitWithVal {
		
		public BooleanTrait {
			if (trait == null) throw new NullPointerException("trait is null");
		}
		
	}
	
	/**
	 * this is a trait, which is just there (it is not possible to disable/modify it)<br>
	 * for example can swim for a ship
	 */
	public record JustATrait(EntityTrait.JustATrait trait) implements EntityTraitWithVal {
		
		public JustATrait {
			if (trait == null) throw new NullPointerException("trait is null");
		}
		
	}
	
	static final int ST_JAT = 0x17FA0EAA;
	static final int ST_BT  = 0xDF28A844;
	static final int ST_NT  = 0x41CF008F;
	static final int ST_ET  = 0xFF5EF588;
	
	static void writeTrait(EntityTraitWithVal trait, Connection conn) throws IOException {
		switch (trait) {
		case JustATrait jat -> conn.writeInt(ST_JAT);
		case BooleanTrait bt -> {
			conn.writeInt(ST_BT);
			conn.writeByte(bt.value ? 1 : 0);
		}
		case NumberTrait nt -> {
			conn.writeInt(ST_NT);
			conn.writeInt(nt.value);
		}
		case EnumTrait<?> et -> {
			conn.writeInt(ST_ET);
			Class<? extends Enum<?>> cls = et.trait.cls();
			conn.writeClass(cls);
			conn.writeString(et.value.name());
			conn.writeInt(et.value.ordinal());
			conn.writeInt(cls.getEnumConstants().length);
		}
		}
	}
	
	static EntityTraitWithVal readTrait(EntityTrait trait, Connection conn) throws IOException {
		return switch (trait) {
		case EntityTrait.JustATrait jat -> {
			conn.readInt(ST_JAT);
			yield jat.withVal();
		}
		case EntityTrait.BooleanTrait bt -> {
			conn.readInt(ST_BT);
			yield bt.withVal(conn.readByte(0, 1) != 0);
		}
		case EntityTrait.NumberTrait nt -> {
			conn.readInt(ST_NT);
			yield nt.withVal(conn.readInt());
		}
		case EntityTrait.EnumTrait<?> et -> {
			conn.readInt(ST_ET);
			Class<?> cls = conn.readClass();
			if (cls != et.cls()) throw new StreamCorruptedException("did not read the expected class");
			String   name    = conn.readString();
			int      ordinal = conn.readPos();
			int      clen    = conn.readStrictPos();
			Object[] consts  = cls.getEnumConstants();
			if (clen != consts.length) throw new IllegalStateException("I have a different version of the enum class");
			Enum<?> e = (Enum<?>) consts[ordinal];
			if (!e.name().equals(name)) throw new IllegalStateException("I have a different version of the enum class");
			yield et.withVal(e);
		}
		};
	}
	
}
