package ru.amiloxs.beesun.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.amiloxs.beesun.BeeSun;
import ru.amiloxs.beesun.config.PluginFiles;
import ru.amiloxs.beesun.model.HologramLine;
import ru.amiloxs.beesun.util.ItemUtil;

public final class BeeService implements Listener {
    private final BeeSun plugin;
    private final PluginFiles files;
    private final List<ArmorStand> hologramStands = new ArrayList<>();
    private final List<HologramLine> loadedLines = new ArrayList<>();
    private final Map<UUID, Long> droppedItems = new HashMap<>();
    private final Set<UUID> playersInRadius = new HashSet<>();
    private Location center;
    private long tick;
    private long nextReward;
    private long lastAmbient;
    private int taskId = -1;

    public BeeService(BeeSun plugin, PluginFiles files) {
        this.plugin = plugin;
        this.files = files;
    }

    public void reload() {
        stop();
        World world = Bukkit.getWorld(files.block().getString("anchor.world", "lobby"));
        if (world == null) {
            world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        }
        if (world == null) {
            plugin.getLogger().warning("Мир для якоря не найден.");
            return;
        }

        double x = files.block().getDouble("anchor.x", 0.0);
        double y = files.block().getDouble("anchor.y", 64.0);
        double z = files.block().getDouble("anchor.z", 0.0);
        center = new Location(world, x, y, z);

        String blockType = files.block().getString("anchor.block", "RESPAWN_ANCHOR");
        Material material = Material.matchMaterial(blockType);
        if (material != null) {
            center.getBlock().setType(material, false);
        }
        center = center.getBlock().getLocation().add(0.5, 0.0, 0.5);

        loadHologramLines();

        tick = 0;
        nextReward = chargeTicks();
        lastAmbient = -ambientTicks();
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L).getTaskId();
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        taskId = -1;
        clearHologram();
        for (UUID uuid : droppedItems.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
        droppedItems.clear();
        playersInRadius.clear();

        // Убираем блок якоря при выключении плагина
        if (center != null && center.getWorld() != null) {
            center.getBlock().setType(Material.AIR, false);
        }
    }

    public void close() {
        stop();
    }

