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
package de.hechler.patrick.games.squareconqerer.ui;

import java.util.ConcurrentModificationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import de.hechler.patrick.games.squareconqerer.Messages;

/**
 * this document can be used to limits a document, so that there is always a valid number
 * 
 * @author Patrick Hechler
 */
public class NumberDocument extends PlainDocument implements Document {
	
	private static final String THE_MAXIMUM_NUMBER_IS_LOWER_THAN_THE_MINIMUM_NUMBER_IS_NOT_VALID = Messages.getString("NumberDocument.max-lower-than-min"); //$NON-NLS-1$
	private static final String ZERO_MUST_BE_A_VALID_NUMBER                                      = Messages.getString("NumberDocument.zero-is-invalid");    //$NON-NLS-1$
	
	private static final long serialVersionUID = -9063163520415353449L;
	
	private static final Pattern PATTERN = Pattern.compile("^[0-9]+$"); //$NON-NLS-1$
	
	private final int minNum;
	private final int maxNum;
	
	/**
	 * create a new {@link NumberDocument} which is always between
	 * 
	 * @param minNum the minimum number must be at max 0
	 * @param maxNum the maximum number must be at min 0
	 */
	public NumberDocument(int minNum, int maxNum) {
		this.minNum = minNum;
		this.maxNum = maxNum;
		if (minNum > 0 || maxNum < 0) {
			throw new IllegalArgumentException(ZERO_MUST_BE_A_VALID_NUMBER);
		}
		if (minNum > maxNum) {
			throw new IllegalArgumentException(THE_MAXIMUM_NUMBER_IS_LOWER_THAN_THE_MINIMUM_NUMBER_IS_NOT_VALID);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		if (str == null || str.isEmpty()) {
			return;
		}
		Matcher matcher = PATTERN.matcher(str);
		if (!matcher.matches()) {
			return;
		}
		StringBuilder b = new StringBuilder();
		if (offs != 0) {
			b.append(getText(0, offs));
		}
		b.append(str);
		if (offs != getLength()) {
			b.append(getText(offs, getLength() - offs));
		}
		try {
			String str0 = b.toString();
			int    val  = Integer.parseInt(str0);
			if (val < this.minNum || val > this.maxNum) {
				return;
			}
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			return;
		}
		super.insertString(offs, str, a);
	}
	
	/**
	 * returns the current number of this document
	 * 
	 * @return the current number of this document
	 */
	public int getNumber() {
		if (getLength() == 0) {
			return 0;
		}
		try {
			return Integer.parseInt(getText(0, getLength()));
		} catch (NumberFormatException | BadLocationException e) {
			throw new ConcurrentModificationException(e);
		}
	}
	
}
