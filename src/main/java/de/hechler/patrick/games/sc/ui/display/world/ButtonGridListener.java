package de.hechler.patrick.games.sc.ui.display.world;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public interface ButtonGridListener {
	
	default void kTyped(KeyEvent e) {}
	
	default void mPressed(MouseEvent e, int x, int y, int subx, int suby) {}
	
	default void mMoved(MouseEvent e, int x, int y, int subx, int suby, boolean dragging) {}
	
	default void mReleased(MouseEvent e, int x, int y, int subx, int suby) {}
	
	default void mWhelling(MouseWheelEvent e, int x, int y, int subx, int suby) {}
	
}
