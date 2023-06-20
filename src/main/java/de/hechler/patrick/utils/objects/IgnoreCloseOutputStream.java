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
package de.hechler.patrick.utils.objects;

import java.io.IOException;
import java.io.OutputStream;

/**
 * this class is used to use a stream which should not be closed
 * 
 * @author Patrick Hechler
 */
public class IgnoreCloseOutputStream extends OutputStream {
	
	private final OutputStream out;
	
	/**
	 * creates a new output stream which just delegates to the given input stream, but ignores the {@link #close()} operation
	 * 
	 * @param out the output stream on which this ignore close stream should delegate to
	 */
	public IgnoreCloseOutputStream(OutputStream out) { this.out = out; }
	
	/** {@inheritDoc} */
	@Override
	public void write(int b) throws IOException { out.write(b); }
	
	/** {@inheritDoc} */
	@Override
	public void write(byte[] b) throws IOException { out.write(b); }
	
	/** {@inheritDoc} */
	@Override
	public void write(byte[] b, int off, int len) throws IOException { out.write(b, off, len); }
	
	/** {@inheritDoc} */
	@Override
	public void flush() throws IOException { out.flush(); }
	
	/**
	 * does nothing and just returns
	 */
	@Override
	public void close() throws IOException {/**/}
	
}
