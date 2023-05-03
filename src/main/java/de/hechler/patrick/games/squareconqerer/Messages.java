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
package de.hechler.patrick.games.squareconqerer;

import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * this class manages the externalized strings
 */
public class Messages {
	
	private static final Locale         LOCALE          = Locale.getDefault(Category.DISPLAY);
	private static final String         BUNDLE_NAME     = Messages.class.getPackageName() + ".messages";                            //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, LOCALE, Messages.class.getModule());
	
	private Messages() {}
	
	/**
	 * returns the value of the given key
	 * 
	 * @param key the key of the value
	 * @return the value with the given key
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			throw new AssertionError("missing key: '" + key + "': " + e, e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * returns the format value of the given key
	 * 
	 * @param key the key of the value
	 * @return the value with the given key
	 */
	public static MessageFormat getFormat(String key) {
		try {
			return new MessageFormat(RESOURCE_BUNDLE.getString(key), LOCALE);
		} catch (MissingResourceException e) {
			throw new AssertionError("missing key: '" + key + "': " + e, e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * lets the formatter format the given arguments
	 * <p>
	 * this method hides the array creation in with the var-args
	 * 
	 * @param format the format
	 * @param args   the arguments array
	 * @return the formatted string
	 */
	public static String format(Format format, Object... args) {
		return format.format(args);
	}
	
}
