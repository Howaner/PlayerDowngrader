package de.howaner.PlayerDowngrader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDowngraderPlugin extends JavaPlugin {
	public static Logger log;

	@Override
	public void onEnable() {
		log = this.getLogger();
		this.startConvert();
	}

	@Override
	public void onDisable() {
		
	}

	public void startConvert() {
		for (World world : Bukkit.getWorlds()) {
			File folder = new File(world.getWorldFolder(), "playerdata");
			for (File playerFile : folder.listFiles()) {
				if (!playerFile.isFile()) {
					continue;
				}

				try {
					log.log(Level.INFO, "Convert {0} ...", playerFile.getName());
					NBTTagCompound rootCompound = NBTCompressedStreamTools.a(new FileInputStream(playerFile));

					NBTTagCompound[] inventoryItems = this.readInventory(rootCompound, "Inventory");
					this.downgradeItems(inventoryItems);

					NBTTagCompound[] enderChestItems = this.readInventory(rootCompound, "EnderItems");
					this.downgradeItems(enderChestItems);

					playerFile.delete();
					NBTCompressedStreamTools.a(rootCompound, new FileOutputStream(playerFile));
				} catch (Exception ex) {
					log.log(Level.WARNING, "Can't convert " + playerFile.getName(), ex);
				}
			}
		}
	}

	public Item getItemFromName(String name) {
		if (name.startsWith("minecraft:")) {
			name = name.substring(10);
		}

		return (Item) Item.REGISTRY.get(name);
	}

	public void downgradeItem(NBTTagCompound compound) {
		if (!compound.hasKeyOfType("id", 8)) {
			// Not a string -> Already 1.7
			return;
		}

		String itemName = compound.getString("id");
		Item item = this.getItemFromName(itemName);
		if (item == null) {
			log.log(Level.INFO, "Unknown item: {0}", itemName);
			return;
		}

		int id = Item.getId(item);
		compound.setShort("id", (short) id);
	}

	public void downgradeItems(NBTTagCompound[] compounds) {
		for (NBTTagCompound compound : compounds) {
			this.downgradeItem(compound);
		}
	}

	public NBTTagCompound[] readInventory(NBTTagCompound rootCompound, String tag) {
		NBTTagList itemsList = rootCompound.getList(tag, 10);
		NBTTagCompound[] itemCompounds = new NBTTagCompound[itemsList.size()];

		for (int i = 0; i < itemsList.size(); i++) {
			itemCompounds[i] = itemsList.get(i);
		}

		return itemCompounds;
	}

}
