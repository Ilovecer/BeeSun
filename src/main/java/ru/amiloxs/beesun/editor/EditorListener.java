package ru.amiloxs.beesun.editor;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.amiloxs.beesun.BeeSun;

public final class EditorListener implements Listener {
    private final BeeSun plugin;

    public EditorListener(BeeSun plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top == null || !(top.getHolder() instanceof EditorHolder)) {
            return;
        }

        EditorHolder holder = (EditorHolder) top.getHolder();
        int rawSlot = event.getRawSlot();

        if (rawSlot >= 0 && rawSlot < 54) {
            if (rawSlot >= 45 && rawSlot <= 53) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();

                // Previous page click: slot 45
                if (rawSlot == 45 && holder.getPage() > 1) {
                    savePageItems(top, holder.getPage());
                    EditorGui.open(player, holder.getPage() - 1, plugin);
                    return;
                }

                // Next page click: slot 53
                if (rawSlot == 53) {
                    savePageItems(top, holder.getPage());
                    int totalPages = plugin.getItemStorage().getTotalPages();
                    if (holder.getPage() < totalPages) {
                        EditorGui.open(player, holder.getPage() + 1, plugin);
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top == null || !(top.getHolder() instanceof EditorHolder)) {
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 45 && rawSlot <= 53) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory top = event.getInventory();
        if (top == null || !(top.getHolder() instanceof EditorHolder)) {
            return;
        }

        EditorHolder holder = (EditorHolder) top.getHolder();
        savePageItems(top, holder.getPage());
        plugin.files().saveItemsStorage(plugin.getItemStorage());

        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            String msg = plugin.files().message("editor.saved");
            if (msg != null && !msg.isEmpty()) {
                player.sendMessage(BeeSun.color(msg));
            }
        }
    }

    private void savePageItems(Inventory inventory, int page) {
        Map<Integer, ItemStack> pageItems = new HashMap<>();
        for (int slot = 0; slot <= 44; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null && stack.getType() != Material.AIR) {
                pageItems.put(slot, stack.clone());
            }
        }
        plugin.getItemStorage().setPage(page, pageItems);
    }
}
