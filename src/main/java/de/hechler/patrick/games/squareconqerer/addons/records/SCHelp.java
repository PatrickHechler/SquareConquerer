package de.hechler.patrick.games.squareconqerer.addons.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record SCHelp(List<SCHelpBlock> blocks) {
	
	public SCHelp(List<SCHelpBlock> blocks) {
		ArrayList<SCHelpBlock> list = new ArrayList<>(blocks);
		for (SCHelpBlock b : list) {
			if (b == null) throw new NullPointerException("null blocks are not supporetd!");
		}
		this.blocks = Collections.unmodifiableList(list);
	}

	public SCHelp(SCHelpBlock... blocks) {
		this(Arrays.asList(blocks));
	}

	public SCHelp(SCHelpBlock block) {
		this(List.of(block));
	}

}
