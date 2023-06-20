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

import de.hechler.patrick.games.squareconqerer.Messages;

/**
 * this interface is used to represent a single trait/ability of an entity
 * 
 * @author pat
 */
public sealed interface EntityTrait {
	
	/** this string holds the name of the lives trait (theoretically it is not needed) */
	static final String TRAIT_LIVES      = Messages.getString("EntityTrait.trait-lives");      //$NON-NLS-1$
	/** this string holds the name of the maximum lives trait (theoretically it is not needed) */
	static final String TRAIT_MAX_LIVES  = Messages.getString("EntityTrait.trait-max-lives");  //$NON-NLS-1$
	/** this string holds the name of the view range trait (theoretically it is not needed) */
	static final String TRAIT_VIEW_RANGE = Messages.getString("EntityTrait.trait-view-range"); //$NON-NLS-1$
	
	/**
	 * returns the int value of the trait with the given value
	 * 
	 * @param traits the traits
	 * @param name   the name of the {@link NumberTrait}
	 * @return the int value of the trait with the given value
	 */
	static int intValue(Map<String, EntityTraitWithVal> traits, String name) {
		return ((EntityTraitWithVal.NumberTrait) traits.get(name)).value();
	}
	
	/**
	 * returns the enum value of the trait with the given value
	 * 
	 * @param traits the traits
	 * @param name   the name of the {@link EnumTrait}
	 * @return the enum value of the trait with the given value
	 */
	@SuppressWarnings("unchecked")
	static <T extends Enum<T>> T enumValue(Map<String, EntityTraitWithVal> traits, String name) {
		return ((EntityTraitWithVal.EnumTrait<T>) traits.get(name)).value();
	}
	
	/**
	 * returns the enum value of the trait with the given value
	 * 
	 * @param traits the traits
	 * @param name   the name of the {@link BooleanTrait}
	 * @return the enum value of the trait with the given value
	 */
	static boolean booleanValue(Map<String, EntityTraitWithVal> traits, String name) {
		return ((EntityTraitWithVal.BooleanTrait) traits.get(name)).value();
	}
	
