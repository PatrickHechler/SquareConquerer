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
package de.hechler.patrick.games.squareconqerer.stuff;

import java.io.IOException;
import java.io.OutputStream;

import de.hechler.patrick.games.squareconqerer.Messages;

/**
 * used for streams which just fails
 * 
 * @author Patrick Hechler
 */
public class InvalidOutputStream extends OutputStream {
	
	private static final String THIS_STREAM_IS_INVALID = Messages.getString("InvalidOutputStream.invalid-stream"); //$NON-NLS-1$
	
	/**
	 * the instance of the {@link InvalidOutputStream}
	 */
	public static final InvalidOutputStream INSTANCE = new InvalidOutputStream();
	
	
	/**
	 * throws an {@link UnsupportedOperationException}
	 * 
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException(THIS_STREAM_IS_INVALID);
	}
	
	/**
	 * throws an {@link UnsupportedOperationException}
	 * 
	 * @throws UnsupportedOperationException always
	 */
	@Override
	@SuppressWarnings("unused")
	public void write(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException(THIS_STREAM_IS_INVALID);
	}
	
}
