//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.world;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.text.Format;
import java.util.Iterator;
import java.util.function.Predicate;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPage;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageBlock;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageEntry;
import de.hechler.patrick.games.squareconqerer.stuff.Random2;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

/**
 * this class can be used to generate a {@link World} from a {@link SCPage}
 * 
 * @author Patrick Hechler
 */
@SuppressWarnings("preview")
public class PageWorld {
	
	private static final String ERROR_ILLEGAL_LINE2        = Messages.getString("PageWorld.illegal-line2");      //$NON-NLS-1$
	private static final String PIXEL_NOT_FOUND_IN_PAGE    = Messages.getString("PageWorld.pixel-behind-page");  //$NON-NLS-1$
	private static final String ERROR_INVALID_LINE         = Messages.getString("PageWorld.illegal-line1");      //$NON-NLS-1$
	private static final String POSITION_NOT_FOUND         = Messages.getString("PageWorld.pos-not-found");      //$NON-NLS-1$
	private static final Format UNKNOWN_COLOR              = Messages.getFormat("PageWorld.unknown-color");      //$NON-NLS-1$
	private static final String PAGE_WORLD_FINISH          = Messages.getString("PageWorld.log-finish");         //$NON-NLS-1$
	private static final String PAGE_WORLD_FINISH_DRAWING  = Messages.getString("PageWorld.log-finish-draw");    //$NON-NLS-1$
	private static final String PAGE_WORLD_FINISH_COUNTING = Messages.getString("PageWorld.log-finish-cnt");     //$NON-NLS-1$
	private static final String MAX_BOUNDS                 = Messages.getString("PageWorld.log-max-char-bound"); //$NON-NLS-1$
	private static final String OTHER_TILE_IS_NULL         = Messages.getString("PageWorld.other-is-null");      //$NON-NLS-1$
	private static final String TEXT_TILE_IS_NULL          = Messages.getString("PageWorld.text-is-null");       //$NON-NLS-1$
	private static final String WORLD_TILE_IS_NULL         = Messages.getString("PageWorld.world-is-null");      //$NON-NLS-1$
	private static final String LINK_TILE_IS_NULL          = Messages.getString("PageWorld.link-is-null");       //$NON-NLS-1$
	private static final String PAGE_TILE_IS_NULL          = Messages.getString("PageWorld.page-is-null");       //$NON-NLS-1$
	
	private static final int   VAL_PAGE   = 255;
	private static final int   VAL_LINK   = 190;
	private static final int   VAL_WRLD   = 127;
	private static final int   VAL_TEXT   = 63;
	private static final int   VAL_NONE   = 0;
	private static final Color COLOR_PAGE = new Color(VAL_PAGE, VAL_PAGE, VAL_PAGE);
	private static final Color COLOR_LINK = new Color(VAL_LINK, VAL_LINK, VAL_LINK);
	private static final Color COLOR_WRLD = new Color(VAL_WRLD, VAL_WRLD, VAL_WRLD);
	private static final Color COLOR_TEXT = new Color(VAL_TEXT, VAL_TEXT, VAL_TEXT);
	
	private final SCPage page;
	
	private Tile pageTile;
	private Tile linkTile;
	private Tile wrldTile;
	private Tile textTile;
	private Tile othrTile;
	
	/**
	 * create a new {@link PageWorld} builder from the given page
	 * 
	 * @param page the page, which will be displayed on the created world
	 */
	public PageWorld(SCPage page) {
		this.page = page;
		randomTiles(false);
	}
	
	private static Tile randomTile(Random2 rnd, boolean allowNotExplored) {
		OreResourceType ort = OreResourceType.of(rnd.nextInt(OreResourceType.count()));
		GroundType      tt;
		do {
			tt = GroundType.of(rnd.nextInt(GroundType.count()));
		} while (!allowNotExplored && tt == GroundType.NOT_EXPLORED);
		return new Tile(tt, ort, true);
	}
	
	
	/**
	 * @return the pageTile
	 */
	public Tile pageTile() { return this.pageTile; }
	
	/**
	 * @param pageTile the pageTile to set
	 */
	public void pageTile(Tile pageTile) {
		if (pageTile == null) throw new NullPointerException(PAGE_TILE_IS_NULL);
		this.pageTile = pageTile;
	}
	
