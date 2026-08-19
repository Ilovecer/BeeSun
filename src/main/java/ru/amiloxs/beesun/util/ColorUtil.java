package ru.amiloxs.beesun.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public final class ColorUtil {
    private static final Pattern HEX_TAG_PATTERN = Pattern.compile("(?i)<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_AMP_PATTERN = Pattern.compile("(?i)&#([A-Fa-f0-9]{6})");
    private static final Pattern TAG_PATTERN = Pattern.compile("(?i)<([a-zA-Z_0-9]+)>");

    private static final Map<String, String> NAMED_TAGS = new HashMap<>();

    static {
        NAMED_TAGS.put("black", "§0");
        NAMED_TAGS.put("dark_blue", "§1");
        NAMED_TAGS.put("dark_green", "§2");
        NAMED_TAGS.put("dark_aqua", "§3");
        NAMED_TAGS.put("dark_red", "§4");
        NAMED_TAGS.put("dark_purple", "§5");
        NAMED_TAGS.put("purple", "§5");
        NAMED_TAGS.put("gold", "§6");
        NAMED_TAGS.put("gray", "§7");
        NAMED_TAGS.put("grey", "§7");
        NAMED_TAGS.put("dark_gray", "§8");
        NAMED_TAGS.put("dark_grey", "§8");
        NAMED_TAGS.put("blue", "§9");
        NAMED_TAGS.put("green", "§a");
        NAMED_TAGS.put("aqua", "§b");
        NAMED_TAGS.put("red", "§c");
        NAMED_TAGS.put("light_purple", "§d");
        NAMED_TAGS.put("pink", "§d");
        NAMED_TAGS.put("yellow", "§e");
        NAMED_TAGS.put("white", "§f");
        NAMED_TAGS.put("bold", "§l");
        NAMED_TAGS.put("b", "§l");
        NAMED_TAGS.put("italic", "§o");
        NAMED_TAGS.put("i", "§o");
        NAMED_TAGS.put("underlined", "§n");
        NAMED_TAGS.put("u", "§n");
        NAMED_TAGS.put("strikethrough", "§m");
        NAMED_TAGS.put("st", "§m");
        NAMED_TAGS.put("obfuscated", "§k");
        NAMED_TAGS.put("magic", "§k");
        NAMED_TAGS.put("reset", "§r");
        NAMED_TAGS.put("r", "§r");
    }

    private ColorUtil() {}

    public static String color(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Convert <#RRGGBB> to §x§r§r§g§g§b§b
        Matcher hexTagMatcher = HEX_TAG_PATTERN.matcher(text);
        StringBuffer sb1 = new StringBuffer();
        while (hexTagMatcher.find()) {
            String hex = hexTagMatcher.group(1);
            StringBuilder repl = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                repl.append('§').append(Character.toLowerCase(c));
            }
            hexTagMatcher.appendReplacement(sb1, repl.toString());
        }
        hexTagMatcher.appendTail(sb1);

        // Convert &#RRGGBB to §x§r§r§g§g§b§b
        Matcher hexAmpMatcher = HEX_AMP_PATTERN.matcher(sb1.toString());
        StringBuffer sb2 = new StringBuffer();
        while (hexAmpMatcher.find()) {
            String hex = hexAmpMatcher.group(1);
            StringBuilder repl = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                repl.append('§').append(Character.toLowerCase(c));
            }
            hexAmpMatcher.appendReplacement(sb2, repl.toString());
        }
        hexAmpMatcher.appendTail(sb2);

        // Convert <white>, <gold>, <b>, etc.
        Matcher tagMatcher = TAG_PATTERN.matcher(sb2.toString());
        StringBuffer sb3 = new StringBuffer();
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1).toLowerCase();
            String code = NAMED_TAGS.get(tagName);
            if (code != null) {
                tagMatcher.appendReplacement(sb3, Matcher.quoteReplacement(code));
            } else {
                tagMatcher.appendReplacement(sb3, Matcher.quoteReplacement(tagMatcher.group(0)));
            }
        }
        tagMatcher.appendTail(sb3);

        return ChatColor.translateAlternateColorCodes('&', sb3.toString());
    }
}
