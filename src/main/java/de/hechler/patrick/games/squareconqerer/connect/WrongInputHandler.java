package de.hechler.patrick.games.squareconqerer.connect;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.function.IntFunction;

/**
 * this interface is there, so that race conditions where the server and the client both want to send something at the same time can be cleared without throwing an
 * error
 * <p>
 * except for the {@link #wrongInputEOF(int, int)} method all methods are allowed to return normally<br>
 * If a method returns normally, the Connection will retry the last operation.<br>
 * note that the retry implies that {@link #wrongInputEOF(int, int)} will result in a loop until it throws an error
 * 
 * @author pat
 */
public interface WrongInputHandler {
	
	/**
	 * this method should never return normally, otherwise the thread will come to an infinite loop (until this method does throw an exception or the underlying
	 * stream does (because it was closed or so))
	 * 
	 * @param expectedTotalLen the total length of the expected structure
	 * @param missingLen       the missing length of the expected structure
	 * @throws StreamCorruptedException always
	 */
	void wrongInputEOF(int expectedTotalLen, int missingLen) throws StreamCorruptedException, EOFException;
	
	void wrongInputByte(int read, int expected) throws IOException, StreamCorruptedException, EOFException;
	void wrongInputByte(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException;
	void wrongInputByte(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException;
	void wrongInputByte(int read, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException;
	
	void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException;
	
	void wrongInputInt(int read, int expected) throws IOException, StreamCorruptedException, EOFException;
	void wrongInputInt(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException;
	void wrongInputInt(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException;
	void wrongInputInt(int read, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException;
	
}
