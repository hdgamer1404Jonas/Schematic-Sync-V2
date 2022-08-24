package de.hdg.schematicsync.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SchemSyncSelection implements InventoryHolder {

    private Inventory inv;

    public SchemSyncSelection() {
        inv = Bukkit.createInventory(this, 27, "Schematic Sync");
        init();

    }

    private void init() {
        ItemStack close;
        close = createItem("Close", Material.BARRIER, Collections.singletonList("Closes the inventory"));
        inv.setItem(26, close);
        ItemStack sync;
        sync = createItem("Sync", Material.GREEN_CONCRETE, Collections.singletonList("Sync a schematic to all Servers. Enter the Schematic name once clicked!"));
        inv.setItem(11, sync);
        ItemStack delete;
        delete = createItem("Delete", Material.RED_CONCRETE, Collections.singletonList("Delete all schematics from the SFTP Server. WARNING: only do this, if you encounter problems with the sync!"));
        inv.setItem(15, delete);
    }

    private ItemStack createItem(String name, Material material, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
