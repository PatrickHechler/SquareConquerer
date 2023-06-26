package de.hechler.patrick.games.sc.addons.def;

import java.util.List;

import de.hechler.patrick.games.sc.addons.Addon;
import de.hechler.patrick.games.sc.addons.AddonProvider;
import de.hechler.patrick.games.sc.addons.addable.BuildType;
import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.addons.addable.UnitType;
import de.hechler.patrick.utils.objects.Version;

public class FigtherAddonProvider extends AddonProvider {
	
	public static final UnitType   MEELE_TYPE         = new MeeleType();
	public static final UnitType   WORKER_TYPE        = new WorkerType();
	public static final BuildType  CLONE_FACTORY_TYPE = new CloneFactoryType();
	public static final GroundType GRASS_TYPE         = new GrassType();
	
	private static final List<Addon> ADDONS = List.of(//
			new SimpleAddon(MEELE_TYPE.name, MEELE_TYPE.localName, new String[] { "fighter", "unit" }, new Version(1, 0, 0), MEELE_TYPE), //
			new SimpleAddon(WORKER_TYPE.name, WORKER_TYPE.localName, new String[] { "fighter", "unit" }, new Version(1, 0, 0), WORKER_TYPE), //
			new SimpleAddon(CLONE_FACTORY_TYPE.name, CLONE_FACTORY_TYPE.localName, new String[] { "fighter", "build" }, new Version(1, 0, 0), CLONE_FACTORY_TYPE), //
			new SimpleAddon(GRASS_TYPE.name, GRASS_TYPE.localName, new String[] { "fighter", "ground" }, new Version(1, 0, 0), GRASS_TYPE) //
	);
	
	@Override
	public Iterable<Addon> addons() {
		return ADDONS;
	}
	
}
