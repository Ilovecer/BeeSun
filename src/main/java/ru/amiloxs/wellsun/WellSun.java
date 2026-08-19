package ru.amiloxs.wellsun;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.amiloxs.wellsun.command.WellSunCommand;
import ru.amiloxs.wellsun.config.PluginFiles;
import ru.amiloxs.wellsun.editor.EditorListener;
import ru.amiloxs.wellsun.service.WellService;
import ru.amiloxs.wellsun.storage.ItemStorage;
import ru.amiloxs.wellsun.util.ColorUtil;

public final class WellSun extends JavaPlugin {
    private WellService wellService;
    private PluginFiles files;
    private ItemStorage itemStorage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        itemStorage = new ItemStorage(this);
        files = new PluginFiles(this);
        wellService = new WellService(this, files);
        wellService.reload();

        PluginCommand command = getCommand("wellsun");
        WellSunCommand executor = new WellSunCommand(this, wellService);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getServer().getPluginManager().registerEvents(wellService, this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);

        getLogger().info("WellSun успешно запущен.");
        Bukkit.getScheduler().runTask(this, () -> Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.isOp() || player.hasPermission("wellsun.admin"))
                .forEach(player -> player.sendMessage(color(files.message("enabled")))));
    }

    @Override
    public void onDisable() {
        if (wellService != null) {
            wellService.close();
        }
        if (itemStorage != null && files != null) {
            files.saveItemsStorage(itemStorage);
        }
    }

    public static String color(String text) {
        return ColorUtil.color(text);
    }

    public PluginFiles files() {
        return files;
    }

    public ItemStorage getItemStorage() {
        return itemStorage;
    }
}
