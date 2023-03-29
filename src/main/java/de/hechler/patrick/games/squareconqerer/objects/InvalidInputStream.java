package de.hechler.patrick.games.squareconqerer.objects;

import java.io.IOException;
import java.io.InputStream;

public class InvalidInputStream extends InputStream {
	
	public static final InvalidInputStream INSTANCE = new InvalidInputStream();
	
	@Override
	public int read() throws IOException {
		throw new UnsupportedOperationException("this stream is invalid");
	}
	
}