	/**
	 * @return the linkTile
	 */
	public Tile linkTile() { return this.linkTile; }
	
	/**
	 * @param linkTile the linkTile to set
	 */
	public void linkTile(Tile linkTile) {
		if (linkTile == null) throw new NullPointerException(LINK_TILE_IS_NULL);
		this.linkTile = linkTile;
	}
	
	/**
	 * @return the worldTile
	 */
	public Tile worldTile() { return this.wrldTile; }
	
	/**
	 * @param worldTile the worldTile to set
	 */
	public void worldTile(Tile worldTile) {
		if (worldTile == null) throw new NullPointerException(WORLD_TILE_IS_NULL);
		this.wrldTile = worldTile;
	}
	
	/**
	 * @return the textTile
	 */
	public Tile textTile() { return this.textTile; }
	
	/**
	 * @param textTile the textTile to set
	 */
	public void textTile(Tile textTile) {
		if (textTile == null) throw new NullPointerException(TEXT_TILE_IS_NULL);
		this.textTile = textTile;
	}
	
	/**
	 * @return the otherTile
	 */
	public Tile otherTile() {
		return this.othrTile;
	}
	
	/**
	 * @param otherTile the otherTile to set
	 */
	public void otherTile(Tile otherTile) {
		if (otherTile == null) throw new NullPointerException(OTHER_TILE_IS_NULL);
		this.othrTile = otherTile;
	}
	
	/**
	 * regenerate the tiles used by this builder randomly<br>
	 * 
	 * @param allowNotExplored if {@link GroundType#NOT_EXPLORED} is allowed to be generated randomly
	 */
	public void randomTiles(boolean allowNotExplored) {
		Random2 rnd = new Random2();
		do {
			this.pageTile = randomTile(rnd, allowNotExplored);
			this.linkTile = randomTile(rnd, allowNotExplored);
			this.wrldTile = randomTile(rnd, allowNotExplored);
			this.textTile = randomTile(rnd, allowNotExplored);
			this.othrTile = randomTile(rnd, allowNotExplored);
		} while (sameTiles());
	}
	
	private boolean sameTiles() {
		return !maxOne(GroundType::isForest) || !maxOne(GroundType::isGrass) || !maxOne(GroundType::isMountain) || !maxOne(GroundType::isSand)
			|| !maxOne(GroundType::isSwamp) || !maxOne(GroundType::isWater);
	}
	
	private boolean maxOne(Predicate<GroundType> t) {
		return maxOne(t, this.pageTile.ground, this.linkTile.ground, this.wrldTile.ground, this.textTile.ground, this.othrTile.ground);
	}
	
	private static boolean maxOne(Predicate<GroundType> t, GroundType... types) {
		boolean matched = false;
		for (GroundType tt : types) {
			if (t.test(tt)) {
				if (matched) return false;
				matched = true;
			}
		}
		return true;
	}
	
