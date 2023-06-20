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
import java.text.Format;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.connect.Connection;

/**
 * this interface is used to represent a single trait/ability together with the value of an entity
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
	 * 
	 * @param trait the {@link #trait()}
	 * @param value the value
	 */
	public record NumberTrait(EntityTrait.NumberTrait trait, int value) implements EntityTraitWithVal {
		
		private static final String TRAIT_IS_NULL = Messages.getString("EntityTraitWithVal.no-trait"); //$NON-NLS-1$
		private static final String THE_GIVEN_VALUE_IS_TOO_SMALL = Messages.getString("EntityTraitWithVal.value-too-small"); //$NON-NLS-1$
		private static final String THE_GIVEN_VALUE_IS_TOO_LARGE = Messages.getString("EntityTraitWithVal.value-too-large"); //$NON-NLS-1$
		private static final String VALUE_IS_NULL = Messages.getString("EntityTraitWithVal.no-value"); //$NON-NLS-1$
		private static final String I_HAVE_A_DIFFERENT_VERSION_OF_THE_ENUM_CLASS = Messages.getString("EntityTraitWithVal.different-version-of-enum-class"); //$NON-NLS-1$
		private static final Format DID_NOT_READ_THE_EXPECTED_CLASS = Messages.getFormat("EntityTraitWithVal.read-unexpected-enum-class"); //$NON-NLS-1$
		
		/**
		 * creates a new number trait
		 * 
		 * @param trait the {@link #trait()}
		 * @param value the {@link #value()}
		 */
		public NumberTrait {
			if (trait == null) throw new NullPointerException(TRAIT_IS_NULL);
			if (value < trait.minValue()) throw new IllegalArgumentException(THE_GIVEN_VALUE_IS_TOO_SMALL);
			if (value > trait.maxValue()) throw new IllegalArgumentException(THE_GIVEN_VALUE_IS_TOO_LARGE);
		}
		
	}
	
	/**
	 * a enum trait
	 * 
	 * @author Patrick Hechler
	 * @param trait the {@link #trait()}
	 * @param value the value
	 * @param <E>   the enum type
	 */
	public record EnumTrait<E extends Enum<?>>(EntityTrait.EnumTrait<E> trait, E value) implements EntityTraitWithVal {
		
		/**
		 * creates a new enum trait
		 * 
		 * @param trait the {@link #trait()}
		 * @param value the {@link #value()}
		 */
		public EnumTrait {
			if (trait == null) throw new NullPointerException(NumberTrait.TRAIT_IS_NULL);
			if (value == null) throw new NullPointerException(NumberTrait.VALUE_IS_NULL);
		}
		
	}
	
	/**
	 * a boolean trait<br>
	 * for example something like has the trait or not
	 * 
	 * @param trait the {@link #trait()}
	 * @param value the value
	 */
	public record BooleanTrait(EntityTrait.BooleanTrait trait, boolean value) implements EntityTraitWithVal {
		
		/**
		 * creates a new boolean trait
		 * 
		 * @param trait the {@link #trait()}
		 * @param value the value
		 */
		public BooleanTrait {
			if (trait == null) throw new NullPointerException(NumberTrait.TRAIT_IS_NULL);
		}
		
	}
	
	/**
	 * this is a trait, which is just there (it is not possible to disable/modify it)<br>
	 * for example can swim for a ship
	 * 
	 * @param trait the {@link #trait()}
	 */
	public record JustATrait(EntityTrait.JustATrait trait) implements EntityTraitWithVal {
		
		/**
		 * creates a new trait value
		 * 
		 * @param trait the {@link #trait()}
		 */
		public JustATrait {
			if (trait == null) throw new NullPointerException(NumberTrait.TRAIT_IS_NULL);
		}
		
	}
	
	/** @see #writeTrait(EntityTraitWithVal, Connection) */
	static final int ST_JAT = 0x17FA0EAA;
	/** @see #writeTrait(EntityTraitWithVal, Connection) */
	static final int ST_BT  = 0xDF28A844;
	/** @see #writeTrait(EntityTraitWithVal, Connection) */
	static final int ST_NT  = 0x41CF008F;
	/** @see #writeTrait(EntityTraitWithVal, Connection) */
	static final int ST_ET  = 0xFF5EF588;
	
	/**
	 * writes the given trait
	 * <ul>
	 * <li>if the trait is a {@link JustATrait}:
	 * <ol>
	 * <li>{@link Connection#writeInt(int) writes} {@value #ST_JAT}</li>
	 * </ol>
	 * </li>
	 * <li>if the trait is a {@link BooleanTrait}:
	 * <ol>
	 * <li>{@link Connection#writeInt(int) writes} {@value #ST_BT}</li>
	 * <li>{@link Connection#writeByte(int) writes} <code>{@link BooleanTrait#value()} ? 1 : 0</code></li>
	 * </ol>
	 * </li>
	 * <li>if the trait is a {@link NumberTrait}:
	 * <ol>
	 * <li>{@link Connection#writeInt(int) writes} {@value #ST_NT}</li>
	 * <li>{@link Connection#writeInt(int) writes} {@link NumberTrait#value()}</li>
	 * </ol>
	 * </li>
	 * <li>if the trait is a {@link EnumTrait}:
	 * <ol>
	 * <li>{@link Connection#writeInt(int) writes} {@value #ST_ET}</li>
	 * <li>{@link Connection#writeClass(Class) writes} the {@link EntityTrait.EnumTrait#cls() class}</li>
	 * <li>{@link Connection#writeString(String) writes} the {@link EnumTrait#value() values} {@link Enum#name() name}</li>
	 * <li>{@link Connection#writeInt(int) writes} the {@link EnumTrait#value() values} {@link Enum#ordinal() ordinal}</li>
	 * <li>{@link Connection#writeClass(Class) writes} the {@link Class#getEnumConstants() enum constant} count of {@link EntityTrait.EnumTrait#cls() class}</li>
	 * </ol>
	 * </li>
	 * </ul>
	 * 
	 * @param trait the trait to send
	 * @param conn  the connection
	 * @throws IOException if an IO error occurs
	 */
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
	
	/**
	 * reads a {@link EntityTraitWithVal} which holds the given trait
	 * <p>
	 * if the trait which was read is not from the same type as the given trait, this operation fails
	 * 
	 * @param trait the trait of the value to read
	 * @param conn  the connection
	 * @return the value of the trait which was read
	 * @throws IOException if an IO error occurs
	 * @see #writeTrait(EntityTraitWithVal, Connection)
	 */
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
			if (cls != et.cls()) throw new StreamCorruptedException(Messages.format(NumberTrait.DID_NOT_READ_THE_EXPECTED_CLASS, et.cls(), cls));
			String   name    = conn.readString();
			int      ordinal = conn.readPos();
			int      clen    = conn.readStrictPos();
			Object[] consts  = cls.getEnumConstants();
			if (clen != consts.length) throw new IllegalStateException(NumberTrait.I_HAVE_A_DIFFERENT_VERSION_OF_THE_ENUM_CLASS);
			Enum<?> e = (Enum<?>) consts[ordinal];
			if (!e.name().equals(name)) throw new IllegalStateException(NumberTrait.I_HAVE_A_DIFFERENT_VERSION_OF_THE_ENUM_CLASS);
			yield et.withVal(e);
		}
		};
	}
	
}
