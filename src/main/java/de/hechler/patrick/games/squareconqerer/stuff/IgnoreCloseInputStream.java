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
import java.io.InputStream;
import java.io.OutputStream;

public class IgnoreCloseInputStream extends InputStream {
	
	private final InputStream in;
	
	public IgnoreCloseInputStream(InputStream in) { this.in = in; }

	@Override
	public int read() throws IOException { return in.read(); }

	@Override
	public int read(byte[] b) throws IOException { return in.read(b); }

	@Override
	public int read(byte[] b, int off, int len) throws IOException { return in.read(b, off, len); }

	@Override
	public String toString() { return in.toString(); }

	@Override
	public byte[] readAllBytes() throws IOException { return in.readAllBytes(); }

	@Override
	public byte[] readNBytes(int len) throws IOException { return in.readNBytes(len); }

	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException { return in.readNBytes(b, off, len); }

	@Override
	public long skip(long n) throws IOException { return in.skip(n); }

	@Override
	public void skipNBytes(long n) throws IOException { in.skipNBytes(n); }

	@Override
	public int available() throws IOException { return in.available(); }

	@Override
	public void mark(int readlimit) { in.mark(readlimit); }

	@Override
	public void reset() throws IOException { in.reset(); }

	@Override
	public boolean markSupported() { return in.markSupported(); }

	@Override
	public long transferTo(OutputStream out) throws IOException { return in.transferTo(out); }
	
}
