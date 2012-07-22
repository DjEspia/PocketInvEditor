package net.zhuoweizhang.pocketinveditor;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.zhuoweizhang.pocketinveditor.util.Vector;

public final class WorldInfoActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {

	private static final int DIALOG_CHANGE_GAME_MODE = 1167366;

	private static final String[] GAMEMODES = {"Survival", "Creative"};

	private TextView gameModeText;

	private Button gameModeChangeButton;

	private EditText worldTimeText;

	private Button timeToMorningButton;

	private Button timeToNightButton;

	private Button spawnToPlayerButton;

	private Button warpToSpawnButton;

	private TextView playerXText, playerYText, playerZText;

	private EditText healthText;

	private Button fullHealthButton, infiniteHealthButton;

	private Button sidewaysOnButton, sidewaysOffButton;

	private TextView worldNameText;

	private TextView worldFolderNameText;

	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.world_info);
		gameModeText = (TextView) findViewById(R.id.world_info_gamemode);
		gameModeChangeButton = (Button) findViewById(R.id.world_info_gamemode_change);
		gameModeChangeButton.setOnClickListener(this);
		gameModeText.setText(EditorActivity.level.getGameType() == 1 ? R.string.gamemode_creative : R.string.gamemode_survival);
		worldTimeText = (EditText) findViewById(R.id.world_info_time_text);
		updateTimeText();
		worldTimeText.setOnFocusChangeListener(this);
		spawnToPlayerButton = (Button) findViewById(R.id.world_info_spawn_to_player_button);
		spawnToPlayerButton.setOnClickListener(this);
		warpToSpawnButton = (Button) findViewById(R.id.world_info_warp_to_spawn_button);
		warpToSpawnButton.setOnClickListener(this);
		timeToMorningButton = (Button) findViewById(R.id.world_info_time_to_morning);
		timeToMorningButton.setOnClickListener(this);
		timeToNightButton = (Button) findViewById(R.id.world_info_time_to_night);
		timeToNightButton.setOnClickListener(this);
		playerXText = (TextView) findViewById(R.id.world_info_player_x);
		playerYText = (TextView) findViewById(R.id.world_info_player_y);
		playerZText = (TextView) findViewById(R.id.world_info_player_z);
		updatePlayerPositionText();
		healthText = (EditText) findViewById(R.id.world_info_health);
		fullHealthButton = (Button) findViewById(R.id.world_info_full_health);
		infiniteHealthButton = (Button) findViewById(R.id.world_info_infinite_health);
		healthText.setOnFocusChangeListener(this);
		fullHealthButton.setOnClickListener(this);
		infiniteHealthButton.setOnClickListener(this);
		updatePlayerHealthText();
		sidewaysOnButton = (Button) findViewById(R.id.world_info_sideways_on);
		sidewaysOffButton = (Button) findViewById(R.id.world_info_sideways_off);
		sidewaysOnButton.setOnClickListener(this);
		sidewaysOffButton.setOnClickListener(this);
		worldNameText = (TextView) findViewById(R.id.world_info_name);
		worldNameText.setText(EditorActivity.level.getLevelName());
		worldNameText.setOnFocusChangeListener(this);
		worldFolderNameText = (TextView) findViewById(R.id.world_info_folder_name);
		worldFolderNameText.setText(EditorActivity.worldFolder.getName());
		worldFolderNameText.setOnFocusChangeListener(this);
	}

	public void updateTimeText() {
		worldTimeText.setText(Long.toString(EditorActivity.level.getTime()));
	}

	public void updatePlayerPositionText() {
		Vector loc = EditorActivity.level.getPlayer().getLocation();
		playerXText.setText("X: " + Float.toString(loc.getX()));
		playerYText.setText("Y: " + Float.toString(loc.getY()));
		playerZText.setText("Z: " + Float.toString(loc.getZ()));
	}

	private void setSpawnToPlayerPosition() {
		Level level = EditorActivity.level;
		Vector loc = level.getPlayer().getLocation();
		level.setSpawnX((int) loc.getX());
		level.setSpawnY((int) loc.getY());
		level.setSpawnZ((int) loc.getZ());
	}

	private void setTimeToMorning() {
		EditorActivity.level.setTime((EditorActivity.level.getTime() / 14400) * 14400);
		EditorActivity.save(this);
	}

	private void setTimeToNight() {
		EditorActivity.level.setTime(((EditorActivity.level.getTime() / 14400) * 14400) + 7200);
		EditorActivity.save(this);
	}

	public void updatePlayerHealthText() {
		healthText.setText(Short.toString(EditorActivity.level.getPlayer().getHealth()));
	}

	private void setPlayerHealthToFull() {
		EditorActivity.level.getPlayer().setHealth((short) 20);
		EditorActivity.save(this);
		updatePlayerHealthText();
	}

	private void setPlayerHealthToInfinite() {
		EditorActivity.level.getPlayer().setHealth(Short.MAX_VALUE);
		EditorActivity.save(this);
		updatePlayerHealthText();
	}

	private void warpPlayerToSpawn() {
		Vector loc = EditorActivity.level.getPlayer().getLocation();
		Level level = EditorActivity.level;
		loc.setX(level.getSpawnX());
		loc.setY(level.getSpawnY());
		loc.setZ(level.getSpawnZ());
		EditorActivity.save(this);
		updatePlayerPositionText();
	}

        private void playerSideways(boolean doThis) {
                EditorActivity.level.getPlayer().setDeathTime(doThis? Short.MAX_VALUE : 0); 
                EditorActivity.save(this);
        }


	public void onClick(View v) {
		if (v == gameModeChangeButton) {
			showDialog(DIALOG_CHANGE_GAME_MODE);
		} else if (v == spawnToPlayerButton) {
			setSpawnToPlayerPosition();
			EditorActivity.save(this);
		} else if (v == timeToMorningButton) {
			setTimeToMorning();
			updateTimeText();
		} else if (v == timeToNightButton) {
			setTimeToNight();
			updateTimeText();
		} else if (v == fullHealthButton) {
			setPlayerHealthToFull();
		} else if (v == infiniteHealthButton) {
			setPlayerHealthToInfinite();
		} else if (v == warpToSpawnButton) {
			warpPlayerToSpawn();
		} else if (v == sidewaysOnButton) {
			playerSideways(true);
		} else if (v == sidewaysOffButton) {
			playerSideways(false);
		}
	}

	public Dialog onCreateDialog(int dialogId) {
		switch (dialogId) {
			case DIALOG_CHANGE_GAME_MODE:
				return createChangeGameModeDialog();
			default:
				return super.onCreateDialog(dialogId);
		}
	}

	public void onPrepareDialog(int dialogId, Dialog dialog) {
		switch (dialogId) {
			case DIALOG_CHANGE_GAME_MODE:
				int levelType = EditorActivity.level.getGameType() == 1 ? 1 : 0;
				((AlertDialog) dialog).getListView().setSelection(levelType);
				break;
			default:
				super.onPrepareDialog(dialogId, dialog);
		}
	}

	protected AlertDialog createChangeGameModeDialog() {
		return new AlertDialog.Builder(this).setTitle(R.string.gamemode).
			setSingleChoiceItems(GAMEMODES, -1, 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						EditorActivity.level.setGameType(item);
						gameModeText.setText(EditorActivity.level.getGameType() == 1 ? 
							R.string.gamemode_creative : R.string.gamemode_survival);
						EditorActivity.save(WorldInfoActivity.this);
						dialog.dismiss();
					}
			}).create();
	}

	public void onFocusChange(View v, boolean hasFocus) {
		if (v == worldTimeText) {
			if (!hasFocus) {
				checkTimeInputAfterChange();
			}
		} else if (v == healthText) {
			if (!hasFocus) {
				checkHealthInputAfterChange();
			}
		} else if (v == worldNameText) {
			if (!hasFocus) {
				checkWorldNameAfterChange();
			}
		} else if (v == worldFolderNameText) {
			if (!hasFocus) {
				checkWorldFolderNameAfterChange();
			}
		}
	}

	public void onPause() {
		super.onPause();
		checkTimeInputAfterChange();
		checkHealthInputAfterChange();
		checkWorldNameAfterChange();
		checkWorldFolderNameAfterChange();
	}

	public void checkTimeInputAfterChange() {
		long newTime = 0;
		try {
			newTime = Long.parseLong(worldTimeText.getText().toString());
			worldTimeText.setError(null);
			if (newTime != EditorActivity.level.getTime()) {
				EditorActivity.level.setTime(newTime);
				EditorActivity.save(this);
			}
		} catch (NumberFormatException e) {
			worldTimeText.setError(this.getResources().getText(R.string.invalid_number));
		}
	}

	public void checkHealthInputAfterChange() {
		short newHealth = 0;
		try {
			newHealth = Short.parseShort(healthText.getText().toString());
			healthText.setError(null);
			if (newHealth != EditorActivity.level.getPlayer().getHealth()) {
				EditorActivity.level.getPlayer().setHealth(newHealth);
				EditorActivity.save(this);
			}
		} catch (NumberFormatException e) {
			healthText.setError(this.getResources().getText(R.string.invalid_number));
		}
	}

	protected void checkWorldNameAfterChange() {
		String newText = worldNameText.getText().toString();
		if (!newText.equals(EditorActivity.level.getLevelName())) {
			EditorActivity.level.setLevelName(newText);
			EditorActivity.save(this);
		}
	}

	protected void checkWorldFolderNameAfterChange() {
		String newText = worldFolderNameText.getText().toString();
		if (!newText.equals(EditorActivity.worldFolder.getName())) {
			File newLoc = new File(EditorActivity.worldFolder.getParentFile(), newText);
			if (newLoc.exists()) {
				worldFolderNameText.setError(this.getResources().getText(R.string.folder_exists));
				return;
			}
			worldFolderNameText.setError(null);
			boolean success = EditorActivity.worldFolder.renameTo(newLoc);
			if (!success) {
				worldFolderNameText.setError(this.getResources().getText(R.string.folder_rename_failed));
			} else {
				Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
				EditorActivity.worldFolder = newLoc;
			}
		}
	}
}
