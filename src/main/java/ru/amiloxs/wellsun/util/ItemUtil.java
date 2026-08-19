package ru.amiloxs.wellsun.util;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.amiloxs.wellsun.WellSun;

public final class ItemUtil {
    private static final Map<Material, String> RUSSIAN_NAMES = new EnumMap<>(Material.class);

    static {
        // Blocks & Basic materials
        put("DIRT", "Земля");
        put("GRASS_BLOCK", "Блок травы");
        put("STONE", "Камень");
        put("COBBLESTONE", "Булыжник");
        put("OAK_LOG", "Дубовое бревно");
        put("OAK_PLANKS", "Дубовые доски");
        put("BEDROCK", "Бедрок");
        put("SAND", "Песок");
        put("GRAVEL", "Гравий");
        put("OBSIDIAN", "Обсидиан");
        put("CRYING_OBSIDIAN", "Плачущий обсидиан");
        put("RESPAWN_ANCHOR", "Якорь возрождения");
        put("BEACON", "Маяк");
        put("TNT", "Динамит");
        put("BOOKSHELF", "Книжная полка");
        put("ENCHANTING_TABLE", "Стол зачаровывания");
        put("ANVIL", "Наковальня");
        put("CHEST", "Сундук");
        put("ENDER_CHEST", "Эндер-сундук");
        put("SHULKER_BOX", "Шалкеровый ящик");

        // Ores & Minerals
        put("COAL", "Уголь");
        put("CHARCOAL", "Древесный уголь");
        put("IRON_INGOT", "Железный слиток");
        put("GOLD_INGOT", "Золотой слиток");
        put("DIAMOND", "Алмаз");
        put("EMERALD", "Изумруд");
        put("NETHERITE_INGOT", "Незеритовый слиток");
        put("NETHERITE_SCRAP", "Незеритовый лом");
        put("AMETHYST_SHARD", "Осколок аметиста");
        put("COPPER_INGOT", "Медный слиток");
        put("LAPIS_LAZULI", "Лазурит");
        put("REDSTONE", "Редстоун");
        put("QUARTZ", "Кварц");

        // Ore Blocks
        put("COAL_BLOCK", "Угольный блок");
        put("IRON_BLOCK", "Железный блок");
        put("GOLD_BLOCK", "Золотой блок");
        put("DIAMOND_BLOCK", "Алмазный блок");
        put("EMERALD_BLOCK", "Изумрудный блок");
        put("NETHERITE_BLOCK", "Незеритовый блок");
        put("REDSTONE_BLOCK", "Редстоун блок");
        put("LAPIS_BLOCK", "Лазуритовый блок");

        // Armor & Weapons - Netherite
        put("NETHERITE_SWORD", "Незеритовый меч");
        put("NETHERITE_PICKAXE", "Незеритовая кирка");
        put("NETHERITE_AXE", "Незеритовый топор");
        put("NETHERITE_SHOVEL", "Незеритовая лопата");
        put("NETHERITE_HOE", "Незеритовая мотыга");
        put("NETHERITE_HELMET", "Незеритовый шлем");
        put("NETHERITE_CHESTPLATE", "Незеритовый нагрудник");
        put("NETHERITE_LEGGINGS", "Незеритовые поножи");
        put("NETHERITE_BOOTS", "Незеритовые ботинки");

        // Armor & Weapons - Diamond
        put("DIAMOND_SWORD", "Алмазный меч");
        put("DIAMOND_PICKAXE", "Алмазная кирка");
        put("DIAMOND_AXE", "Алмазный топор");
        put("DIAMOND_SHOVEL", "Алмазная лопата");
        put("DIAMOND_HOE", "Алмазная мотыга");
        put("DIAMOND_HELMET", "Алмазный шлем");
        put("DIAMOND_CHESTPLATE", "Алмазный нагрудник");
        put("DIAMOND_LEGGINGS", "Алмазные поножи");
        put("DIAMOND_BOOTS", "Алмазные ботинки");

        // Armor & Weapons - Iron / Gold / Bows
        put("IRON_SWORD", "Железный меч");
        put("IRON_PICKAXE", "Железная кирка");
        put("IRON_AXE", "Железный топор");
        put("IRON_HELMET", "Железный шлем");
        put("IRON_CHESTPLATE", "Железный нагрудник");
        put("IRON_LEGGINGS", "Железные поножи");
        put("IRON_BOOTS", "Железные ботинки");
        put("GOLDEN_SWORD", "Золотой меч");
        put("GOLDEN_APPLE", "Золотое яблоко");
        put("ENCHANTED_GOLDEN_APPLE", "Зачарованное золотое яблоко");
        put("BOW", "Лук");
        put("CROSSBOW", "Арбалет");
        put("TRIDENT", "Трезубец");
        put("ARROW", "Стрела");
        put("SPECTRAL_ARROW", "Спектральная стрела");
        put("SHIELD", "Щит");
        put("ELYTRA", "Элитры");
        put("TOTEM_OF_UNDYING", "Тотем бессмертия");

        // Food & Consumables
        put("APPLE", "Яблоко");
        put("BREAD", "Хлеб");
        put("COOKED_BEEF", "Стейк");
        put("COOKED_PORKCHOP", "Жареная свинина");
        put("GOLDEN_CARROT", "Золотая морковь");
        put("EXPERIENCE_BOTTLE", "Пузырек опыта");
        put("ENDER_PEARL", "Жемчуг Эндера");
        put("ENDER_EYE", "Око Эндера");
        put("FIREWORK_ROCKET", "Фейерверк");
        put("POTION", "Зелье");
        put("SPLASH_POTION", "Взрывное зелье");
        put("LINGERING_POTION", "Оседающее зелье");
        put("ENCHANTED_BOOK", "Зачарованная книга");
    }

    private static void put(String materialName, String name) {
        Material material = Material.matchMaterial(materialName);
        if (material != null) {
            RUSSIAN_NAMES.put(material, name);
        }
    }

    private ItemUtil() {}

    public static String getDisplayName(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return "";
        }
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return WellSun.color(meta.getDisplayName());
            }
        }
        String ruName = RUSSIAN_NAMES.get(stack.getType());
        if (ruName != null) {
            return WellSun.color("&f" + ruName);
        }
        String formatted = stack.getType().name().toLowerCase().replace('_', ' ');
        return WellSun.color("&f" + Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1));
    }
}
