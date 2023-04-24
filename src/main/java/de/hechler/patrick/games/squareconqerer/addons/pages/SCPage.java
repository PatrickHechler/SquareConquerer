package de.hechler.patrick.games.squareconqerer.addons.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record SCPage(List<SCPageBlock> blocks) {
	
	public SCPage(List<SCPageBlock> blocks) {
		ArrayList<SCPageBlock> list = new ArrayList<>(blocks);
		for (SCPageBlock b : list) {
			if (b == null) throw new NullPointerException("null blocks are not supporetd!");
		}
		this.blocks = Collections.unmodifiableList(list);
	}

	public SCPage(SCPageBlock... blocks) {
		this(Arrays.asList(blocks));
	}

	public SCPage(SCPageBlock block) {
		this(List.of(block));
	}

}
