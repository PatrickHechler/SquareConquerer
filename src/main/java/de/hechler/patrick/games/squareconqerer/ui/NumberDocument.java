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


public class NumberDocument extends PlainDocument implements Document {
	
	private static final long serialVersionUID = -9063163520415353449L;
	
	public static final Pattern PATTERN = Pattern.compile("^[0-9]+$");

	private final int minNum;
	private final int maxNum;
	
	public NumberDocument(int minNum, int maxNum) {
		this.minNum = minNum;
		this.maxNum = maxNum;
		if (minNum > 0 || maxNum < 0) {
			throw new IllegalArgumentException("zero must be a valid number!");
		}
	}
	
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
			int val = Integer.parseInt(str0);
			if (val < minNum || val > maxNum) {
				return;
			}
		} catch (NumberFormatException e) {
			return;
		}
		super.insertString(offs, str, a);
	}
	
	public int getNumber() {
		if (getLength() == 0) {
			return 0;
		} else {
			try {
				return Integer.parseInt(getText(0, getLength()));
			} catch (NumberFormatException | BadLocationException e) {
				throw new ConcurrentModificationException(e);
			}
		}
	}
	
}
