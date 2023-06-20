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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * this class is used to use a stream which should not be closed
 * 
 * @author Patrick Hechler
 */
public class IgnoreCloseInputStream extends InputStream {
	
	private final InputStream in;
	
	/**
	 * creates a new input stream which just delegates to the given input stream, but ignores the {@link #close()} operation
	 * 
	 * @param in the input stream on which this ignore close stream should delegate to
	 */
	public IgnoreCloseInputStream(InputStream in) { this.in = in; }
	
	/** {@inheritDoc} */
	@Override
	public int read() throws IOException { return this.in.read(); }
	
	/** {@inheritDoc} */
	@Override
	public int read(byte[] b) throws IOException { return this.in.read(b); }
	
	/** {@inheritDoc} */
	@Override
	public int read(byte[] b, int off, int len) throws IOException { return this.in.read(b, off, len); }
	
	/** {@inheritDoc} */
	@Override
	public byte[] readAllBytes() throws IOException { return this.in.readAllBytes(); }
	
	/** {@inheritDoc} */
	@Override
	public byte[] readNBytes(int len) throws IOException { return this.in.readNBytes(len); }
	
	/** {@inheritDoc} */
	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException { return this.in.readNBytes(b, off, len); }
	
	/** {@inheritDoc} */
	@Override
	public long skip(long n) throws IOException { return this.in.skip(n); }
	
	/** {@inheritDoc} */
	@Override
	public void skipNBytes(long n) throws IOException { this.in.skipNBytes(n); }
	
	/** {@inheritDoc} */
	@Override
	public int available() throws IOException { return this.in.available(); }
	
	/** {@inheritDoc} */
	@Override
	public void mark(int readlimit) { this.in.mark(readlimit); }
	
	/** {@inheritDoc} */
	@Override
	public void reset() throws IOException { this.in.reset(); }
	
	/** {@inheritDoc} */
	@Override
	public boolean markSupported() { return this.in.markSupported(); }
	
	/** {@inheritDoc} */
	@Override
	public long transferTo(OutputStream out) throws IOException { return this.in.transferTo(out); }
	
	/**
	 * does nothing and just returns
	 */
	@Override
	public void close() throws IOException {/**/}
	
}
