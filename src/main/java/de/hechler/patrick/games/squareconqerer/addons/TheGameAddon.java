package de.hechler.patrick.games.squareconqerer.addons;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import de.hechler.patrick.games.squareconqerer.addons.entities.AddonEntities;
import de.hechler.patrick.games.squareconqerer.addons.entities.TheGameEntities;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCLicense;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPage;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageBlock;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageEntry;
import de.hechler.patrick.games.squareconqerer.world.PageWorld;
import de.hechler.patrick.games.squareconqerer.world.entity.BuildingImpl;
import de.hechler.patrick.games.squareconqerer.world.entity.UnitImpl;

public final class TheGameAddon extends SquareConquererAddon {
	
	public static final int THE_GAME    = 0xE5664F22;
	public static final int OTHER_ADDON = 0x2DC4071A;
	
	private final TheGameEntities entities = new TheGameEntities();
	
	public TheGameAddon() {
		super(SquareConquererAddon.GAME_ADDON_NAME, UnitImpl.MY_COUNT_NO_NULL + BuildingImpl.MY_COUNT_NO_NULL);
	}
	
	private SCLicense myLicense;
	
	@Override
	public SCLicense license() {
		if (myLicense == null) {
			try (InputStream in = getClass().getResourceAsStream("/LICENSE")) {
				byte[] bytes = in.readAllBytes();
				String text  = new String(bytes, StandardCharsets.UTF_8);
				myLicense = new SCLicense("AGPL v3+", text);
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return myLicense;
	}
	
	private SCPage myHelp;
	
	@Override
	public SCPage help() {
		if (myHelp == null) {
			myHelp = generateMyHelpPage(0);
		}
		return myHelp;
	}
	
	private SCPage generateMyHelpPage(int deep) {
		return new SCPage(// of course you can also change this help in your fork
				new SCPageBlock.TextBlock(//
						/*		*/"Square Conquerer:\n"//
								+ "\tThis is the help page for the (base) game.\n"//
								+ "\tThis Project is licensed under the"//
				), //
				new SCPageBlock.EntryBlock(//
						new SCPageEntry.PageEntry("\tGNU AFFERO GENERAL PUBLIC LICENSE.", "GNU AFFERO GENERAL PUBLIC LICENSE",
								() -> new SCPage(new SCPageBlock.TextBlock(license().text())))//
				), new SCPageBlock.EntryBlock(//
						new SCPageEntry.TextEntry("\tSource Code/GitHub repo: "),
						new SCPageEntry.LinkEntry("PatrickHechler/SquareConquerer", "https://github.com/PatrickHechler/SquareConquerer")//
				), //
				new SCPageBlock.TextBlock(//
						/*		*/"\t\tIf you want, you can create your own fork\n"//
								+ "\t\tand contribute to the game or submit an issue\n"//
								+ "\t\tto the git repo with your ideas (or with the\n"//
								+ "\t\tbugs you found)."//
				), //
				new SCPageBlock.EntryBlock(//
						new SCPageEntry.TextEntry("\tYou found a bug, please report it: "), //
						new SCPageEntry.LinkEntry("bug Issues",
								"https://github.com/PatrickHechler/SquareConquerer/issues?q=is%3Aissue+is%3Aopen+label%3Abug")//
				), //
				new SCPageBlock.SeperatingBlock(true), //
				new SCPageBlock.EntryBlock(//
						new SCPageEntry.TextEntry("General "), //
						selfReference(deep), //
						new SCPageEntry.TextEntry(": "),
						new SCPageEntry.LinkEntry("readme", "https://github.com/PatrickHechler/SquareConquerer/blob/master/readme.md")//
				), //
				new SCPageBlock.SeperatingBlock(false), //
				new SCPageBlock.TextBlock(//
						/*		*/"Tutorials:"//
				), //
				new SCPageBlock.EntryBlock(//
						new SCPageEntry.WorldEntry("\tSimple Tutorial", "Simple Tutorial", () -> new PageWorld(myCredits).createWorld())//
				)//
		);
	}
	
	private SCPageEntry selfReference(int deep) {
		if (deep == 242) { // I got a stackoverflow at 243
			return new SCPageEntry.WorldEntry("Help", "Help (maybe this help is more helpful)", () -> {
				PageWorld pw = new PageWorld(generateMyHelpPage(deep + 1));
				return pw.createWorld();
			});
		}
		int    helpful   = 100 + 42 + deep - (deep % 100);
		String helpTitle = deep > 22 ? "Help (maybe the " + helpful + "th Help is more helpful (this is the " + (deep + 1) + "th Help))" : "Help";
		return new SCPageEntry.PageEntry("Help", helpTitle, () -> generateMyHelpPage(deep + 1));
	}
	
	private SCPage myCredits;
	
	// If you make your own fork, you should make your
	// own add-on with your own credits
	@Override
	public SCPage credits() {
		if (myCredits == null) {
			myCredits = new SCPage(//
					new SCPageBlock.TextBlock(//
							/*		*/"Square Conquerer:\n"//
									+ "\tHere are the credits for the\n"//
									+ "\tSquare Conquerer base game"//
					), //
					new SCPageBlock.EntryBlock(//
							new SCPageEntry.TextEntry("\t(Almost) Everything: "), //
							new SCPageEntry.LinkEntry("Patrick Hechler", "https://github.com/PatrickHechler")//
					) //
			);
		}
		return myCredits;
	}
	
	@Override
	public AddonEntities entities() {
		return entities;
	}
	
	@Override
	protected void initOridinalOffset(int offset) {
		throw new UnsupportedOperationException("the game addon does not support an offset diffferent than zero");
	}
	
	@Override
	public int oridinalOffset() {
		return 0;
	}
	
}
