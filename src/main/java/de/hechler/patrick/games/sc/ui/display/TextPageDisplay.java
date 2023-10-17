package de.hechler.patrick.games.sc.ui.display;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.hechler.patrick.games.sc.ui.pages.TextOnlyPage;

public class TextPageDisplay {
	
	private TextPageDisplay() {}
	
	public static void display(TextOnlyPage p) {
		JDialog d = new JDialog();
		display(p, d, null);
	}
	
	public static JDialog display(TextOnlyPage p, Frame f) {
		JDialog d = new JDialog(f);
		return display(p, d, f);
	}
	
	public static JDialog display(TextOnlyPage p, Window w) {
		JDialog d = new JDialog(w);
		return display(p, d, w);
	}
	
	public static JDialog display(TextOnlyPage p, JDialog d, Component relative) {
		d.setTitle(p.title());
		JTextArea textArea = new JTextArea(p.content());
		textArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(textArea);
		d.setContentPane(scroll);
		return PageDisplay.initDialog(d, relative);
	}
	
}
