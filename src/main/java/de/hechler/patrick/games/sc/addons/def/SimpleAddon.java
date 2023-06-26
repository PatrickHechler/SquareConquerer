package de.hechler.patrick.games.sc.addons.def;

import java.util.Map;

import de.hechler.patrick.games.sc.addons.Addon;
import de.hechler.patrick.games.sc.addons.TheBaseAddonProvider;
import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.ui.pages.Page;
import de.hechler.patrick.games.sc.ui.pages.TextOnlyPage;
import de.hechler.patrick.utils.objects.Version;

public class SimpleAddon extends Addon {
	
	public SimpleAddon(String name, String localName, String[] groups, Version version, AddableType<?, ?> add) {
		super(name, localName, groups, version, Map.of(add.name, add), TheBaseAddonProvider.BASE_ADDON.licenseName);
	}
	
	@Override
	protected TextOnlyPage loadLicense() {
		return TheBaseAddonProvider.BASE_ADDON.license();
	}
	
	@Override
	protected Page loadCredits() {
		return TheBaseAddonProvider.BASE_ADDON.credits();
	}
	
	@Override
	public boolean hasCredits() {
		return true;
	}
	
	@Override
	protected Page loadHelp() {
		return TheBaseAddonProvider.BASE_ADDON.help();
	}
	
	@Override
	public boolean hasHelp() {
		return true;
	}
	
	@Override
	public void checkDependencies(Map<String, Addon> addons, Map<String, AddableType<?, ?>> added) {
		//
	}
	
}
