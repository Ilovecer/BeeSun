package ru.amiloxs.beesun.editor;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class EditorHolder implements InventoryHolder {
    private final int page;
    private Inventory inventory;

    public EditorHolder(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
