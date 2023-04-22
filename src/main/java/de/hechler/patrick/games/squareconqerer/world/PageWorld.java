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
import java.util.Iterator;
import java.util.function.Supplier;

import de.hechler.patrick.games.squareconqerer.Random2;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.addons.records.SCPage;
import de.hechler.patrick.games.squareconqerer.addons.records.SCPageBlock;
import de.hechler.patrick.games.squareconqerer.addons.records.SCPageEntry;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.TileType;

@SuppressWarnings("preview")
public class PageWorld {
	
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
	
	public PageWorld(SCPage page) {
		this.page = page;
		Random2 rnd = new Random2();
		this.pageTile = randomTile(rnd, false);
		this.linkTile = randomTile(rnd, false);
		this.wrldTile = randomTile(rnd, false);
		this.textTile = randomTile(rnd, false);
		this.othrTile = randomTile(rnd, false);
	}
	
	private static Tile randomTile(Random2 rnd, boolean allowNotExplored) {
		OreResourceType ort = OreResourceType.of(rnd.nextInt(OreResourceType.count()));
		TileType        tt;
		do {
			tt = TileType.of(rnd.nextInt(TileType.count()));
		} while (!allowNotExplored && tt == TileType.NOT_EXPLORED);
		return new Tile(tt, ort, true);
	}
	
	public void randomTiles(boolean allowNotExplored) {
		Random2 rnd = new Random2();
		this.pageTile = randomTile(rnd, allowNotExplored);
		this.linkTile = randomTile(rnd, allowNotExplored);
		this.wrldTile = randomTile(rnd, allowNotExplored);
		this.textTile = randomTile(rnd, allowNotExplored);
		this.othrTile = randomTile(rnd, allowNotExplored);
	}
	
	public RootWorld.Builder createWorld() {
		FontRenderContext frc       = new FontRenderContext(null, false, false);
		Font              f         = new Font("Monospace", Font.PLAIN, 9);
		Rectangle2D       maxBounds = f.getMaxCharBounds(frc);
		System.out.println("maxBounds=" + maxBounds);
		System.out.println("minX=" + (int) Math.ceil(-maxBounds.getMinX()) + " minY=" + (int) Math.ceil(-maxBounds.getMinY()) + " width="
				+ (int) Math.ceil(maxBounds.getWidth()) + " height=" + (int) Math.ceil(maxBounds.getHeight()));
		int xStartOff = (int) Math.ceil(-maxBounds.getMinX()) + 1;
		int yStartOff = (int) Math.ceil(-maxBounds.getMinY()) + 1;
		int ch        = (int) Math.ceil(maxBounds.getHeight());
		if (xStartOff < 1 || yStartOff < 1 || ch <= 0) throw new AssertionError();
		Dimension dim = countAll(frc, f, xStartOff, yStartOff, ch);
		System.out.println("pageWorld: finish counting: " + dim);
		BufferedImage img = new BufferedImage(dim.width + 1, dim.height + 1, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D    g   = img.createGraphics();
		frc = g.getFontRenderContext();
		Dimension dim2 = countAll(frc, f, xStartOff, yStartOff, ch);
		System.out.println("pageWorld: finish counting (again): " + dim);
		if (dim2.width > dim.width || dim2.height > dim.height) {
			img = new BufferedImage(dim.width + 1, dim.height + 1, BufferedImage.TYPE_BYTE_GRAY);
			g   = img.createGraphics();
			// hopefully that there will be the same Font Render Context
			frc = g.getFontRenderContext();
		}
		dim = dim2;
		g.setFont(f);
		int yoff = yStartOff;
		for (SCPageBlock block : page.blocks()) {
			yoff = draw(g, block, f, frc, ch, xStartOff, yoff, dim.width, yStartOff - 1);
		}
		System.out.println("pageWorld: finish drawing");
		WritableRaster    raster = img.getRaster();
		RootWorld.Builder b      = new RootWorld.Builder(RootUser.create(new char[0]), dim.width, dim.height);
		fill(raster, b, ch, f, frc, xStartOff, yStartOff);
		System.out.println("pageWorld: finish");
		return b;
	}
	
	private Dimension countAll(FontRenderContext frc, Font f, int xStartOff, int yStartOff, int ch) {
		Dimension dim = new Dimension(xStartOff, yStartOff);
		for (SCPageBlock block : page.blocks()) {
			count(block, dim, frc, f, ch, xStartOff << 1);
		}
		dim.width  += 3;
		dim.height += 3;
		return dim;
	}
	
	private void fill(WritableRaster raster, RootWorld.Builder b, int ch, Font f, FontRenderContext frc, int xStartOff, int yStartOff) {
		int[]  arr = new int[1];
		char[] cs  = new char[b.xlen() + 1];
		cs[cs.length - 1] = '\n';
		for (int y = 0; y < b.ylen(); y++) {
			for (int x = 0; x < b.xlen(); x++) {
				int a = raster.getPixel(x, y, arr)[0];
				cs[x] = (switch (a) {
				case VAL_PAGE -> 'P';
				case VAL_LINK -> 'L';
				case VAL_WRLD -> 'W';
				case VAL_TEXT -> 'T';
				case VAL_NONE -> ' ';
				default -> '#';
				});
				Tile t;
				switch (a) {
				case VAL_PAGE -> t = pageTile;
				case VAL_LINK -> t = linkTile;
				case VAL_WRLD -> t = wrldTile;
				case VAL_TEXT -> t = textTile;
				case VAL_NONE -> t = othrTile;
				default -> {
					System.out.println("a=" + a);
					t = othrTile;
				}
				}
				switch (a) {
				case VAL_PAGE, VAL_LINK, VAL_WRLD -> {
					setPage(ch, f, frc, xStartOff, yStartOff, x, y, t);
				}
				default -> t.page(null, null);
				}
				b.set(x, y, t);
			}
			System.out.print(cs);
		}
	}
	
	private void setPage(int ch, Font f, FontRenderContext frc, int xStartOff, int yStartOff, int x, int y, Tile t) {
		int yPos = yStartOff;
		for (SCPageBlock block : page.blocks()) {
			if (yPos >= y) {
				System.err.println("error: did not found position");
				t.page(null, null);
				break;
			}
			switch (block) {
			case SCPageBlock.TextBlock tb -> yPos += tb.text().lines().count();
			case SCPageBlock.EntryBlock eb -> {
				yPos += ch;
				if (yPos < y) break;
				int                   xPos = xStartOff;
				Iterator<SCPageEntry> iter = eb.entries().iterator();
				SCPageEntry           e    = iter.next();
				while (xPos < x) {
					if (!iter.hasNext()) {
						System.err.println("error: not enugh elements in the iterator");
						break;
					}
					switch (e) {
					case SCPageEntry.LinkEntry le -> xPos += width(frc, f, le.text());
					case SCPageEntry.PageEntry pe -> xPos += width(frc, f, pe.text());
					case SCPageEntry.WorldEntry we -> xPos += width(frc, f, we.text());
					case SCPageEntry.TextEntry te -> xPos += width(frc, f, te.text());
					}
					e = iter.next();
				}
				switch (e) {
				case SCPageEntry.LinkEntry le -> {
					Supplier<SCPage> sup = () -> new SCPage(new SCPageBlock.EntryBlock(le));
					t.page(sup, le.text());
				}
				case SCPageEntry.PageEntry pe -> pe.page();
				case SCPageEntry.WorldEntry we -> {
					Supplier<SCPage> sup = () -> new SCPage(new SCPageBlock.EntryBlock(we));
					t.page(sup, we.worldName());
				}
				case SCPageEntry.TextEntry te -> {
					System.err.println("error: text entry");
					t.page(null, null);
				}
				}
			}
			case SCPageBlock.SeperatingBlock sb -> yPos += sbHigh(sb);
			}
		}
	}
	
	private static int sbHigh(SCPageBlock.SeperatingBlock sb) { return sb.bold() ? 5 : 3; }
	
	private static int draw(Graphics2D g, SCPageBlock block, Font f, FontRenderContext frc, int ch, int xStartOff, int yoff, int lineWhidth,
			int ysub) {
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
		case SCPageBlock.SeperatingBlock sb -> {
			g.drawLine(3, yoff + 1 - ysub, lineWhidth - 3, yoff + 1 - ysub);
			if (sb.bold()) {
				g.drawLine(2, yoff + 2 - ysub, lineWhidth - 2, yoff + 2 - ysub);
				g.drawLine(3, yoff + 3 - ysub, lineWhidth - 3, yoff + 3 - ysub);
				yoff += 5;
			} else yoff += 3;
		}
		case SCPageBlock.TextBlock tb -> {
			for (String line : tb.text().replace("\t", "  ").split("\r\n?|\n")) {
				g.setColor(COLOR_TEXT);
				g.drawString(line, xStartOff, yoff);
				yoff += ch;
			}
		}
		}
		return yoff;
	}
	
