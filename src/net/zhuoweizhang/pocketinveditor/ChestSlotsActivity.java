package net.zhuoweizhang.pocketinveditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import net.zhuoweizhang.pocketinveditor.material.MaterialKey;
import net.zhuoweizhang.pocketinveditor.material.RepairableMaterials;
import net.zhuoweizhang.pocketinveditor.material.icon.MaterialIcon;

import net.zhuoweizhang.pocketinveditor.io.xml.MaterialLoader;
import net.zhuoweizhang.pocketinveditor.io.xml.MaterialIconLoader;
import net.zhuoweizhang.pocketinveditor.material.Material;

import net.zhuoweizhang.pocketinveditor.tileentity.*;

public final class ChestSlotsActivity extends ListActivity implements OnItemLongClickListener, LevelDataLoadListener {

	private List<InventorySlot> inventory;

	private ContainerTileEntity container;

	private ArrayAdapter<InventorySlot> inventoryListAdapter;

	public static final int EDIT_SLOT_REQUEST = 534626;

	public static final int DIALOG_SLOT_OPTIONS = 805;

	/** Currently selected slot, used for determining which slot the long-press menu modifies. */

	private int currentlySelectedSlot = -1;

	/** If there was an Intent for a slot editing request returned from the slot editing activity, 
	 * but the app is not able to immediately handle it, it gets stored here. */

	protected Intent slotActivityResultIntent = null;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		getListView().setOnItemLongClickListener(this);

		if (Material.materials == null) {
			//Load the materials on the main thread - may cause slowdowns
			new MaterialLoader(getResources().getXml(R.xml.item_data)).run();
			new MaterialIconLoader(this).run();

		}

