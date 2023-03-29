package de.hechler.patrick.games.squareconqerer.objects;

import java.io.IOException;
import java.io.OutputStream;

public class InvalidOutputStream extends OutputStream {
	
	public static final InvalidOutputStream INSTANCE = new InvalidOutputStream();
	
	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException("this stream is invalid");
	}
	
}
