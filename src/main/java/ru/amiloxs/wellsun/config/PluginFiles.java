package ru.amiloxs.wellsun.config;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.amiloxs.wellsun.WellSun;
import ru.amiloxs.wellsun.storage.ItemStorage;

public final class PluginFiles {
    private final WellSun plugin;
    private FileConfiguration block;
    private FileConfiguration particle;
    private FileConfiguration messages;
    private FileConfiguration items;
    private final File itemsFile;

    public PluginFiles(WellSun plugin) {
        this.plugin = plugin;
        save("block.yml");
        save("particle.yml");
        save("messages.yml");
        save("items.yml");
        this.itemsFile = new File(plugin.getDataFolder(), "items.yml");
        reload();
    }

    public void reload() {
        block = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "block.yml"));
        particle = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "particle.yml"));
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        items = YamlConfiguration.loadConfiguration(itemsFile);
        if (plugin.getItemStorage() != null) {
            plugin.getItemStorage().load(items);
        }
    }

    public void saveItemsStorage(ItemStorage storage) {
        if (storage != null && items != null && itemsFile != null) {
            storage.save(items, itemsFile);
        }
    }

    public FileConfiguration block() {
        return block;
    }

    public FileConfiguration particle() {
        return particle;
    }

    public FileConfiguration messages() {
        return messages;
    }

    public FileConfiguration items() {
        return items;
    }

    public String message(String path) {
        return messages != null ? messages.getString(path, "") : "";
    }

    private void save(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }
}
