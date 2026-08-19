package ru.amiloxs.beesun.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.amiloxs.beesun.BeeSun;

public final class ItemStorage {
    private final BeeSun plugin;
    private final Map<Integer, Map<Integer, ItemStack>> pages = new HashMap<>();

    public ItemStorage(BeeSun plugin) {
        this.plugin = plugin;
    }

    public synchronized void load(FileConfiguration config) {
        pages.clear();
        if (config == null) return;

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String pageKey : itemsSection.getKeys(false)) {
            int page;
            try {
                if (pageKey.startsWith("page_")) {
                    page = Integer.parseInt(pageKey.substring(5));
                } else {
                    page = Integer.parseInt(pageKey);
                }
            } catch (NumberFormatException ignored) {
                continue;
            }

            ConfigurationSection pageSection = itemsSection.getConfigurationSection(pageKey);
            if (pageSection == null) continue;

            Map<Integer, ItemStack> pageMap = pages.computeIfAbsent(page, k -> new HashMap<>());
            for (String key : pageSection.getKeys(false)) {
                try {
                    int slot;
                    if (key.startsWith("slot_")) {
                        slot = Integer.parseInt(key.substring(5));
                    } else {
                        slot = Integer.parseInt(key);
                    }
                    ItemStack stack = pageSection.getItemStack(key);
                    if (stack != null && stack.getType() != Material.AIR) {
                        pageMap.put(slot, stack);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public synchronized void save(FileConfiguration config, File file) {
        if (config == null || file == null) return;

        config.set("items", null);
        int totalPages = getTotalPages();
        for (int page = 1; page <= totalPages; page++) {
            Map<Integer, ItemStack> pageMap = pages.get(page);
            if (pageMap != null && !pageMap.isEmpty()) {
                for (Map.Entry<Integer, ItemStack> entry : pageMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().getType() != Material.AIR) {
                        config.set("items.page_" + page + ".slot_" + entry.getKey(), entry.getValue());
                    }
                }
            } else {
                config.createSection("items.page_" + page);
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить items.yml: " + e.getMessage());
        }
    }

    public synchronized boolean isPageFull(int page) {
        Map<Integer, ItemStack> pageMap = pages.get(page);
        if (pageMap == null || pageMap.size() < 45) {
            return false;
        }
        for (int slot = 0; slot <= 44; slot++) {
            ItemStack stack = pageMap.get(slot);
            if (stack == null || stack.getType() == Material.AIR) {
                return false;
            }
        }
        return true;
    }

    public synchronized int getTotalPages() {
        int maxPage = 2;
        for (int page : pages.keySet()) {
            if (page > maxPage) {
                maxPage = page;
            }
        }
        for (int p = 1; p <= maxPage; p++) {
            if (isPageFull(p)) {
                if (p + 2 > maxPage) {
                    maxPage = p + 2;
                }
            }
        }
        return maxPage;
    }

    public synchronized Map<Integer, ItemStack> getPage(int page) {
        Map<Integer, ItemStack> map = pages.get(page);
        return map != null ? new HashMap<>(map) : new HashMap<>();
    }

    public synchronized void setPage(int page, Map<Integer, ItemStack> items) {
        Map<Integer, ItemStack> pageMap = pages.computeIfAbsent(page, k -> new HashMap<>());
        pageMap.clear();
        if (items != null) {
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                if (entry.getValue() != null && entry.getValue().getType() != Material.AIR) {
                    pageMap.put(entry.getKey(), entry.getValue().clone());
                }
            }
        }
    }

    public synchronized List<ItemStack> getAllItems() {
        List<ItemStack> list = new ArrayList<>();
        for (Map<Integer, ItemStack> pageMap : pages.values()) {
            for (ItemStack stack : pageMap.values()) {
                if (stack != null && stack.getType() != Material.AIR) {
                    list.add(stack.clone());
                }
            }
        }
        return list;
    }

    public synchronized List<ItemStack> getRandomRewardItems(int min, int max) {
        List<ItemStack> pool = getAllItems();
        if (pool.isEmpty()) {
            return Collections.emptyList();
        }
        int count = min;
        if (max > min) {
            count = ThreadLocalRandom.current().nextInt(min, max + 1);
        }
        if (count <= 0) {
            return Collections.emptyList();
        }

        List<ItemStack> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ItemStack randomItem = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            result.add(randomItem.clone());
        }
        return result;
    }
}