	private static int draw(Graphics2D g, String text, Font f, FontRenderContext frc, int xoff, int yoff, Color c) {
		text = text.replace("\t", "  ");
		g.setColor(c);
		g.drawString(text, xoff, yoff);
		xoff += width(frc, f, text);
		return xoff;
	}
	
	// makes an worst case estimation of the text size
	private static void count(SCPageBlock block, Dimension dim, FontRenderContext frc, Font f, int ch, int xStartOff) {
		switch (block) {
		case SCPageBlock.EntryBlock eb -> {
			dim.height += ch;
			int xlen = xStartOff;
			for (SCPageEntry entry : eb.entries()) {
				String text = switch (entry) {
				case SCPageEntry.PageEntry pe -> pe.text().replace("\t", "  ");
				case SCPageEntry.LinkEntry le -> le.text().replace("\t", "  ");
				case SCPageEntry.TextEntry te -> te.text().replace("\t", "  ");
				case SCPageEntry.WorldEntry we -> we.text().replace("\t", "  ");
				};
				xlen += width(frc, f, text);
			}
			dim.width = Math.max(dim.width, xlen);
		}
		case SCPageBlock.SeperatingBlock sb -> dim.height += sbHigh(sb);
		case SCPageBlock.TextBlock tb -> {
			dim.height += tb.text().lines().count() * ch;
			dim.width   = Math.max(dim.width, xStartOff + tb.text().lines()
					.mapToInt(str -> (int) Math.ceil(new TextLayout(str.replace("\t", "  "), f, frc).getBounds().getWidth())).max().getAsInt());
		}
		}
	}
	
	private static int width(FontRenderContext frc, Font f, String text) {
		// FIXME find better solution than to multiply with 1.1
		return (int) Math.ceil(new TextLayout(text, f, frc).getBounds().getWidth() * 1.1D);
	}
	
}
