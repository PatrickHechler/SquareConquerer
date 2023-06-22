package de.hechler.patrick.games.sc.values;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.world.WorldThing;

public record WorldThingValue(String name, WorldThing<?, ?> value, UUID uuid, AddableType<?, ?> type) implements Value {
	
	public WorldThingValue(String name, WorldThing<?, ?> value, UUID uuid, AddableType<?, ?> type) {
		if (value != null && type != null || value != null && uuid != null) {
			throw new IllegalArgumentException("why do you use this constructor? (and then incorrectly)");
		}
		this.name  = Objects.requireNonNull(name, "name");
		this.value = value;
		this.uuid  = uuid;
		this.type  = type;
	}
	
	public WorldThingValue(String name, WorldThing<?, ?> value) {
		this(name, value, null, null);
	}
	
	public WorldThingValue(String name, UUID uuid, AddableType<?, ?> type) {
		this(name, null, uuid, type);
	}
	
	public Optional<WorldThing<?, ?>> asOptional() {
		return Optional.ofNullable(this.value);
	}
	
	public boolean isEmpty() {
		return this.value == null;
	}
	
	public boolean hasValue() {
		return this.value != null;
	}
	
	public Optional<AddableType<?, ?>> asOptionalType() {
		return Optional.ofNullable(this.value != null ? this.value.type() : this.type);
	}
	
	public boolean knownType() {
		return this.value != null || this.type != null;
	}
	
	public boolean unknownType() {
		return this.value == null && this.type == null;
	}
	
	public AddableType<?, ?> type() {
		return this.value != null ? this.value.type() : this.type;
	}
	
	public Optional<UUID> asOptionalUUID() {
		return Optional.ofNullable(this.value != null ? this.value.uuid : this.uuid);
	}
	
	public boolean knownUUID() {
		return this.value != null || this.uuid != null;
	}
	
	public boolean unknownUUID() {
		return this.value == null && this.uuid == null;
	}
	
	public UUID uuid() {
		return this.value != null ? this.value.uuid : this.uuid;
	}
	
}