	/**
	 * checks that the given trait is {@link JustATrait}
	 * 
	 * @param traits the traits
	 * @param name   the name of the {@link JustATrait}
	 * @return the {@link JustATrait} trait of the {@link EntityTraitWithVal}
	 */
	static JustATrait justAValue(Map<String, EntityTraitWithVal> traits, String name) {
		return ((EntityTraitWithVal.JustATrait) traits.get(name)).trait();
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
	 * 
	 * @param name            the name
	 * @param minValue        the minimum value
	 * @param maxValue        the maximum value
	 * @param defaultIntValue the default value
	 */
	public record NumberTrait(String name, int minValue, int maxValue, int defaultIntValue) implements EntityTrait {
		
		private static final String NAME_IS_NULL = Messages.getString("EntityTrait.no-name"); //$NON-NLS-1$
		private static final String MIN_VALUE_IS_GREATHER_THAN_MAX_VALUE = Messages.getString("EntityTrait.min-val-greather-max-val"); //$NON-NLS-1$
		private static final String THE_DEFAULT_VALUE_IS_INVALID = Messages.getString("EntityTrait.default-val-invalid"); //$NON-NLS-1$
		private static final String THE_GIVEN_VALUE_IS_TOO_SMALL = Messages.getString("EntityTrait.value-too-small"); //$NON-NLS-1$
		private static final String THE_GIVEN_VALUE_IS_TOO_LARGE = Messages.getString("EntityTrait.value-too-large"); //$NON-NLS-1$
		private static final String CLS_IS_NULL = Messages.getString("EntityTrait.no-class"); //$NON-NLS-1$
		private static final String DEFAULT_VALUE_IS_NULL = Messages.getString("EntityTrait.no-default-value"); //$NON-NLS-1$
		private static final String CLS_IS_NO_ENUM_CLASS = Messages.getString("EntityTrait.cls-not-enum"); //$NON-NLS-1$
		private static final String DEFAULT_VALUE_IS_NO_INSTANCE_OF_THE_GIVEN_CLASS = Messages.getString("EntityTrait.default-value-not-instance"); //$NON-NLS-1$
		private static final String VALUE_IS_NULL = Messages.getString("EntityTrait.no-value"); //$NON-NLS-1$
		private static final String THE_VALUE_IS_NO_INSTANCE_OF_THE_GIVEN_CLASS = Messages.getString("EntityTrait.value-not-instance"); //$NON-NLS-1$
		
		/**
		 * create a constant value number trait
		 * 
		 * @param name       the name
		 * @param constValue the value
		 */
		public NumberTrait(String name, int constValue) {
			this(name, constValue, constValue, constValue);
		}
		
		/**
		 * create a number trait with the given name, value range and default value
		 * 
		 * @param name            the name
		 * @param minValue        the minimum value
		 * @param maxValue        the maximum value
		 * @param defaultIntValue the default value
		 */
		public NumberTrait {
			if (name == null) throw new NullPointerException(NAME_IS_NULL);
			if (minValue > maxValue) throw new IllegalArgumentException(MIN_VALUE_IS_GREATHER_THAN_MAX_VALUE);
			if (minValue > defaultIntValue || defaultIntValue > maxValue) throw new IllegalArgumentException(THE_DEFAULT_VALUE_IS_INVALID);
		}
		
		/**
		 * returns an {@link EntityTraitWithVal.NumberTrait} with the given value
		 * 
		 * @param value the int value of the {@link EntityTraitWithVal.NumberTrait}
		 * @return an {@link EntityTraitWithVal.NumberTrait} with the given value
		 * @throws IllegalArgumentException if the value is outside of the range
		 */
		public EntityTraitWithVal.NumberTrait withVal(int value) throws IllegalArgumentException {
			if (value < this.minValue) throw new IllegalArgumentException(THE_GIVEN_VALUE_IS_TOO_SMALL);
			if (value > this.maxValue) throw new IllegalArgumentException(THE_GIVEN_VALUE_IS_TOO_LARGE);
			return new EntityTraitWithVal.NumberTrait(this, value);
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * this is the same as <code>{@link #withVal(int) withVal}({@link #defaultIntValue()})</code>
		 */
		@Override
		public EntityTraitWithVal.NumberTrait defaultValue() {
			return new EntityTraitWithVal.NumberTrait(this, this.defaultIntValue);
		}
		
	}
	
	/**
	 * a enum trait
	 * 
	 * @param name             the name
	 * @param cls              a enum class
	 * @param defaultEnumValue the default value
	 *
	 * @param <E>              the enum type
	 */
	public record EnumTrait<E extends Enum<?>>(String name, Class<E> cls, E defaultEnumValue) implements EntityTrait {
		
		/**
		 * create a new enum trait with the given values
		 * 
		 * @param name             the name
		 * @param cls              the enum class
		 * @param defaultEnumValue the default constant
		 */
		public EnumTrait {
			if (name == null) throw new NullPointerException(NumberTrait.NAME_IS_NULL);
			if (cls == null) throw new NullPointerException(NumberTrait.CLS_IS_NULL);
			if (defaultEnumValue == null) throw new NullPointerException(NumberTrait.DEFAULT_VALUE_IS_NULL);
			if (!cls.isEnum()) throw new IllegalArgumentException(NumberTrait.CLS_IS_NO_ENUM_CLASS);
			if (!cls.isInstance(defaultEnumValue)) throw new IllegalArgumentException(NumberTrait.DEFAULT_VALUE_IS_NO_INSTANCE_OF_THE_GIVEN_CLASS);
		}
		
		/**
		 * returns an {@link EntityTraitWithVal.EnumTrait} with the given value
		 * 
		 * @param value the enum value
		 * @return an {@link EntityTraitWithVal.EnumTrait} with the given value
		 * @throws IllegalArgumentException if the given value is no {@link Class#isInstance(Object) instance} of the {@link #cls() class}
		 */
		@SuppressWarnings("unchecked")
		public EntityTraitWithVal.EnumTrait<E> withVal(Enum<?> value) throws IllegalArgumentException {
			if (value == null) throw new NullPointerException(NumberTrait.VALUE_IS_NULL);
			if (!this.cls.isInstance(value)) throw new IllegalArgumentException(NumberTrait.THE_VALUE_IS_NO_INSTANCE_OF_THE_GIVEN_CLASS);
			return new EntityTraitWithVal.EnumTrait<>(this, (E) value);
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * this is the same as <code>{@link #withVal(Enum) withVal}({@link #defaultEnumValue()})</code>
		 * 
		 * @return an {@link EntityTraitWithVal.EnumTrait} with the given value
		 */
		@Override
		public EntityTraitWithVal.EnumTrait<E> defaultValue() {
			return new EntityTraitWithVal.EnumTrait<E>(this, this.defaultEnumValue);
		}
		
	}
	
	/**
	 * a boolean trait<br>
	 * for example something like has the trait or not
	 * 
	 * @param name             the name
	 * @param defaultBoolValue the default value
	 */
	public record BooleanTrait(String name, boolean defaultBoolValue) implements EntityTrait {
		
		/**
		 * creates a new boolean trait
		 * 
		 * @param name             the name
		 * @param defaultBoolValue the default value
		 */
		public BooleanTrait {
			if (name == null) throw new NullPointerException(NumberTrait.NAME_IS_NULL);
		}
		
		/**
		 * returns an {@link EntityTraitWithVal.BooleanTrait} with the given value
		 * 
		 * @param value the boolean value
		 * @return an {@link EntityTraitWithVal.BooleanTrait} with the given value
		 */
		public EntityTraitWithVal.BooleanTrait withVal(boolean value) {
			return new EntityTraitWithVal.BooleanTrait(this, value);
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * this is the same as <code>{@link #withVal(boolean) withVal}({@link #defaultBoolValue()})</code>
		 * 
		 * @return an {@link EntityTraitWithVal.EnumTrait} with the given value
		 */
		@Override
		public EntityTraitWithVal.BooleanTrait defaultValue() {
			return new EntityTraitWithVal.BooleanTrait(this, this.defaultBoolValue);
		}
		
	}
	
	/**
	 * this is a trait, which is just there (it is not possible to disable/modify it)<br>
	 * for example something like can swim for a ship
	 * 
	 * @param name the name
	 */
	public record JustATrait(String name) implements EntityTrait {
		
		/**
		 * creates a new trait with the given name
		 * 
		 * @param name the name
		 */
		public JustATrait {
			if (name == null) throw new NullPointerException(NumberTrait.NAME_IS_NULL);
		}
		
		/**
		 * returns a {@link EntityTraitWithVal.JustATrait} with this as {@link EntityTraitWithVal#trait()}
		 * 
		 * @return a {@link EntityTraitWithVal.JustATrait} with this as {@link EntityTraitWithVal#trait()}
		 */
		public EntityTraitWithVal.JustATrait withVal() {
			return new EntityTraitWithVal.JustATrait(this);
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * this is the same as invoking {@link #withVal()}
		 * 
		 * @return a new {@link EntityTraitWithVal.JustATrait}
		 */
		@Override
		public EntityTraitWithVal.JustATrait defaultValue() {
			return new EntityTraitWithVal.JustATrait(this);
		}
		
	}
	
}
