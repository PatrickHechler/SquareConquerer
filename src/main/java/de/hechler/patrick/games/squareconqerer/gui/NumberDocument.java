package de.hechler.patrick.games.squareconqerer.gui;

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
