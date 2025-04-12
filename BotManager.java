package com.minecraft.bots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class BotManager {
    private final MinecraftBots plugin;
    private final Logger logger;
    private final Map<UUID, FakePlayer> activeBots;
    private final Map<UUID, BukkitTask> botTasks;

    public BotManager(MinecraftBots plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.activeBots = new HashMap<>();
        this.botTasks = new HashMap<>();
    }

    /**
     * Spawn multiple bots at a location
     * @param count Number of bots to spawn
     * @param location Location to spawn bots
     * @param botNames List of bot names to use
     * @return Number of bots successfully spawned
     */
    public int spawnBots(int count, Location location, List<String> botNames) {
        int spawned = 0;
        World world = location.getWorld();
        
        if (world == null) {
            logger.warning("Failed to spawn bots: Invalid world");
            return 0;
        }

        for (int i = 0; i < count; i++) {
            // Choose a randomized location within 10 blocks
            double offsetX = (Math.random() * 20) - 10;
            double offsetZ = (Math.random() * 20) - 10;
            
            Location spawnLoc = location.clone().add(offsetX, 0, offsetZ);
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);
            
            // Get a name for this bot
            String botName = botNames.get(i % botNames.size());
            
            // Try to spawn the bot
            FakePlayer bot = spawnBot(botName, spawnLoc);
            if (bot != null) {
                spawned++;
            }
        }
        
        return spawned;
    }
    
    /**
     * Spawn a single bot
     * @param name Bot's name
     * @param location Location to spawn the bot
     * @return The spawned bot or null if failed
     */
    public FakePlayer spawnBot(String name, Location location) {
        try {
            FakePlayer bot = new FakePlayer(plugin, name, location);
            UUID botId = bot.getUUID();
            
            activeBots.put(botId, bot);
            
            // Schedule anti-AFK tasks
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!bot.isActive()) {
                        // Bot is no longer active, cancel this task
                        this.cancel();
                        botTasks.remove(botId);
                        return;
                    }
                    
                    // Perform random movement actions
                    performAntiAFK(bot);
                }
            }.runTaskTimer(plugin, 100L, 200L); // Run every 10 seconds (200 ticks)
            
            botTasks.put(botId, task);
            logger.info("Spawned bot: " + name);
            
            return bot;
        } catch (Exception e) {
            logger.warning("Failed to spawn bot: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Make the bot perform anti-AFK actions
     * @param bot The bot to perform actions
     */
    private void performAntiAFK(FakePlayer bot) {
        int action = (int) (Math.random() * 6);
        
        switch (action) {
            case 0:
                // Look around randomly
                bot.lookRandom();
                break;
            case 1:
                // Walk in a random direction
                bot.startWalking();
                // Schedule stopping after 2-5 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        bot.stopWalking();
                    }
                }.runTaskLater(plugin, (long) (40 + Math.random() * 60)); // 2-5 seconds (40-100 ticks)
                break;
            case 2:
                // Jump
                bot.jump();
                break;
            case 3:
                // Sneak
                bot.sneak(true);
                // Stop sneaking after 1-2 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        bot.sneak(false);
                    }
                }.runTaskLater(plugin, (long) (20 + Math.random() * 20)); // 1-2 seconds (20-40 ticks)
                break;
            case 4:
                // Swing arm
                bot.swingArm();
                break;
            case 5:
                // Look at a nearby player if available
                bot.lookAtNearestPlayer();
                break;
        }
    }
    
    /**
     * Remove all bots
     * @return Number of bots removed
     */
    public int removeAllBots() {
        int count = activeBots.size();
        
        // Cancel all scheduled tasks
        for (BukkitTask task : botTasks.values()) {
            task.cancel();
        }
        botTasks.clear();
        
        // Remove all bots
        for (FakePlayer bot : activeBots.values()) {
            bot.remove();
        }
        activeBots.clear();
        
        logger.info("Removed " + count + " bots");
        return count;
    }
    
    /**
     * Remove a specific bot
     * @param botId UUID of the bot to remove
     * @return true if removed, false if not found
     */
    public boolean removeBot(UUID botId) {
        FakePlayer bot = activeBots.remove(botId);
        if (bot != null) {
            // Cancel the task
            BukkitTask task = botTasks.remove(botId);
            if (task != null) {
                task.cancel();
            }
            
            // Remove the bot
            bot.remove();
            logger.info("Removed bot: " + bot.getName());
            return true;
        }
        return false;
    }
}
