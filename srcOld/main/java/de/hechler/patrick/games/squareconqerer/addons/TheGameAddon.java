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
package de.hechler.patrick.games.squareconqerer.addons;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.addons.defaults.AddonDefaults;
import de.hechler.patrick.games.squareconqerer.addons.entities.AddonEntities;
import de.hechler.patrick.games.squareconqerer.addons.entities.EntityTrait;
import de.hechler.patrick.games.squareconqerer.addons.entities.EntityTraitWithVal;
import de.hechler.patrick.games.squareconqerer.addons.entities.TheGameEntities;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCLicense;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPage;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageBlock;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageEntry;
import de.hechler.patrick.games.squareconqerer.world.PageWorld;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.Storage;

public final class TheGameAddon extends SCAddon implements AddonDefaults {
	
	public static final int THE_GAME    = 0xE5664F22;
	public static final int OTHER_ADDON = 0x2DC4071A;
	
	private final TheGameEntities entities = new TheGameEntities();
	
	public TheGameAddon() {
		super(SCAddon.GAME_ADDON_NAME);
	}
	
	private SCLicense myLicense;
	
	@Override
	public SCLicense license() {
		if (this.myLicense == null) {
			try (InputStream in = getClass().getResourceAsStream("/LICENSE")) {
				byte[] bytes = in.readAllBytes();
				String text  = new String(bytes, StandardCharsets.UTF_8);
				this.myLicense = new SCLicense("AGPL v3+", text);
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return this.myLicense;
	}
	
	private SCPage myHelp;
	
	@Override
	public SCPage help() {
		if (this.myHelp == null) {
			this.myHelp = generateMyHelpPage(0);
		}
		return this.myHelp;
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
						new SCPageEntry.LinkEntry("bug Issues", "https://github.com/PatrickHechler/SquareConquerer/issues?q=is%3Aissue+is%3Aopen+label%3Abug")//
				), //
				new SCPageBlock.SeparatingBlock(true), //
				new SCPageBlock.EntryBlock(//
						new SCPageEntry.TextEntry("General "), //
						selfReference(deep), //
						new SCPageEntry.TextEntry(": "),
						new SCPageEntry.LinkEntry("readme", "https://github.com/PatrickHechler/SquareConquerer/blob/master/readme.md")//
				), //
				new SCPageBlock.SeparatingBlock(false), //
				new SCPageBlock.TextBlock(//
						/*		*/"Tutorials:"//
				), //
				new SCPageBlock.EntryBlock(//
						new SCPageEntry.WorldEntry("\tSimple Tutorial", "Simple Tutorial", () -> new PageWorld(this.myCredits).createWorld())//
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
		if (this.myCredits == null) {
			this.myCredits = new SCPage(//
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
		return this.myCredits;
	}
	
	@Override
	public AddonEntities entities() {
		return this.entities;
	}
	
	@Override
	public AddonDefaults defaults() {
		return this;
	}
	
	@Override
	public Map<String, Collection<Map<String, EntityTraitWithVal>>> startEntities() {
		Map<String, Collection<Map<String, EntityTraitWithVal>>> result = new HashMap<>();
		Map<String, EntityTrait>                                 orig   = this.entities.traits(Storage.NAME);
		Map<String, EntityTraitWithVal>                          wv     = useDefaults(orig);
		result.put(Storage.NAME, List.of(wv));
		orig = this.entities.traits(Carrier.NAME);
		wv   = useDefaults(orig);
		result.put(Carrier.NAME, List.of(wv));
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, EntityTraitWithVal> useDefaults(Map<String, EntityTrait> store) {
		Map<String, EntityTraitWithVal> wv = new HashMap<>();
		((Map<String, EntityTrait>) (Map<String, ?>) wv).putAll(store);
		((Map<String, Object>) (Map<String, ?>) wv).replaceAll((n, et) -> ((EntityTrait) et).defaultValue());
		return wv;
	}
	
}
