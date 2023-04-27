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
 * this interface is used to represent a single trait/ability together with the
 * value of an entity
 * 
 * @author pat
 */
public sealed interface EntityTraitWithVal {
	
	/**
	 * returns the name of the trait
	 * 
	 * @return the name of the trait
	 */
	default String name() {
		return trait().name();
	}
	
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
	 * this is a trait, which is just there (it is not possible to disable/modify
	 * it)<br>
	 * for example can swim for a ship
	 */
	public record JustATrait(EntityTrait.JustATrait trait) implements EntityTraitWithVal {
		
		public JustATrait {
			if (trait == null) throw new NullPointerException("trait is null");
		}
		
	}
	
}
