package ru.amiloxs.beesun;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.amiloxs.beesun.command.BeeSunCommand;
import ru.amiloxs.beesun.config.PluginFiles;
import ru.amiloxs.beesun.editor.EditorListener;
import ru.amiloxs.beesun.service.BeeService;
import ru.amiloxs.beesun.storage.ItemStorage;
import ru.amiloxs.beesun.util.ColorUtil;

public final class BeeSun extends JavaPlugin {
    private BeeService beeService;
    private PluginFiles files;
    private ItemStorage itemStorage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        itemStorage = new ItemStorage(this);
        files = new PluginFiles(this);
        beeService = new BeeService(this, files);
        beeService.reload();

        PluginCommand command = getCommand("beesun");
        BeeSunCommand executor = new BeeSunCommand(this, beeService);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getServer().getPluginManager().registerEvents(beeService, this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);

        getLogger().info("BeeSun успешно запущен.");
        Bukkit.getScheduler().runTask(this, () -> Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.isOp() || player.hasPermission("beesun.admin"))
                .forEach(player -> player.sendMessage(color(files.message("enabled")))));
    }

    @Override
    public void onDisable() {
        if (beeService != null) {
            beeService.close();
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
