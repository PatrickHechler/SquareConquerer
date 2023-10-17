package de.hechler.patrick.games.sc.addons;

import java.util.Map;

import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.ui.pages.Page;
import de.hechler.patrick.games.sc.ui.pages.TextOnlyPage;
import de.hechler.patrick.games.sc.world.def.DefWorldType;

public class WorldAddon extends Addon {
	
	/**
	 * the name of the world addon
	 */
	public static final String WORLD_ADDON_NAME = "base.world.SquareConquerer";
	
	WorldAddon() {
		super(WORLD_ADDON_NAME, "Square Conquerer world", new String[0], TheBaseAddon.VERSION, Map.of(DefWorldType.WORLD_TYPE.name, DefWorldType.WORLD_TYPE), TheBaseAddonProvider.BASE_ADDON.licenseName);
	}
	
	/** {@inheritDoc} */
	@Override
	protected TextOnlyPage loadLicense() {
		return TheBaseAddonProvider.BASE_ADDON.license();
	}
	
	/** {@inheritDoc} */
	@Override
	protected Page loadCredits() {
		return TheBaseAddonProvider.BASE_ADDON.credits();
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean hasCredits() {
		return TheBaseAddonProvider.BASE_ADDON.hasCredits();
	}
	
	/** {@inheritDoc} */
	@Override
	protected Page loadHelp() {
		return TheBaseAddonProvider.BASE_ADDON.help();
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean hasHelp() {
		return TheBaseAddonProvider.BASE_ADDON.hasHelp();
	}
	
	/** {@inheritDoc} */
	@Override
	public void checkDependencies(@SuppressWarnings("unused") Map<String, Addon> addons,
			@SuppressWarnings("unused") Map<String, AddableType<?, ?>> added) { /* no dependencies */ }
	
}
