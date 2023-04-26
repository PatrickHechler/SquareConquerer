package de.hechler.patrick.games.squareconqerer.connect;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.function.IntFunction;

/**
 * this interface is there, so that race conditions where the server and the client both want to send something at the same time can be cleared without throwing an
 * error<br>
 * In such a race condition ({@link #wrongInputWRInt(int, int, int)}) the clients request is firstly executed and then the server can resent its message
 * <p>
 * except for the {@link #wrongInputEOF(int, int)} method all methods are allowed to return normally<br>
 * If a method returns normally, the Connection will return the value returned by the {@link WrongInputHandler}.<br>
 * note that the {@link #wrongInputEOF(int, int)} is the only method which is not allowed to return normally.<br>
 * if it does, the result is unspecified
 * <p>
 * note that the return value of the {@link WrongInputHandler} is directly returned by the {@link Connection}
 * 
 * @author pat
 */
public interface WrongInputHandler {
	
	/**
	 * this method should never return normally, otherwise the behavior of the {@link Connection} is unspecified
	 * 
	 * @param expectedTotalLen the total length of the expected structure
	 * @param missingLen       the missing length of the expected structure
	 * @throws StreamCorruptedException if no {@link EOFException} is thrown
	 * @throws EOFException             if no {@link StreamCorruptedException} is thrown
	 */
	default void wrongInputEOF(int expectedTotalLen, int missingLen) throws StreamCorruptedException, EOFException {
		Connection.DEFAULT_WRONG_INPUT.wrongInputEOF(expectedTotalLen, missingLen);
	}
	
	// this is the most interesting method
	default void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException {
		Connection.DEFAULT_WRONG_INPUT.wrongInputWRInt(read, wrote, expectedRead);
	}
	
	default void wrongInputByte(int read, int expected) throws IOException, StreamCorruptedException, EOFException {
		Connection.DEFAULT_WRONG_INPUT.wrongInputByte(read, expected);
	}
	
	default int wrongInputByte(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException {
		return Connection.DEFAULT_WRONG_INPUT.wrongInputByte(read, expected, expected2);
	}
	
	default int wrongInputByte(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException {
		return Connection.DEFAULT_WRONG_INPUT.wrongInputByte(read, expected);
	}
	
	default int wrongInputByte(int read, int[] expected, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException {
		return Connection.DEFAULT_WRONG_INPUT.wrongInputByte(read, expected, msgGen);
	}
	
	default void wrongInputInt(int read, int expected) throws IOException, StreamCorruptedException, EOFException {
		Connection.DEFAULT_WRONG_INPUT.wrongInputInt(read, expected);
	}
	
	default int wrongInputInt(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException {
		return Connection.DEFAULT_WRONG_INPUT.wrongInputInt(read, expected, expected2);
	}
	
	default int wrongInputInt(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException {
		return Connection.DEFAULT_WRONG_INPUT.wrongInputInt(read, expected);
	}
	
	default int wrongInputInt(int read, int[] expected, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException {
		return Connection.DEFAULT_WRONG_INPUT.wrongInputInt(read, expected, msgGen);
	}
	
	default int wrongInputPositive(int read, boolean strictlyPositive) throws IOException, StreamCorruptedException, EOFException {
		return Connection.DEFAULT_WRONG_INPUT.wrongInputPositive(read, strictlyPositive);
	}
	
}
