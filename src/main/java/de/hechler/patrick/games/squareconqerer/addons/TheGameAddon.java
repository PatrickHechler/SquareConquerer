package de.hechler.patrick.games.squareconqerer.addons;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import de.hechler.patrick.games.squareconqerer.addons.records.SCHelp;
import de.hechler.patrick.games.squareconqerer.addons.records.SCHelpBlock;
import de.hechler.patrick.games.squareconqerer.addons.records.SCHelpEntry;
import de.hechler.patrick.games.squareconqerer.addons.records.SCLicense;

public final class TheGameAddon extends SquareConquererAddon {
	
	public TheGameAddon() {
		super(SquareConquererAddon.GAME_ADDON_NAME);
	}
	
	private Optional<SCLicense> myLicense;
	
	@Override
	public Optional<SCLicense> license() {
		if (myLicense == null) {
			try (InputStream in = getClass().getResourceAsStream("/LICENSE")) {
				byte[] bytes = in.readAllBytes();
				String text  = new String(bytes, StandardCharsets.UTF_8);
				myLicense = Optional.of(new SCLicense("AGPL v3+", text));
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return myLicense;
	}
	
	private SCHelp myHelp;
	
	@Override
	public SCHelp help() {
		if (myHelp == null) {
			myHelp = new SCHelp(//
					new SCHelpBlock.TextBlock("Square Conquerer:\n"//
							+ "  This is an Open Source Game.\n"//
							+ "    If you want, you can create your own fork\n"//
							+ "    and contribute to the game or submit an issue\n"//
							+ "    with your ideas."//
					), //
					new SCHelpBlock.EntryBlock(//
							new SCHelpEntry.TextEntry("Source Code/GitHub repo: "),
							new SCHelpEntry.LinkEntry("PatrickHechler/SquareConquerer", "https://github.com/PatrickHechler/SquareConquerer")//
					), //
					new SCHelpBlock.EntryBlock(//
							new SCHelpEntry.TextEntry("General Help: "),
							new SCHelpEntry.LinkEntry("readme", "https://github.com/PatrickHechler/SquareConquerer/blob/master/readme.md")//
					), //
					new SCHelpBlock.EntryBlock(//
							new SCHelpEntry.TextEntry("Tutorial: "),//
							new SCHelpEntry.WorldEntry("Simple Tutorial", () -> null)//
					)//
			);
		}
		return myHelp;
	}
	
}
