package de.hechler.patrick.games.squareconqerer.objects;

import java.io.IOException;
import java.io.OutputStream;

public class IgnoreCloseOutputStream extends OutputStream {
	
	private final OutputStream out;
	
	public IgnoreCloseOutputStream(OutputStream out) { this.out = out; }
	
	@Override
	public void write(int b) throws IOException { out.write(b); }
	
	@Override
	public void write(byte[] b) throws IOException { out.write(b); }
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException { out.write(b, off, len); }
	
	@Override
	public void flush() throws IOException { out.flush(); }
	
}
