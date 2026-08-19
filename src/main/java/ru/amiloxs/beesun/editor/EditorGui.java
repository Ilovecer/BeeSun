package ru.amiloxs.beesun.editor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.amiloxs.beesun.BeeSun;

public final class EditorGui {

    private EditorGui() {}

    public static void open(Player player, int requestedPage, BeeSun plugin) {
        final int totalPages = plugin.getItemStorage().getTotalPages();
        final int page = Math.max(1, Math.min(requestedPage, totalPages));

        EditorHolder holder = new EditorHolder(page);
        String titleRaw = plugin.files().message("editor.title");
        if (titleRaw == null || titleRaw.isEmpty()) {
            titleRaw = "<#00D8FF>&lЯдро Солнца &8| &fПредметы &7(Стр. %page%/%max_page%)";
        }
        String title = BeeSun.color(titleRaw
                .replace("%page%/2", "%page%/" + totalPages)
                .replace("%page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(totalPages))
                .replace("%max_pages%", String.valueOf(totalPages))
                .replace("%total_pages%", String.valueOf(totalPages)));
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        // Fill saved items in slots 0..44
        Map<Integer, ItemStack> items = plugin.getItemStorage().getPage(page);
        for (int slot = 0; slot <= 44; slot++) {
            ItemStack stack = items.get(slot);
            if (stack != null && stack.getType() != Material.AIR) {
                inv.setItem(slot, stack.clone());
            }
        }

        // Purple pane item for decoration (slots 45-53)
        String paneName = plugin.files().message("editor.pane-name");
        ItemStack purplePane = createItem(Material.PURPLE_STAINED_GLASS_PANE, paneName != null && !paneName.isEmpty() ? paneName : "<#00D8FF> ");

        // Slot 45: Previous page arrow or purple pane
        if (page > 1) {
            String prevName = plugin.files().message("editor.prev-page-name");
            if (prevName == null || prevName.isEmpty()) {
                prevName = "« <#00D8FF>Предыдущая страница &7(Стр. %prev_page%)";
            }
            prevName = prevName
                    .replace("%prev_page%", String.valueOf(page - 1))
                    .replace("%page%", String.valueOf(page))
                    .replace("%max_page%", String.valueOf(totalPages))
                    .replace("%max_pages%", String.valueOf(totalPages));

            List<String> prevLore = plugin.files().messages().getStringList("editor.prev-page-lore");
            List<String> formattedLore = prevLore.stream()
                    .map(l -> l.replace("%prev_page%", String.valueOf(page - 1))
                               .replace("%page%", String.valueOf(page))
                               .replace("%max_page%", String.valueOf(totalPages))
                               .replace("%max_pages%", String.valueOf(totalPages)))
                    .collect(Collectors.toList());

            inv.setItem(45, createItem(Material.ARROW, prevName, formattedLore));
        } else {
            inv.setItem(45, purplePane);
        }

        // Slots 46-52: Purple panes
        for (int slot = 46; slot <= 52; slot++) {
            inv.setItem(slot, purplePane);
        }

        // Slot 53: Next page arrow or purple pane
        if (page < totalPages) {
            String nextName = plugin.files().message("editor.next-page-name");
            if (nextName == null || nextName.isEmpty()) {
                nextName = "<#00D8FF>Следующая страница &7(Стр. %next_page%) »";
            }
            nextName = nextName
                    .replace("%next_page%", String.valueOf(page + 1))
                    .replace("%page%", String.valueOf(page))
                    .replace("%max_page%", String.valueOf(totalPages))
                    .replace("%max_pages%", String.valueOf(totalPages));

            List<String> nextLore = plugin.files().messages().getStringList("editor.next-page-lore");
            List<String> formattedLore = nextLore.stream()
                    .map(l -> l.replace("%next_page%", String.valueOf(page + 1))
                               .replace("%page%", String.valueOf(page))
                               .replace("%max_page%", String.valueOf(totalPages))
                               .replace("%max_pages%", String.valueOf(totalPages)))
                    .collect(Collectors.toList());

            inv.setItem(53, createItem(Material.ARROW, nextName, formattedLore));
        } else {
            inv.setItem(53, purplePane);
        }

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(BeeSun.color(name));
            }
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream().map(BeeSun::color).collect(Collectors.toList()));
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack createItem(Material material, String name) {
        return createItem(material, name, null);
    }
}
