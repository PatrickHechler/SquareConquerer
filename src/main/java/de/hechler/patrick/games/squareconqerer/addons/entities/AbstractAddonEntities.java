package de.hechler.patrick.games.squareconqerer.addons.entities;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.objects.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public abstract class AbstractAddonEntities implements AddonEntities {
	
	private final Map<Class<? extends Entity>, String> entitiCls;
	
	
	public AbstractAddonEntities(Map<Class<? extends Entity>, String> entitiCls) {
		this.entitiCls = Collections.unmodifiableMap(new HashMap<>(entitiCls));
	}
	
	@Override
	public void sendUnit(Connection conn, Unit u) throws IOException {
		conn.writeInt(u.x());
		conn.writeInt(u.y());
		conn.writeInt(u.lives());
		int ca = u.carryAmount();
		conn.writeInt(ca);
		if (ca != 0) {
			OpenWorld.writeRes(conn, u.carryRes());
		}
		finishSendUnit(conn, u);
	}
	
	protected abstract void finishSendUnit(Connection conn, Unit u) throws IOException;
	
	@Override
	public Unit recieveUnit(Connection conn, User usr) throws IOException, StreamCorruptedException {
		int x = conn.readInt();
		int y = conn.readInt();
		if (x < 0 || y < 0) throw new StreamCorruptedException("negative coordinates");
		int lives = conn.readInt();
		if (lives < 0) throw new StreamCorruptedException("negative live count");
		int ca = conn.readInt();
		if (ca < 0) throw new StreamCorruptedException("negative carry amount");
		Resource res = null;
		if (ca != 0) {
			res = RemoteWorld.readRes(conn);
		}
		return finishRecieveUnit(conn, usr, x, y, lives, ca, res);
	}
	
	protected abstract Unit finishRecieveUnit(Connection conn, User usr, int x, int y, int lives, int ca, Resource res) throws IOException;
	
	@Override
	public void sendBuild(Connection conn, Building b) throws IOException {
		conn.writeInt(b.x());
		conn.writeInt(b.y());
		conn.writeInt(b.lives());
		boolean fb = b.isFinishedBuild();
		conn.writeByte(fb ? 1 : 0);
		if (!fb) {
			conn.writeInt(b.remainingBuildTurns());
			EnumIntMap<ProducableResourceType> res = b.neededResources();
			if (res == null) {
				conn.writeInt(0);
			} else {
				int[] arr = res.array();
				conn.writeInt(arr.length);
				for (int i = 0; i < arr.length; i++) {
					conn.writeInt(arr[i]);
				}
			}
		}
	}
	
	protected abstract void finishSendBuild(Connection conn, Building b) throws IOException, StreamCorruptedException;
	
	@Override
	public Building recieveBuild(Connection conn, User usr) throws IOException, StreamCorruptedException {
		int x     = conn.readInt();
		int y     = conn.readInt();
		int lives = conn.readInt();
		if (x < 0 || y < 0 || lives < 0) throw new StreamCorruptedException("negative amount");
		int                                remTurns = 0;
		EnumIntMap<ProducableResourceType> res      = null;
		boolean fb = conn.readByte(0, 1) != 0;
		if (fb) {
			remTurns = conn.readInt();
			if (remTurns < 0) throw new StreamCorruptedException("negative amount");
			if (conn.readInt(0, ProducableResourceType.count()) != 0) {
				res = new EnumIntMap<>(ProducableResourceType.class);
				int[] arr = res.array();
				for (int i = 0; i < arr.length; i++) {
					arr[i] = conn.readInt();
					if (arr[i] < 0) throw new StreamCorruptedException("negative amount");
				}
			}
		}
		return finishRecieveBuild(conn, usr, x, y, lives, fb, remTurns, res);
	}
	
	protected abstract Building finishRecieveBuild(Connection conn, User usr, int x, int y, int lives, boolean fb, int remTurns,
			EnumIntMap<ProducableResourceType> res) throws IOException, StreamCorruptedException;
	
	@Override
	public Map<Class<? extends Entity>, String> entityClassses() { return entitiCls; }
	
}
