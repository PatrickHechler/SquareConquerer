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
package de.hechler.patrick.games.sc.addons;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.error.MissingDependencyException;
import de.hechler.patrick.games.sc.ui.pages.Page;
import de.hechler.patrick.games.sc.ui.pages.PageBlock.EntryBlock;
import de.hechler.patrick.games.sc.ui.pages.PageEntry.LinkEntry;
import de.hechler.patrick.games.sc.ui.pages.PageEntry.TextEntry;
import de.hechler.patrick.games.sc.ui.pages.TextOnlyPage;
import de.hechler.patrick.utils.objects.Version;

public class TheBaseAddon extends Addon {
	
	public static final Version VERSION = new Version(1, 0, 0);
	
	public static final String BASE_PROVIDER_NAME = "base:SquareConquerer";
	
	TheBaseAddon() {
		super(BASE_PROVIDER_NAME, "Square Conquerer", new String[0], VERSION, Map.of(GroundType.NOT_EXPLORED_TYPE.name, GroundType.NOT_EXPLORED_TYPE), "AGPL v3+");
	}
	
	/** {@inheritDoc} */
	@Override
	protected TextOnlyPage loadLicense() {
		try (InputStream in = getClass().getResourceAsStream("/license/LICENSE")) {
			return new TextOnlyPage("AGPL v3+", new String(in.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	@Override
	public boolean hasCredits() {
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	protected Optional<Page> loadCredits() {
		return Optional.of(new Page("Square Conquerer Credits", //
				new EntryBlock(//
						new TextEntry("Almoust everything: "), //
						new LinkEntry("Patrick Hechler", URI.create("https://github.com/PatrickHechler")) //
				)//
		));
	}
	
	@Override
	public boolean hasHelp() {
		return true; // thats a lie
	}
	
	/** {@inheritDoc} */
	@Override
	protected Optional<Page> loadHelp() {
		return Optional.of(new Page("Square Conquerer Help", //
				new EntryBlock(new TextEntry("there is no help, good luck"))//
		));
	}
	
	/** {@inheritDoc} */
	@Override
	public void checkDependencies(Map<String, Addon> addons, Map<String, AddableType<?, ?>> added) {
		String val = System.getProperty("square-conquerer.base-addon.no-check");
		if ("no-check".equals(val)) {
			return; // this can be dangerous
		}
		if (addons.get(this.name) != this) {
			throw new MissingDependencyException("I am missing the base addon ('" + this.name + "')!");
		}
		for (AddableType<?, ?> a : added.values()) {
			if (a instanceof GroundType && a != GroundType.NOT_EXPLORED_TYPE) {
				return;
			}
		}
		throw new MissingDependencyException("the only ground type is the not-explored ground type!");
	}
	
}
