package de.hechler.patrick.games.squareconqerer.connect;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * this abstract class provides a simple wrapper around the {@link WrongInputHandler} interface to the to {@link #handleWrongInputByte(int)} and
 * {@link #handleWrongInputInt(int)} methods. when
 * 
 * @author pat
 */
public abstract class AbstractWrongInputHandler implements WrongInputHandler {
	
	protected Supplier<String> msg;
	
	protected void handleWrongInputInt(int value, boolean readwrite) throws IOException, StreamCorruptedException, EOFException {
		String m = this.msg.get();
		this.msg = null;
		throw new StreamCorruptedException(m);
	}
	
	protected void handleWrongInputByte(int value) throws IOException, StreamCorruptedException, EOFException {
		String m = this.msg.get();
		this.msg = null;
		throw new StreamCorruptedException(m);
	}
	
	private String msg(String msg) {
		String m = this.msg.get();
		this.msg = null;
		return "invalid input while handeling invalid input! first invalid input: " + m + " second invalid input: " + msg;
	}
	
	@Override
	public void wrongInputEOF(int expectedTotalLen, int missingLen) throws StreamCorruptedException, EOFException {
		if (this.msg != null) {
			throw new EOFException(msg("got: EOF but expected " + missingLen + " more bytes for something " + expectedTotalLen + " bytes big"));
		}
		throw new EOFException("invalid input! got: EOF but expected " + missingLen + " more bytes for something " + expectedTotalLen + " bytes big");
	}
	
	@Override
	public void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg("got: " + read + " after I wrote " + wrote + " but expected " + expectedRead));
		this.msg = () -> "got: " + read + " after I wrote " + wrote + " but expected " + expectedRead;
		handleWrongInputInt(expectedRead, true);
	}
	
	@Override
	public void wrongInputByte(int read, int expected) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg("got: " + read + " but expected " + expected));
		this.msg = () -> "got: " + read + " but expected " + expected;
		handleWrongInputByte(read);
	}
	
	@Override
	public void wrongInputByte(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg("got: " + read + " but expected " + expected + " or " + expected2));
		this.msg = () -> "got: " + read + " but expected " + expected + " or " + expected2;
		handleWrongInputByte(read);
	}
	
	@Override
	public void wrongInputByte(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg("got: " + read + " but expected on of: " + Arrays.toString(expected)));
		this.msg = () -> "got: " + read + " but expected on of: " + Arrays.toString(expected);
		handleWrongInputByte(read);
	}
	
	@Override
	public void wrongInputByte(int read, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg(msgGen.apply(read)));
		this.msg = () -> msgGen.apply(read);
		handleWrongInputByte(read);
	}
	
	@Override
	public void wrongInputInt(int read, int expected) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg("got: " + read + " but expected " + expected));
		this.msg = () -> "got: " + read + " but expected " + expected;
		handleWrongInputInt(read, false);
	}
	
	@Override
	public void wrongInputInt(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg("got: " + read + " but expected " + expected + " or " + expected2));
		this.msg = () -> "got: " + read + " but expected " + expected + " or " + expected2;
		handleWrongInputInt(read, false);
	}
	
	@Override
	public void wrongInputInt(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg("got: " + read + " but expected on of: " + Arrays.toString(expected)));
		this.msg = () -> "got: " + read + " but expected on of: " + Arrays.toString(expected);
		handleWrongInputInt(read, false);
	}
	
	@Override
	public void wrongInputInt(int read, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException {
		if (this.msg != null) throw new StreamCorruptedException(msg(msgGen.apply(read)));
		this.msg = () -> msgGen.apply(read);
		handleWrongInputInt(read, false);
	}
	
}
