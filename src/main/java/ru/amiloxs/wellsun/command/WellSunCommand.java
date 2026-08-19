package ru.amiloxs.wellsun.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.amiloxs.wellsun.WellSun;
import ru.amiloxs.wellsun.editor.EditorGui;
import ru.amiloxs.wellsun.service.WellService;

public final class WellSunCommand implements CommandExecutor, TabCompleter {
    private final WellSun plugin;
    private final WellService service;

    public WellSunCommand(WellSun plugin, WellService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wellsun.admin") && !sender.isOp()) {
            sender.sendMessage(WellSun.color(plugin.files().message("no-permission")));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                plugin.files().reload();
                service.reload();
                sender.sendMessage(WellSun.color(plugin.files().message("reloaded")));
                return true;
            }

            if (args[0].equalsIgnoreCase("editor")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(WellSun.color(plugin.files().message("only-players")));
                    return true;
                }
                Player player = (Player) sender;
                EditorGui.open(player, 1, plugin);
                int totalPages = plugin.getItemStorage().getTotalPages();
                String msg = plugin.files().message("editor.opened")
                        .replace("%page%/2", "%page%/" + totalPages)
                        .replace("%page%", "1")
                        .replace("%max_page%", String.valueOf(totalPages))
                        .replace("%max_pages%", String.valueOf(totalPages));
                if (!msg.isEmpty()) {
                    player.sendMessage(WellSun.color(msg));
                }
                return true;
            }
        }

        sender.sendMessage(WellSun.color(plugin.files().message("usage")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if ((sender.hasPermission("wellsun.admin") || sender.isOp()) && args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : Arrays.asList("reload", "editor")) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
