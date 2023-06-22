package de.hechler.patrick.games.sc.ui.display;

import java.awt.Frame;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.hechler.patrick.games.sc.ui.pages.TextOnlyPage;

public class TextPageDisplay {
	
	public static void display(TextOnlyPage p) {
		JDialog d = new JDialog();
		d.setLocationByPlatform(true);
		display(p, d);
	}
	
	public static JDialog display(TextOnlyPage p, Frame f) {
		JDialog d = new JDialog(f);
		d.setLocationRelativeTo(f);
		return display(p, d);
	}
	
	public static JDialog display(TextOnlyPage p, Window w) {
		JDialog d = new JDialog(w);
		d.setLocationRelativeTo(w);
		return display(p, d);
	}
	
	public static JDialog display(TextOnlyPage p, JDialog d) {
		d.setTitle(p.title());
		JTextArea textArea = new JTextArea(p.content());
		textArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(textArea);
		d.setContentPane(scroll);
		return PageDisplay.initDialog(d);
	}
	
}
