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
package de.hechler.patrick.games.sc.ui.display.world.utils;

import java.util.ConcurrentModificationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * this document can be used to limits a document, so that there is always a valid number
 * 
 * @author Patrick Hechler
 */
public class FPNumberDocument extends PlainDocument {
	
	private static final long serialVersionUID = -9063163520415353449L;
	
	private static final Pattern PATTERN = Pattern.compile("^[0-9]+$"); //$NON-NLS-1$
	
	private final double minNum;
	private final double maxNum;
	
	/**
	 * create a new {@link NumberDocument} which is always between
	 * 
	 * @param minNum the minimum number must be at max 0
	 * @param maxNum the maximum number must be at min 0
	 */
	public FPNumberDocument(double minNum, double maxNum) {
		this.minNum = minNum;
		this.maxNum = maxNum;
		if (minNum > maxNum) {
			throw new IllegalArgumentException("the maximum number is below the minimum number max: " + maxNum + " min: " + minNum);
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
				if (this.minNum > 9 && val >= 0) {
					super.replace(0, getLength(), Double.toString(this.minNum), a);
				} else if (this.maxNum < 0 && val >= 0) {
					super.replace(0, getLength(), Double.toString(this.maxNum), a);
				}
				return;
			}
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			return;
		}
		super.insertString(offs, str, a);
	}
	
	/**
	 * returns the current number of this document
	 * <p>
	 * note that if the document contains an empty or invalid string this method returns 0 (even if 0 is outside of the allowed bounds)<br>
	 * this call is equivalent to {@link #getNumber(double) getNumber(0D)}
	 * 
	 * @return the current number of this document
	 * 
	 * @see #getNumber(double)
	 */
	public double getNumber() {
		return getNumber(0D);
	}
	
	/**
	 * returns the current number of this document
	 * <br>
	 * if the document contains an empty or invalid string this method returns <code>empty</code> (even if <code>empty</code> is outside of the allowed bounds)
	 * 
	 * @param empty the value to be returned when the document holds an empty string
	 * 
	 * @return the number of this document or <code>empty</code> if there is none
	 */
	public double getNumber(double empty) {
		if (getLength() == 0) {
			return empty;
		}
		try {
			double val = Double.parseDouble(getText(0, getLength()));
			if (val < this.minNum || val > this.maxNum) return empty;
			return val;
		} catch (NumberFormatException | BadLocationException e) {
			throw new ConcurrentModificationException(e);
		}
	}
	
}
