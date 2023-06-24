// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.sc.ui.display;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import de.hechler.patrick.games.sc.ui.pages.Page;
import de.hechler.patrick.games.sc.ui.pages.PageBlock;
import de.hechler.patrick.games.sc.ui.pages.PageBlock.SeparatingBlock;
import de.hechler.patrick.games.sc.ui.pages.PageEntry;
import de.hechler.patrick.games.sc.world.World;

public class PageDisplay {
	
	private final BiConsumer<JDialog, World> setWorld;
	private final Supplier<String>           discardMsg;
	
	public PageDisplay(BiConsumer<JDialog, World> setWorld, Supplier<String> discardMsg) {
		this.setWorld   = Objects.requireNonNull(setWorld, "set world");
		this.discardMsg = Objects.requireNonNull(discardMsg, "discard message");
	}
	
	public JDialog display(Page p) {
		JDialog d = new JDialog();
		d.setLocationByPlatform(true);
		return display(p, d);
	}
	
	public JDialog display(Page p, Frame f) {
		JDialog d = new JDialog(f);
		d.setLocationRelativeTo(f);
		return display(p, d);
	}
	
	public JDialog display(Page p, Window w) {
		JDialog d = new JDialog(w);
		d.setLocationRelativeTo(w);
		return display(p, d);
	}
	
	public JDialog display(Page p, JDialog d) {
		d.setTitle(p.title());
		JPanel dp = new JPanel();
		dp.setLayout(null);
		d.setContentPane(new JScrollPane(dp));
		int yoff = 0;
		int maxx = 0;
		for (PageBlock block : p.blocks()) {
			switch (block) {
			case @SuppressWarnings("preview") PageBlock.SeparatingBlock sb -> yoff += pageAddSepBlock(dp, yoff, sb);
			case @SuppressWarnings("preview") PageBlock.EntryBlock eb -> {
				int xoff = 0;
				int yadd = 0;
				for (PageEntry entry : eb.entries()) {
					switch (entry) {
					case @SuppressWarnings("preview") PageEntry.TextEntry te -> {
						Dimension pref = pageAddTextEntry(dp, yoff, xoff, te);
						xoff += pref.width;
						yadd  = Math.max(yadd, pref.height);
					}
					case @SuppressWarnings("preview") PageEntry.LinkEntry le -> {
						Dimension pref = pageAddLinkEntry(d, dp, yoff, xoff, le);
						xoff += pref.width;
						yadd  = Math.max(yadd, pref.height);
					}
					case @SuppressWarnings("preview") PageEntry.PageLinkEntry pe -> {
						Dimension pref = pageAddPageEntry(d, dp, yoff, xoff, pe);
						xoff += pref.width;
						yadd  = Math.max(yadd, pref.height);
					}
					case @SuppressWarnings("preview") PageEntry.WorldEntry we -> {
						Dimension pref = pageAddWorldEntry(d, dp, yoff, xoff, we);
						xoff += pref.width;
						yadd  = Math.max(yadd, pref.height);
					}
					}
				}
				yoff += yadd;
				maxx  = Math.max(maxx, xoff);
			}
			}
		}
		dp.setPreferredSize(new Dimension(maxx, yoff));
		return initDialog(d);
	}
	
	private static int pageAddSepBlock(JPanel dp, int yoff, SeparatingBlock sb) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.BLACK);
		panel.setForeground(Color.BLACK);
		panel.setLocation(0, yoff);
		panel.setSize(1, sb.bold() ? 4 : 3);
		panel.setOpaque(true);
		dp.add(panel);
		dp.addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(@SuppressWarnings("unused") ComponentEvent e) {
				panel.setSize(dp.getWidth(), sb.bold() ? 4 : 3);
			}
			
		});
		return sb.bold() ? 4 : 3;
	}
	
	private Dimension pageAddWorldEntry(JDialog dialog, JPanel dp, int yoff, int xoff, PageEntry.WorldEntry we) {
		JTextArea text = new JTextArea(we.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		text.setForeground(new Color(191, 150, 0));
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		text.setToolTipText("World: " + we.worldName());
		text.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(@SuppressWarnings("unused") MouseEvent e) {
				int chosen = JOptionPane.showConfirmDialog(dialog, "load world " + we.worldName() + '?' + PageDisplay.this.discardMsg.get(),
						"load " + we.worldName() + '?', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) return;
				World w = we.world().get();
				if (w == null) {
					JOptionPane.showMessageDialog(dialog, "error: world is null", "there is no world", JOptionPane.ERROR_MESSAGE);
					return;
				}
				PageDisplay.this.setWorld.accept(dialog, w);
			}
			
		});
		Dimension pref = text.getPreferredSize();
		text.setBounds(xoff, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	private Dimension pageAddPageEntry(JDialog dialog, JPanel dp, int yoff, int xoff, PageEntry.PageLinkEntry pe) {
		JTextArea text = new JTextArea(pe.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		text.setForeground(new Color(225, 64, 64));
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		text.setToolTipText("Page: " + pe.title());
		text.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(@SuppressWarnings("unused") MouseEvent e) {
				display(pe.page().get(), new JDialog(dialog));
			}
			
		});
		Dimension pref = text.getPreferredSize();
		text.setBounds(xoff, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	private static Dimension pageAddLinkEntry(JDialog dialog, JPanel dp, int yoff, int xoff, PageEntry.LinkEntry le) {
		JTextArea text = new JTextArea(le.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		text.setForeground(Color.BLUE);
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		text.setToolTipText("Link: " + le.link());
		text.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(@SuppressWarnings("unused") MouseEvent e) {
				try {
					URI uri    = le.link();
					int chosen = JOptionPane.showConfirmDialog(dialog, "open link: " + uri, "open link", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (chosen != JOptionPane.YES_OPTION) return;
					Desktop.getDesktop().browse(uri);
				} catch (IOException | RuntimeException e1) {
					JOptionPane.showMessageDialog(dialog, "error while opening link '" + le.link() + "': " + e1.toString(), "could not open the link",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		Dimension pref = text.getPreferredSize();
		text.setBounds(xoff, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	private static Dimension pageAddTextEntry(JPanel dp, int yoff, int xoff, PageEntry.TextEntry te) {
		JTextArea text = new JTextArea(te.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		Dimension pref = text.getPreferredSize();
		text.setBounds(xoff, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	public static JDialog initDialog(JDialog d) {
		d.setModalityType(ModalityType.APPLICATION_MODAL);
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		d.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
		d.getRootPane().getActionMap().put("escape", new AbstractAction() {
			
			private static final long serialVersionUID = 2109799710377947913L;
			
			@Override
			public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
				d.dispose();
			}
			
		});
		Container cont = d.getContentPane();
		if (cont instanceof JScrollPane scroll) {
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		d.pack();
		Rectangle bounds = d.getGraphicsConfiguration().getBounds();
		int       w      = d.getWidth();
		int       h      = d.getHeight();
		boolean   set    = false;
		if (bounds.width < d.getWidth()) {
			w   = bounds.width;
			set = true;
			if (cont instanceof JScrollPane scroll) {
				h += scroll.getHorizontalScrollBar().getHeight();
			}
		}
		if (bounds.height < d.getHeight()) {
			h   = bounds.height;
			set = true;
			if (cont instanceof JScrollPane scroll) {
				w   = Math.min(w + scroll.getVerticalScrollBar().getWidth(), bounds.width);
			}
		}
		if (set) {
			d.setSize(w, h);
		}
		if (cont instanceof JScrollPane scroll) {
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
		d.setVisible(true);
		return d;
	}
	
}