    private void loadHologramLines() {
        loadedLines.clear();
        if (!files.block().getBoolean("hologram.enabled", true)) {
            return;
        }

        List<?> list = files.block().getList("hologram.lines");
        if (list == null) {
            return;
        }

        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                String content = map.containsKey("content") ? String.valueOf(map.get("content")) : "";
                double height = map.containsKey("height") ? parseDouble(map.get("height"), 0.28) : 0.28;
                double offsetX = map.containsKey("offsetX") ? parseDouble(map.get("offsetX"), 0.0) : 0.0;
                double offsetZ = map.containsKey("offsetZ") ? parseDouble(map.get("offsetZ"), 0.0) : 0.0;
                loadedLines.add(new HologramLine(content, height, offsetX, offsetZ));
            } else if (item instanceof String str) {
                loadedLines.add(new HologramLine(str, 0.28, 0.0, 0.0));
            }
        }
    }

    private double parseDouble(Object val, double def) {
        if (val instanceof Number num) {
            return num.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(val));
        } catch (Exception e) {
            return def;
        }
    }

    private void tick() {
        if (center == null || center.getWorld() == null) return;
        tick++;
        cleanupDroppedItems();
        updateAnchorCharge();

        List<Player> players = nearbyPlayers();
        checkPlayerRadiusTransitions(players);

        if (players.isEmpty()) {
            clearHologram();
        } else {
            drawCircle();
            updateHologram();
            playAmbient();
        }

        drawItemTrails();

        if (tick >= nextReward) {
            nextReward = tick + chargeTicks();
            if (!players.isEmpty()) {
                reward(players);
                resetAnchorCharge();
            }
        }
    }

    private List<Player> nearbyPlayers() {
        double radius = plugin.getConfig().getDouble("anchor.player-radius", 12.0);
        double squared = radius * radius;
        List<Player> result = new ArrayList<>();
        for (Player player : center.getWorld().getPlayers()) {
            if (!player.isDead() && player.getLocation().distanceSquared(center) <= squared) {
                result.add(player);
            }
        }
        return result;
    }

    private void checkPlayerRadiusTransitions(List<Player> currentPlayers) {
        Set<UUID> currentUuids = new HashSet<>();
        double radius = plugin.getConfig().getDouble("anchor.player-radius", 12.0);
        String radiusStr = (radius == (long) radius) ? String.valueOf((long) radius) : String.valueOf(radius);
        String enterMsg = files.message("enter-radius");

        for (Player player : currentPlayers) {
            UUID uuid = player.getUniqueId();
            currentUuids.add(uuid);
            if (playersInRadius.add(uuid)) {
                // Игрок только что вошел в радиус
                if (enterMsg != null && !enterMsg.isEmpty()) {
                    String formatted = enterMsg
                            .replace("%block_radius%", radiusStr)
                            .replace("%reward_mode%", rewardLabel())
                            .replace("%player_name%", player.getName());
                    player.sendMessage(BeeSun.color(formatted));
                }
            }
        }

        // Удаляем игроков, которые вышли из радиуса
        playersInRadius.removeIf(uuid -> !currentUuids.contains(uuid));
    }

    private void drawCircle() {
        if (!files.particle().getBoolean("effects.circle.enabled", true) || tick % 2 != 0) return;

        int points = Math.max(8, files.particle().getInt("effects.circle.points", 48));
        double radius = Math.max(0.5, files.particle().getDouble("effects.circle.radius", 5.0));
        double y = files.particle().getDouble("effects.circle.y-offset", 0.12);
        float size = (float) files.particle().getDouble("effects.circle.size", 1.5);
        int r = files.particle().getInt("effects.circle.color.r", 170);
        int g = files.particle().getInt("effects.circle.color.g", 0);
        int b = files.particle().getInt("effects.circle.color.b", 255);

        String particleName = files.particle().getString("effects.circle.particle", "DUST");
        Particle particle = parseParticle(particleName);
        Particle.DustOptions dust = (particle != null && particle.getDataType() == Particle.DustOptions.class) ? new Particle.DustOptions(Color.fromRGB(r, g, b), size) : null;

        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            Location location = new Location(
                    center.getWorld(),
                    center.getX() + Math.cos(angle) * radius,
                    center.getY() + y,
                    center.getZ() + Math.sin(angle) * radius
            );
            for (Player player : nearbyPlayers()) {
                if (dust != null) {
                    player.spawnParticle(particle, location, 1, 0, 0, 0, dust);
                } else {
                    player.spawnParticle(particle, location, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private void playAmbient() {
        if (!files.particle().getBoolean("sounds.ambient.enabled", true) || tick - lastAmbient < ambientTicks()) {
            return;
        }
        lastAmbient = tick;
        playSound("sounds.ambient");
    }

    private void updateHologram() {
        if (!files.block().getBoolean("hologram.enabled", true) || loadedLines.isEmpty()) {
            return;
        }

        if (hologramStands.size() != loadedLines.size()) {
            clearHologram();
            double baseHeight = files.block().getDouble("hologram.base-height", 2.8);
            double currentY = baseHeight;

            for (int i = 0; i < loadedLines.size(); i++) {
                HologramLine line = loadedLines.get(i);
                if (i > 0) {
                    currentY -= line.getHeight();
                }
                Location standLoc = center.clone().add(line.getOffsetX(), currentY, line.getOffsetZ());
                ArmorStand stand = center.getWorld().spawn(standLoc, ArmorStand.class);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setMarker(true);
                stand.setInvulnerable(true);
                stand.setCustomNameVisible(true);
                hologramStands.add(stand);
            }
        }

        String mode = rewardLabel();
        String seconds = String.valueOf(Math.max(0, (nextReward - tick + 19) / 20));

        for (int i = 0; i < loadedLines.size(); i++) {
            String text = loadedLines.get(i).getContent()
                    .replace("%time_prize%", seconds)
                    .replace("%reward_mode%", mode);
            hologramStands.get(i).setCustomName(BeeSun.color(text));
        }
    }

    private void clearHologram() {
        for (ArmorStand stand : hologramStands) {
            if (!stand.isDead()) {
                stand.remove();
            }
        }
        hologramStands.clear();
    }

    private void reward(List<Player> players) {
        String mode = rewardMode();
        if ("EXPERIENCE".equals(mode) || "EXPERIENCE_AND_COINS".equals(mode)) {
            int amount = Math.max(0, plugin.getConfig().getInt("anchor.experience", 150));
            for (Player player : players) {
                player.giveExp(amount);
            }
        }
        if ("COINS".equals(mode) || "EXPERIENCE_AND_COINS".equals(mode)) {
            String cmd = plugin.getConfig().getString("anchor.coins-command", "eco give %player_name% 150");
            for (Player player : players) {
                runCommand(cmd, player);
            }
        }

        // Выпадение предметов из редактора
        if (plugin.getConfig().getBoolean("anchor.item-drop.enabled", true)) {
            int minCount = Math.max(1, plugin.getConfig().getInt("anchor.item-drop.min-count", 1));
            int maxCount = Math.max(minCount, plugin.getConfig().getInt("anchor.item-drop.max-count", 3));
            List<ItemStack> randomItems = plugin.getItemStorage().getRandomRewardItems(minCount, maxCount);

            double launchHeight = plugin.getConfig().getDouble("anchor.item-launch-height", 0.8);
            double minDist = plugin.getConfig().getDouble("anchor.item-drop.distance-min-blocks", 1.0);
            double maxDist = plugin.getConfig().getDouble("anchor.item-drop.distance-max-blocks", 5.0);
            boolean showDisplayName = plugin.getConfig().getBoolean("anchor.item-drop.show-display-name", true);

            for (ItemStack stack : randomItems) {
                if (stack == null || stack.getType() == Material.AIR) continue;
                Location dropLoc = center.clone().add(0, launchHeight, 0);
                Item dropped = center.getWorld().dropItem(dropLoc, stack);

                // Расчет скорости разлета на заданный диапазон блоков
                double targetDist = ThreadLocalRandom.current().nextDouble(minDist, maxDist);
                double horizontalSpeed = targetDist / 12.5;
                double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                double vx = Math.cos(angle) * horizontalSpeed;
                double vz = Math.sin(angle) * horizontalSpeed;
                double vy = ThreadLocalRandom.current().nextDouble(0.35, 0.45);
                dropped.setVelocity(new Vector(vx, vy, vz));

                // Отображение цветного названия предмета при включенной настройке
                if (showDisplayName) {
                    String displayName = ItemUtil.getDisplayName(stack);
                    dropped.setCustomName(displayName);
                    dropped.setCustomNameVisible(true);
                } else {
                    dropped.setCustomNameVisible(false);
                }

                // Регистрируем ТОЛЬКО предмет, выпавший из якоря
                droppedItems.put(dropped.getUniqueId(), tick);
            }
        }

        playSound("sounds.drop");
    }

    private void updateAnchorCharge() {
        if (!(center.getBlock().getBlockData() instanceof RespawnAnchor anchor)) return;
        long total = chargeTicks();
        long cycleProgress = total - Math.max(0, nextReward - tick);
        int charges = (int) Math.min(4, Math.max(0, (cycleProgress * 4L) / total));
        if (anchor.getCharges() != charges) {
            anchor.setCharges(charges);
            center.getBlock().setBlockData(anchor, false);
        }
    }

    private void resetAnchorCharge() {
        if (!(center.getBlock().getBlockData() instanceof RespawnAnchor anchor)) return;
        anchor.setCharges(0);
        center.getBlock().setBlockData(anchor, false);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        // Удаляем подобранный предмет якоря из списка отслеживания
        droppedItems.remove(event.getItem().getUniqueId());
    }

    private void drawItemTrails() {
        boolean showParticles = plugin.getConfig().getBoolean("anchor.item-drop.show-particles", true)
                && files.particle().getBoolean("effects.item-trail.enabled", true);

        if (!showParticles || droppedItems.isEmpty()) return;

        long visible = Math.max(1, Math.round(files.particle().getDouble("effects.item-trail.visible-seconds", 1.5) * 20.0));
        long hidden = Math.max(1, Math.round(files.particle().getDouble("effects.item-trail.hidden-seconds", 3.0) * 20.0));
        boolean visibleNow = tick % (visible + hidden) < visible;
        if (!visibleNow) return;

        int count = Math.max(1, files.particle().getInt("effects.item-trail.count", 3));
        float size = (float) files.particle().getDouble("effects.item-trail.size", 1.5);
        int r = files.particle().getInt("effects.item-trail.color.r", 170);
        int g = files.particle().getInt("effects.item-trail.color.g", 0);
        int b = files.particle().getInt("effects.item-trail.color.b", 255);

        String particleName = files.particle().getString("effects.item-trail.particle", "DUST");
        Particle particle = parseParticle(particleName);
        Particle.DustOptions dust = (particle != null && particle.getDataType() == Particle.DustOptions.class) ? new Particle.DustOptions(Color.fromRGB(r, g, b), size) : null;

        for (UUID uuid : new ArrayList<>(droppedItems.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof Item item && item.isValid() && !item.isDead()) {
                Location location = entity.getLocation().add(0, 0.1, 0);
                for (Player player : nearbyPlayers()) {
                    if (dust != null) {
                        player.spawnParticle(particle, location, count, 0.06, 0.06, 0.06, dust);
                    } else {
                        player.spawnParticle(particle, location, count, 0.06, 0.06, 0.06, 0);
                    }
                }
            }
        }
    }

    private void cleanupDroppedItems() {
        long lifetime = Math.max(1, plugin.getConfig().getLong("anchor.item-lifetime-seconds", 90)) * 20L;
        for (UUID uuid : new ArrayList<>(droppedItems.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);
            Long created = droppedItems.get(uuid);
            if (entity == null || entity.isDead() || !entity.isValid() || created == null || tick - created >= lifetime) {
                if (entity != null && entity.isValid() && !entity.isDead()) {
                    entity.remove();
                }
                droppedItems.remove(uuid);
            }
        }
    }

    private void runCommand(String command, Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player_name%", player.getName()));
    }

    private String rewardMode() {
        if (!plugin.getConfig().getBoolean("anchor.cycle.enabled", false)) {
            return plugin.getConfig().getString("anchor.reward-mode", "EXPERIENCE_AND_COINS").toUpperCase();
        }
        long switchTicks = Math.max(1, plugin.getConfig().getLong("anchor.cycle.switch-seconds", 300)) * 20L;
        long cycle = (tick / switchTicks) % 2;
        return plugin.getConfig().getString(cycle == 0 ? "anchor.cycle.first-mode" : "anchor.cycle.second-mode", "EXPERIENCE").toUpperCase();
    }

    private String rewardLabel() {
        String mode = rewardMode();
        if ("EXPERIENCE".equals(mode)) {
            return files.message("reward-modes.experience");
        }
        if ("COINS".equals(mode)) {
            return files.message("reward-modes.coins");
        }
        if ("EXPERIENCE_AND_COINS".equals(mode)) {
            return files.message("reward-modes.experience-and-coins");
        }
        if ("ITEMS".equals(mode)) {
            return files.message("reward-modes.items");
        }
        return files.message("reward-modes.experience-and-coins");
    }

    private long chargeTicks() {
        return Math.max(1, plugin.getConfig().getLong("anchor.charge-seconds", 20)) * 20L;
    }

    private long ambientTicks() {
        return Math.max(1, files.particle().getLong("sounds.ambient.interval-seconds", 5)) * 20L;
    }

    private Particle parseParticle(String name) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("DUST") || name.equalsIgnoreCase("REDSTONE")) {
            try {
                return Particle.valueOf("DUST");
            } catch (Exception e) {
                return Particle.valueOf("REDSTONE");
            }
        }
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                return Particle.valueOf("DUST");
            } catch (Exception ex) {
                return Particle.valueOf("REDSTONE");
            }
        }
    }

    private void playSound(String path) {
        if (!files.particle().getBoolean(path + ".enabled", true)) return;
        try {
            String name = files.particle().getString(path + ".sound", "BLOCK_NOTE_BLOCK_CHIME").toUpperCase();
            Sound sound = Sound.valueOf(name);
            float vol = (float) files.particle().getDouble(path + ".volume", 1.0);
            float pitch = (float) files.particle().getDouble(path + ".pitch", 1.0);
            center.getWorld().playSound(center, sound, vol, pitch);
        } catch (IllegalArgumentException ignored) {}
    }
}
