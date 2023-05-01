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

import java.util.Map;

/**
 * this interface is used to represent a single trait/ability of an entity
 * 
 * @author pat
 */
public sealed interface EntityTrait {
	
	static final String TRAIT_LIVES      = "lives";
	static final String TRAIT_MAX_LIVES  = "max lives";
	static final String TRAIT_VIEW_RANGE = "view range";
	
	static int intValue(Map<String, EntityTraitWithVal> traits, String name) {
		return ((EntityTraitWithVal.NumberTrait) traits.get(name)).value();
	}
	
	@SuppressWarnings("unchecked")
	static <T extends Enum<T>> T enumValue(Map<String, EntityTraitWithVal> traits, String name) {
		return ((EntityTraitWithVal.EnumTrait<T>) traits.get(name)).value();
	}
	
	static boolean booleanValue(Map<String, EntityTraitWithVal> traits, String name) {
		return ((EntityTraitWithVal.BooleanTrait) traits.get(name)).value();
	}
	
	/**
	 * returns the localized name of the trait
	 * 
	 * @return the localized name of the trait
	 */
	String name();
	
	/**
	 * returns this trait with its default value
	 * 
	 * @return this trait with its default value
	 */
	EntityTraitWithVal defaultValue();
	
	/**
	 * a number trait<br>
	 * for example something like max lives for example
	 */
	public record NumberTrait(String name, int minValue, int maxValue, int defaultIntValue) implements EntityTrait {
		
		public NumberTrait(String name, int constValue) {
			this(name, constValue, constValue, constValue);
		}
		
		public NumberTrait {
			if (name == null) throw new NullPointerException("name is null");
			if (minValue > maxValue) throw new IllegalArgumentException("minValue is greather than maxValue");
			if (minValue > defaultIntValue || defaultIntValue > maxValue) throw new IllegalArgumentException("the default value is invalid");
		}
		
		public EntityTraitWithVal withVal(int value) {
			if (value < minValue) throw new IllegalArgumentException("the given value is too small");
			if (value > maxValue) throw new IllegalArgumentException("the given value is too large");
			return new EntityTraitWithVal.NumberTrait(this, value);
		}
		
		public EntityTraitWithVal defaultValue() {
			return new EntityTraitWithVal.NumberTrait(this, this.defaultIntValue);
		}
		
	}
	
	/**
	 * a enum trait
	 *
	 * @param <E> the enum type
	 */
	public record EnumTrait<E extends Enum<?>>(String name, Class<E> cls, E defaultEnumValue) implements EntityTrait {
		
		public EnumTrait {
			if (name == null) throw new NullPointerException("name is null");
			if (cls == null) throw new NullPointerException("cls is null");
			if (defaultEnumValue == null) throw new NullPointerException("default value is null");
			if (!cls.isEnum()) throw new IllegalArgumentException("cls is no enum class");
			if (!cls.isInstance(defaultEnumValue)) throw new IllegalArgumentException("default value is no instance of the given class");
		}
		
		@SuppressWarnings("unchecked")
		public EntityTraitWithVal withVal(Enum<?> enum1) {
			if (enum1 == null) throw new NullPointerException("value is null");
			if (!this.cls.isInstance(enum1)) throw new IllegalArgumentException("the value is no instance of the given class");
			return new EntityTraitWithVal.EnumTrait<>(this, (E) enum1);
		}
		
		public EntityTraitWithVal defaultValue() {
			return new EntityTraitWithVal.EnumTrait<E>(this, this.defaultEnumValue);
		}
		
	}
	
	/**
	 * a boolean trait<br>
	 * for example something like has the trait or not
	 */
	public record BooleanTrait(String name, boolean defaultBoolValue) implements EntityTrait {
		
		public BooleanTrait {
			if (name == null) throw new NullPointerException("name is null");
		}
		
		public EntityTraitWithVal withVal(boolean value) {
			return new EntityTraitWithVal.BooleanTrait(this, value);
		}
		
		public EntityTraitWithVal defaultValue() {
			return new EntityTraitWithVal.BooleanTrait(this, this.defaultBoolValue);
		}
		
	}
	
	/**
	 * this is a trait, which is just there (it is not possible to disable/modify
	 * it)<br>
	 * for example something like can swim for a ship
	 */
	public record JustATrait(String name) implements EntityTrait {
		
		public JustATrait {
			if (name == null) throw new NullPointerException("name is null");
		}
		
		public EntityTraitWithVal withVal() {
			return new EntityTraitWithVal.JustATrait(this);
		}
		
		public EntityTraitWithVal defaultValue() {
			return new EntityTraitWithVal.JustATrait(this);
		}
		
	}
	
}