		if (EditorActivity.level != null) {
			onLevelDataLoad();
		} else {
			EditorActivity.loadLevelData(this, this, this.getIntent().getStringExtra("world"));
		}
	}

	public void onLevelDataLoad() {
		int tileEntityIndex = this.getIntent().getIntExtra("Index", -1);
		container = (ContainerTileEntity) EditorActivity.level.getTileEntities().get(tileEntityIndex);
		inventory = container.getItems();

		inventoryListAdapter = new MaterialIconArrayAdapter<InventorySlot>(this, R.layout.slot_list_item, R.id.slot_list_main_text, inventory);
		setListAdapter(inventoryListAdapter);
		inventoryListAdapter.notifyDataSetChanged();

		if (slotActivityResultIntent != null) {
			onSlotActivityResult(slotActivityResultIntent);
		}
	}

	public void onStart() {
		super.onStart();
		if (EditorActivity.level != null && inventoryListAdapter != null) {
			inventoryListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (!this.getIntent().getBooleanExtra("CanEditSlots", false)) {
			showGetProMessage();
		} else {
			openInventoryEditScreen(position, inventory.get(position));
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (!this.getIntent().getBooleanExtra("CanEditSlots", false)) return false;
		currentlySelectedSlot = position;
		showDialog(DIALOG_SLOT_OPTIONS);
		return true;
	}

	private void openInventoryEditScreen(int position, InventorySlot slot) {
		Intent intent = new Intent(this, EditInventorySlotActivity.class);
		ItemStack stack = slot.getContents();
		intent.putExtra("TypeId", stack.getTypeId());
		intent.putExtra("Damage", stack.getDurability());
		intent.putExtra("Count", stack.getAmount());
		intent.putExtra("Slot", slot.getSlot());
		intent.putExtra("Index", position);
		startActivityForResult(intent, EDIT_SLOT_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == EDIT_SLOT_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				if (EditorActivity.level != null && inventory != null) {
					onSlotActivityResult(intent);
				} else {
					slotActivityResultIntent = intent;
				}
			}
		}
	}

	protected void onSlotActivityResult(Intent intent) {
		int slotIndex = intent.getIntExtra("Index", -1);
		if (slotIndex < 0) {
			System.err.println("wrong slot index");
			return;
		}
		InventorySlot slot = inventory.get(slotIndex);
		ItemStack stack = slot.getContents();
		stack.setAmount(intent.getIntExtra("Count", 0));
		stack.setDurability(intent.getShortExtra("Damage", (byte) 0));
		stack.setTypeId(intent.getShortExtra("TypeId", (byte) 0));
		EntitiesInfoActivity.save(this);
		slotActivityResultIntent = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if (!this.getIntent().getBooleanExtra("CanEditSlots", false)) return false;
		super.onCreateOptionsMenu(menu);
		menu.add(getResources().getString(R.string.add_empty_slot));
		menu.add(getResources().getString(R.string.warp_to_tile_entity));
		menu.add(getResources().getString(R.string.inventory_repair_all));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getTitle().equals(getResources().getString(R.string.add_empty_slot))){
			InventorySlot newSlot = addEmptySlot();
			if (newSlot != null) {
				openInventoryEditScreen(inventoryListAdapter.getPosition(newSlot), newSlot);
			}
			return true;
		} else if(item.getTitle().equals(getResources().getString(R.string.warp_to_tile_entity))) {
			TileEntityViewActivity.warpToTileEntity(this, container);
			return true;
		} else if (item.getTitle().equals(getResources().getString(R.string.inventory_repair_all))) {
			repairAllItems();
		}
		return super.onOptionsItemSelected(item);
	}

	private InventorySlot addEmptySlot() {
		if(inventory.size() > container.getContainerSize()){
			return null;
		}

		InventorySlot slot = new InventorySlot((byte) inventory.size(), new ItemStack((short)0,(short)0,(short)1));
		alignSlots();
		inventory.add(slot);
		inventoryListAdapter.notifyDataSetChanged();
		EntitiesInfoActivity.save(this);
		return slot;
	}

	private void alignSlots() {
		for(int i = 0; i < inventory.size(); i++){
			inventory.get(i).setSlot((byte) i);
		}
	}

	public Dialog onCreateDialog(int dialogId) {
		switch (dialogId) {
			case DIALOG_SLOT_OPTIONS:
				return createSlotOptionsDialog();
			default:
				return super.onCreateDialog(dialogId);
		}
	}

	public void onPrepareDialog(int dialogId, Dialog dialog) {
		switch (dialogId) {
			case DIALOG_SLOT_OPTIONS:
				InventorySlot slot = inventory.get(currentlySelectedSlot);
				AlertDialog aDialog = (AlertDialog) dialog;
				aDialog.setTitle(slot.toString());
				break;
			default:
				super.onPrepareDialog(dialogId, dialog);
		}
	}

	protected AlertDialog createSlotOptionsDialog() {
		CharSequence[] options = {this.getResources().getText(R.string.slot_duplicate), this.getResources().getText(R.string.slot_delete)};
		return new AlertDialog.Builder(this).setTitle("Slot name goes here").
			setItems(options, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogI, int button) {
					switch (button) {
						case 0: //slot duplicate
							duplicateSelectedSlot();
							break;
						case 1: //slot delete
							deleteSelectedSlot();
							break;
					}
				}
			}).create();
	}

	protected void duplicateSelectedSlot() {
		InventorySlot oldSlot = inventory.get(currentlySelectedSlot);
		InventorySlot newSlot = addEmptySlot();
		if (newSlot != null) {
			ItemStack oldStack = oldSlot.getContents();
			ItemStack newStack = new ItemStack(oldStack);
			newSlot.setContents(newStack);
		}
		inventoryListAdapter.notifyDataSetChanged();
		EntitiesInfoActivity.save(this);
	}

	protected void deleteSelectedSlot() {
		inventory.remove(currentlySelectedSlot);
		inventoryListAdapter.notifyDataSetChanged();
		EntitiesInfoActivity.save(this);
	}

	protected void showGetProMessage() {
		Toast.makeText(this, R.string.get_pro_to_edit_containers, Toast.LENGTH_SHORT).show();
	}

	protected void repairAllItems() {
		int repairedCount = 0;
		for (InventorySlot slot: inventory) {
			ItemStack stack = slot.getContents();
			if (stack.getDurability() > 0 && RepairableMaterials.isRepairable(stack)) {
				stack.setDurability((short) 0);
				repairedCount++;
			}
		}
		inventoryListAdapter.notifyDataSetChanged();
		EntitiesInfoActivity.save(this);
	}

}
