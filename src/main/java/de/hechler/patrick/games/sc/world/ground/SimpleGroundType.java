package de.hechler.patrick.games.sc.world.ground;

import java.awt.Image;
import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.spec.ValueSpec;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.utils.objects.Random2;

public abstract class SimpleGroundType extends GroundType {
	
	private Image img;
	
	public SimpleGroundType(String name, String localName) {
		super(name, localName, Map.of());
	}
	
	public Ground newInstance(Random2 r) {
		return new SimpleGround(r.nextUUID());
	}
	
	@Override
	public Ground withValues(Map<String, Value> values, UUID uuid) throws TurnExecutionException {
		if (!values.isEmpty()) {
			throw new IllegalArgumentException("too many values");
		}
		return new SimpleGround(uuid);
	}
	
	@Override
	public Ground withRandomValues(World w, Random2 r, int x, int y) {
		return new SimpleGround(r.nextUUID());
	}
	
	@Override
	public Ground withDefaultValues(World w, Random2 r, int x, int y) {
		return new SimpleGround(r.nextUUID());
	}
	
	@Override
	public Map<String, ValueSpec> specs() {
		return Map.of();
	}
	
	@Override
	public ValueSpec spec(String name) {
		throw new UnsupportedOperationException("I don't have any values");
	}
	
	protected abstract Image loadImage();
	
	public class SimpleGround extends Ground {
		
		public SimpleGround(UUID uuid) {
			super(uuid);
		}
		
		@Override
		public Image image(int width, int heigh) {
			if (SimpleGroundType.this.img == null) {
				synchronized (SimpleGroundType.this) {
					SimpleGroundType.this.img = SimpleGroundType.this.loadImage();
				}
			}
			return SimpleGroundType.this.img;
		}
		
		@Override
		public GroundType type() {
			return SimpleGroundType.this;
		}
		
		@Override
		public Ground unmodifiable() {
			return this;
		}
		
		@Override
		public Map<String, Value> values() {
			return Map.of();
		}
		
		@Override
		public Value value(String name) {
			throw new UnsupportedOperationException("I don't have any values");
		}
		
		@Override
		public void value(Value newValue) {
			throw new UnsupportedOperationException("I don't have any values");
		}
		
	}
	
}
