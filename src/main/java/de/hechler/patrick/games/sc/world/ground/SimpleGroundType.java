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
package de.hechler.patrick.games.sc.world.ground;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.spec.ValueSpec;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.utils.objects.Random2;

@SuppressWarnings("javadoc")
public abstract class SimpleGroundType extends GroundType {
	
	private Image         img;
	private BufferedImage imgR;
	
	private final Map<String, Value> defs;
	
	public SimpleGroundType(String name, String localName, Map<String, ValueSpec> specs, Map<String, Value> defs) {
		super(name, localName, specs);
		this.defs = Map.copyOf(defs);
	}
	
	public Ground newInstance(UUID uuid) {
		return new SimpleGround(uuid, this.defs);
	}
	
	@Override
	public Ground withValues(Map<String, Value> vals, UUID uuid) throws TurnExecutionException {
		if (vals.size() != this.defs.size()) {
			throw new IllegalArgumentException("not the correct amount of values!");
		}
		for (Value v : vals.values()) {
			spec(v.name()).validate(v);
		}
		return new SimpleGround(uuid, vals);
	}
	
	@Override
	@SuppressWarnings("unused")
	public Ground withRandomValues(World w, Random2 r, int x, int y) {
		return new SimpleGround(r.nextUUID(), this.defs);
	}
	
	@Override
	@SuppressWarnings("unused")
	public Ground withDefaultValues(World w, Random2 r, int x, int y) {
		return new SimpleGround(r.nextUUID(), this.defs);
	}
	
	protected abstract Image loadImage();
	
	public class SimpleGround extends Ground {
		
		private final Map<String, Value> vals;
		
		public SimpleGround(UUID uuid, Map<String, Value> vals) {
			super(uuid);
			this.vals = Map.copyOf(vals);
		}
		
		@Override
		public Image image(int width, int heigh) {
			Image i = SimpleGroundType.this.img;
			if (i == null) {
				synchronized (SimpleGroundType.this) {
					i = SimpleGroundType.this.img;
					if (i == null) {
						i                         = SimpleGroundType.this.loadImage();
						SimpleGroundType.this.img = i;
					}
				}
			}
			if (i.getWidth(null) == width) {
				return i;
			}
			// store one rescaled globale version
			BufferedImage r = SimpleGroundType.this.imgR;
			if (r != null && r.getWidth(null) == width) {
				return r;
			}
			synchronized (SimpleGroundType.this) {
				r = SimpleGroundType.this.imgR;
				if (r != null && r.getWidth(null) == width) {
					return r;
				}
				r = new BufferedImage(width, heigh, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = r.createGraphics();
				g.drawImage(i, 0, 0, width, heigh, null);
				g.dispose();
				SimpleGroundType.this.imgR = r;
				return r;
			}
		}
		
		@Override
		public GroundType type() {
			return SimpleGroundType.this;
		}
		
		@Override
		public Map<String, Value> values() {
			return this.vals;
		}
		
		@Override
		public Value value(String name) {
			Value val = this.vals.get(name);
			if (val == null) {
				throw new IllegalArgumentException("I could not find any value with the name '" + name + "'");
			}
			return val;
		}
		
		@Override
		public void value(@SuppressWarnings("unused") Value newValue) {
			throw new UnsupportedOperationException("I don't support changing values");
		}
		
	}
	
}
