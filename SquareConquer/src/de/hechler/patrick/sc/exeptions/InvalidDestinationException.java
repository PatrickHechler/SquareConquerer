package de.hechler.patrick.sc.exeptions;

import java.util.Collection;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.interfaces.Field;

public class InvalidDestinationException extends RuntimeException {
	
	/** UID */
	private static final long serialVersionUID = 4995410013328785730L;
	
	
	public InvalidDestinationException(String msg) {
		super(msg);
	}
	
	public InvalidDestinationException(Direction dir) {
		super("The direction '" + dir + "' is invalid");
	}
	
	public InvalidDestinationException(Direction dir, String reason) {
		super("The direction '" + dir + "' is invalid: " + reason);
	}
	
	public InvalidDestinationException(Direction dir, Grounds invalid) {
		super("The direction '" + dir + "' is invalid, you would land on: '" + invalid + "'");
	}
	
	public InvalidDestinationException(Direction dir, Collection <Grounds> valid) {
		super("The direction '" + dir + "' is invalid, valid would be: '" + valid + "'");
	}
	
	public InvalidDestinationException(Direction dir, Grounds invalid, Collection <Grounds> valid) {
		super("The direction '" + dir + "' is invalid, you would land on: '" + invalid + "', but valid is only: '" + valid + "'");
	}
	
	public InvalidDestinationException(Field position, Direction dir, Grounds invalid, Collection <Grounds> valid) {
		super("The direction '" + dir + "' fronm the field '" + position + "' is invalid, you would land on: '" + invalid + "', but valid is only: '" + valid + "'");
	}
	
}
