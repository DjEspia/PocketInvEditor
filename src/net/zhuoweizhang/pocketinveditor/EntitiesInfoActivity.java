package net.zhuoweizhang.pocketinveditor;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.zhuoweizhang.pocketinveditor.entity.*;
import net.zhuoweizhang.pocketinveditor.io.EntityDataConverter;
import net.zhuoweizhang.pocketinveditor.tileentity.*;
import net.zhuoweizhang.pocketinveditor.util.Vector;

public class EntitiesInfoActivity extends Activity implements View.OnClickListener {

	private TextView entityCountText;

	private List<Entity> entitiesList;

	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entities_info);
		entityCountText = (TextView) findViewById(R.id.entities_main_count);

		loadEntities();
	}

	protected void loadEntities() {
		new Thread(new LoadEntitiesTask()).start();
	}

	protected void onEntitiesLoaded(EntityDataConverter.EntityData entitiesDat) {
		EditorActivity.level.setEntities(entitiesDat.entities);
		EditorActivity.level.setTileEntities(entitiesDat.tileEntities);
		this.entitiesList = entitiesDat.entities;
		countEntities();
	}

	public void onClick(View v) {
	}

	protected void countEntities() {
		Map<EntityType, Integer> countMap = new EnumMap<EntityType, Integer>(EntityType.class);
		for (Entity e: entitiesList) {
			EntityType entityType = e.getEntityType();
			int newCount = 1;
			Integer oldCount = countMap.get(entityType);
			if (oldCount != null) {
				newCount += oldCount;
			}
			countMap.put(entityType, newCount);
		}

		String entityCountString = buildEntityCountString(countMap);
		if (entityCountString.length() == 0) {
			entityCountString = this.getResources().getText(R.string.entities_none).toString();
		}
		entityCountText.setText(entityCountString);
	}

	private String buildEntityCountString(Map<EntityType, Integer> countMap) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<EntityType, Integer> entry: countMap.entrySet()) {
			builder.append(this.getResources().getText(EntityTypeLocalization.namesMap.get(entry.getKey())));
			builder.append(':');
			builder.append(entry.getValue());
			builder.append('\n');
		}
		return builder.toString();
	}

	public void apoCowlypse() {
		List<Entity> list = EditorActivity.level.getEntities();
		Vector playerLoc = EditorActivity.level.getPlayer().getLocation();
		int beginX = (int) playerLoc.getX() - 16;
		int endX = (int) playerLoc.getX() + 16;
		int beginZ = (int) playerLoc.getZ() - 16;
		int endZ = (int) playerLoc.getZ() + 16;
		for (int x = beginX; x < endX; x += 2) {
			for (int z = beginZ; z < endZ; z += 2) {
				Cow cow = new Cow();
				cow.setLocation(new Vector(x, 128, z));
				cow.setEntityTypeId(EntityType.COW.getId());
				cow.setHealth((short) 128);
				list.add(cow);
			}
		}
		save(this);
		countEntities();
	}

	public void cowTipping(EntityType type, short tipNess) {
		List<Entity> list = EditorActivity.level.getEntities();
		for (Entity entity: list) {
			if (entity.getEntityType() == type) {
				((LivingEntity) entity).setDeathTime(tipNess);
			}
		}
		save(this);
	}

	public void spawnMobs(EntityType type, Vector loc, int count) throws Exception {
		List<Entity> entities = EditorActivity.level.getEntities();
		for (int i = 0; i < count; i++) {
			Entity e = type.getEntityClass().newInstance();
			e.setEntityTypeId(type.getId());
			e.setLocation(loc);
			if (e instanceof LivingEntity) {
				((LivingEntity) e).setHealth((short) ((LivingEntity) e).getMaxHealth());
			}
			entities.add(e);
		}
	}

	public int removeEntities(EntityType type) {
		int removedCount = 0;
		List<Entity> entities = EditorActivity.level.getEntities();
		for (int i = entities.size() - 1; i >= 0; --i) {
			Entity e = entities.get(i);
			if (e.getEntityType() == type) {
				entities.remove(i);
				removedCount++;
			}
		}
		return removedCount;
	}

	public void babyfyMobs(EntityType type) {
		List<Entity> entities = EditorActivity.level.getEntities();
		for (Entity e: entities) {
			if (e.getEntityType() == type) {
				((Animal) e).setAge(-24000);
			}
		}
	}

	private class LoadEntitiesTask implements Runnable {
		public void run() {
			File entitiesFile = new File(EditorActivity.worldFolder, "entities.dat");
			try {
				final EntityDataConverter.EntityData entitiesList = EntityDataConverter.read(entitiesFile);
				EntitiesInfoActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						EntitiesInfoActivity.this.onEntitiesLoaded(entitiesList);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				EntitiesInfoActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						EntitiesInfoActivity.this.onEntitiesLoaded(new EntityDataConverter.EntityData(
							new ArrayList<Entity>(), new ArrayList<TileEntity>()));
					}
				});
			}
		}
	}

	public static void save(final Activity context) {
		new Thread(new Runnable() {
			public void run() {
				try {
					EntityDataConverter.write(EditorActivity.level.getEntities(), EditorActivity.level.getTileEntities(), 
						new File(EditorActivity.worldFolder, "entities.dat"));
					if (context != null) {
						context.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show();
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (context != null) {
						context.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(context, R.string.savefailed, Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			}
		}).start();
	}
}