	/**
	 * create a world from the page using the tiles
	 * 
	 * @return the created world
	 */
	public RootWorld.Builder createWorld() {
		FontRenderContext frc       = new FontRenderContext(null, false, false);
		Font              f         = new Font("Monospace", Font.PLAIN, 9);     //$NON-NLS-1$
		Rectangle2D       maxBounds = f.getMaxCharBounds(frc);
		System.out.println(MAX_BOUNDS + maxBounds);
		int xStartOff = (int) Math.ceil(-maxBounds.getMinX()) + 1;
		int yStartOff = (int) Math.ceil(-maxBounds.getMinY()) + 1;
		int ch        = (int) Math.ceil(maxBounds.getHeight());
		if (xStartOff < 1 || yStartOff < 1 || ch <= 0) throw new AssertionError();
		Dimension dim = countAll(frc, f, xStartOff, yStartOff, ch);
		System.out.println(PAGE_WORLD_FINISH_COUNTING + dim);
		BufferedImage img = new BufferedImage(dim.width + 1, dim.height + 1, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D    g   = img.createGraphics();
		g.setFont(f);
		int yoff = yStartOff;
		for (SCPageBlock block : this.page.blocks()) {
			yoff = draw(g, block, f, frc, ch, xStartOff, yoff, dim.width, yStartOff - 1);
		}
		System.out.println(PAGE_WORLD_FINISH_DRAWING);
		WritableRaster    raster = img.getRaster();
		RootWorld.Builder b      = new RootWorld.Builder(RootUser.nopw(), dim.width, dim.height);
		fill(raster, b, ch, f, frc, xStartOff, yStartOff);
		System.out.println(PAGE_WORLD_FINISH);
		return b;
	}
	
	private Dimension countAll(FontRenderContext frc, Font f, int xStartOff, int yStartOff, int ch) {
		Dimension dim = new Dimension(xStartOff, yStartOff);
		for (SCPageBlock block : this.page.blocks()) {
			count(block, dim, frc, f, ch, xStartOff << 1);
		}
		dim.width  += 3;
		dim.height += 3;
		return dim;
	}
	
	private void fill(WritableRaster raster, RootWorld.Builder b, int ch, Font f, FontRenderContext frc, int xStartOff, int yStartOff) {
		int[] arr = new int[1];
		for (int y = 0; y < b.ylen(); y++) {
			for (int x = 0; x < b.xlen(); x++) {
				int  a = raster.getPixel(x, y, arr)[0];
				Tile t;
				switch (a) {
				case VAL_PAGE -> t = this.pageTile;
				case VAL_LINK -> t = this.linkTile;
				case VAL_WRLD -> t = this.wrldTile;
				case VAL_TEXT -> t = this.textTile;
				case VAL_NONE -> t = this.othrTile;
				default -> {
					System.err.println(Messages.format(UNKNOWN_COLOR, Integer.toString(a)));
					t = this.othrTile;
				}
				}
				switch (a) {
				case VAL_PAGE, VAL_LINK, VAL_WRLD -> fillTile(ch, f, frc, xStartOff, yStartOff, x, y, t);
				default -> t.page(null, null);
				}
				b.set(x, y, t);
			}
		}
	}
	
	private void fillTile(int ch, Font f, FontRenderContext frc, int xStartOff, int yStartOff, int x, int y, Tile t) {
		int yPos = yStartOff - ch;
		for (SCPageBlock block : this.page.blocks()) {
			if (yPos >= y) {
				System.err.println(POSITION_NOT_FOUND);
				t.page(null, null);
				return;
			}
			switch (block) {
			case SCPageBlock.TextBlock tb -> yPos += ch * tb.text().lines().count();
			case SCPageBlock.EntryBlock eb -> {
				yPos += ch;
				if (yPos < y) break;
				int xPos = xStartOff;
				fillFromEntryBlock(frc, f, x, t, xPos, eb);
				return;
			}
			case SCPageBlock.SeparatingBlock sb -> yPos += sbHigh(sb);
			}
		}
		System.err.println(ERROR_INVALID_LINE);
	}
	
	private static void fillFromEntryBlock(FontRenderContext frc, Font f, int x, Tile t, int xPos, SCPageBlock.EntryBlock eb) {
		Iterator<SCPageEntry> iter  = eb.entries().iterator();
		SCPageEntry           entry = iter.next();
		while (switch (entry) {
		case SCPageEntry.LinkEntry le -> xPos += width(frc, f, le.text());
		case SCPageEntry.PageEntry pe -> xPos += width(frc, f, pe.text());
		case SCPageEntry.WorldEntry we -> xPos += width(frc, f, we.text());
		case SCPageEntry.TextEntry te -> xPos += width(frc, f, te.text());
		} < x) {
			if (!iter.hasNext()) {
				System.err.println(PIXEL_NOT_FOUND_IN_PAGE);
				return;
			}
			entry = iter.next();
		}
		switch (entry) {
		case SCPageEntry.LinkEntry le -> t.page(() -> new SCPage(new SCPageBlock.EntryBlock(le)), le.text());
		case SCPageEntry.PageEntry pe -> t.page(pe.page(), pe.title());
		case SCPageEntry.WorldEntry we -> t.page(() -> new SCPage(new SCPageBlock.EntryBlock(we)), we.worldName());
		case SCPageEntry.TextEntry te -> {
			System.err.println(ERROR_ILLEGAL_LINE2);
			t.page(null, null);
		}
		}
	}
	
	private static int sbHigh(SCPageBlock.SeparatingBlock sb) { return sb.bold() ? 5 : 3; }
	
	private static int draw(Graphics2D g, SCPageBlock block, Font f, FontRenderContext frc, int ch, int xStartOff, int yoff, int lineWhidth, int ysub) {
		switch (block) {
		case SCPageBlock.EntryBlock eb -> {
			int xoff = xStartOff;
			for (SCPageEntry entry : eb.entries()) {
				switch (entry) {
				case SCPageEntry.PageEntry pe -> xoff = draw(g, pe.text(), f, frc, xoff, yoff, COLOR_PAGE);
				case SCPageEntry.LinkEntry le -> xoff = draw(g, le.text(), f, frc, xoff, yoff, COLOR_LINK);
				case SCPageEntry.TextEntry te -> xoff = draw(g, te.text(), f, frc, xoff, yoff, COLOR_TEXT);
				case SCPageEntry.WorldEntry we -> xoff = draw(g, we.text(), f, frc, xoff, yoff, COLOR_WRLD);
				}
			}
			yoff += ch;
		}
		case SCPageBlock.SeparatingBlock sb -> {
			g.drawLine(3, yoff + 1 - ysub, lineWhidth - 3, yoff + 1 - ysub);
			if (sb.bold()) {
				g.drawLine(2, yoff + 2 - ysub, lineWhidth - 2, yoff + 2 - ysub);
				g.drawLine(3, yoff + 3 - ysub, lineWhidth - 3, yoff + 3 - ysub);
				yoff += 5;
			} else yoff += 3;
		}
		case SCPageBlock.TextBlock tb -> {
			for (String line : tb.text().replace("\t", "  ").split("\r\n?|\n")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				draw(g, line, f, frc, xStartOff, yoff, COLOR_TEXT);
				yoff += ch;
			}
		}
		}
		return yoff;
	}
	
	@SuppressWarnings("cast")
	private static int draw(Graphics2D g, String text, Font f, FontRenderContext frc, int xoff, int yoff, Color c) {
		text = text.replace("\t", "  "); //$NON-NLS-1$ //$NON-NLS-2$
		g.setColor(c);
		TextLayout layout = new TextLayout(text, f, frc);
		layout.draw(g, (float) xoff, (float) yoff);
		xoff += width(layout);
		return xoff;
	}
	
	private static void count(SCPageBlock block, Dimension dim, FontRenderContext frc, Font f, int ch, int xStartOff) {
		switch (block) {
		case SCPageBlock.EntryBlock eb -> {
			dim.height += ch;
			int xlen = xStartOff;
			for (SCPageEntry entry : eb.entries()) {
				String text = switch (entry) {
				case SCPageEntry.PageEntry pe -> pe.text().replace("\t", "  "); //$NON-NLS-1$ //$NON-NLS-2$
				case SCPageEntry.LinkEntry le -> le.text().replace("\t", "  "); //$NON-NLS-1$ //$NON-NLS-2$
				case SCPageEntry.TextEntry te -> te.text().replace("\t", "  "); //$NON-NLS-1$ //$NON-NLS-2$
				case SCPageEntry.WorldEntry we -> we.text().replace("\t", "  "); //$NON-NLS-1$ //$NON-NLS-2$
				};
				xlen += width(frc, f, text);
			}
			dim.width = Math.max(dim.width, xlen);
		}
		case SCPageBlock.SeparatingBlock sb -> dim.height += sbHigh(sb);
		case SCPageBlock.TextBlock tb -> {
			dim.height += tb.text().lines().count() * ch;
			dim.width   = Math.max(dim.width, xStartOff
				+ tb.text().lines().mapToInt(str -> (int) Math.ceil(new TextLayout(str.replace("\t", "  "), f, frc).getBounds().getWidth())).max().getAsInt()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		}
	}
	
	private static int width(FontRenderContext frc, Font f, String text) {
		TextLayout textLayout = new TextLayout(text, f, frc);
		return width(textLayout);
	}
	
	private static int width(TextLayout textLayout) {
		return (int) Math.ceil(textLayout.getLeading() + textLayout.getAdvance());
	}
	
}
