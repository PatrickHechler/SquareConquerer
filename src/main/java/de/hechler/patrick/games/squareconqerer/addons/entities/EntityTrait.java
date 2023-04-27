//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.addons.entities;


/**
 * this interface is used to represent a single trait/ability of an entity
 * 
 * @author pat
 */
public sealed interface EntityTrait {
	
	/**
	 * returns the name of the trait
	 * 
	 * @return the name of the trait
	 */
	String name();
	
	/**
	 * a number trait<br>
	 * for example something like max lives for example
	 */
	public record NumberTrait(String name, int minValue, int maxValue) implements EntityTrait {
		
		public NumberTrait {
			if (name == null) throw new NullPointerException("name is null");
			if (minValue > maxValue) {
				throw new IllegalStateException("minValue is greather than maxValue");
			}
		}
		
		public EntityTraitWithVal withVal(int value) {
			if (value < minValue) throw new IllegalArgumentException("the given value is too small");
			if (value > maxValue) throw new IllegalArgumentException("the given value is too large");
			return new EntityTraitWithVal.NumberTrait(this, value);
		}
		
	}
	
	/**
	 * a enum trait
	 *
	 * @param <E> the enum type
	 */
	public record EnumTrait<E extends Enum<?>>(String name, Class<E> cls) implements EntityTrait {
		
		public EnumTrait {
			if (name == null) throw new NullPointerException("name is null");
			if (cls == null) throw new NullPointerException("cls is null");
			if (!cls.isEnum()) throw new IllegalArgumentException("cls is no enum class");
		}
		
		public EntityTraitWithVal withVal(E value) {
			if (value == null) throw new NullPointerException("value is null");
			return new EntityTraitWithVal.EnumTrait<>(this, value);
		}
		
	}
	
	/**
	 * a boolean trait<br>
	 * for example something like has the trait or not
	 */
	public record BooleanTrait(String name) implements EntityTrait {
		
		public BooleanTrait {
			if (name == null) throw new NullPointerException("name is null");
		}
		
		public EntityTraitWithVal withVal(boolean value) {
			return new EntityTraitWithVal.BooleanTrait(this, value);
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
		
	}
	
}
