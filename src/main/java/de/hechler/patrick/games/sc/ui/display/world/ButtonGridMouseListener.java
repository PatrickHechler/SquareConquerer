package de.hechler.patrick.games.sc.ui.display.world;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public interface ButtonGridMouseListener {
	
	default void pressed(MouseEvent e, int x, int y, int subx, int suby) {
	}
	
	default void moved(MouseEvent e, int x, int y, int subx, int suby, boolean dragging) {
	}
	
	default void released(MouseEvent e, int x, int y, int subx, int suby) {
	}
	
	default void whelling(MouseWheelEvent e, int x, int y, int subx, int suby) {
	}
	
}
