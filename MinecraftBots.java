package com.minecraft.bots;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MinecraftBots extends JavaPlugin {
    private BotManager botManager;
    private Logger logger;
    private List<String> botNames;

    @Override
    public void onEnable() {
        this.logger = getLogger();
        this.botManager = new BotManager(this);
        this.botNames = generateBotNames();
        
        // Register commands
        getCommand("spawnbots").setExecutor(this);
        getCommand("killbots").setExecutor(this);
        
        logger.info("MinecraftBots plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean up all bots when plugin is disabled
        if (botManager != null) {
            botManager.removeAllBots();
        }
        logger.info("MinecraftBots plugin has been disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("spawnbots")) {
            int count = 15; // Default to 15 bots

            if (args.length > 0) {
                try {
                    count = Integer.parseInt(args[0]);
                    if (count <= 0) {
                        player.sendMessage("§cNumber of bots must be a positive integer.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid number format. Usage: /spawnbots [number]");
                    return true;
                }
            }

            // Limit max bots to 50 to prevent server overload
            if (count > 50) {
                player.sendMessage("§cMaximum number of bots is limited to 50 to prevent server lag.");
                count = 50;
            }

            int spawned = botManager.spawnBots(count, player.getLocation(), botNames);
            player.sendMessage("§aSpawned " + spawned + " bots around your location!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("killbots")) {
            int removed = botManager.removeAllBots();
            player.sendMessage("§aRemoved " + removed + " bots from the server.");
            return true;
        }

        return false;
    }

    // Generate realistic bot names
    private List<String> generateBotNames() {
        List<String> names = new ArrayList<>();
        
        // Common name prefixes
        String[] prefixes = {"Alex", "Steve", "Emma", "Sarah", "Mike", "John", "Lisa", "David", "Anna", "James"};
        
        // Generate at least 50 names
        for (int i = 0; i < 50; i++) {
            String prefix = prefixes[i % prefixes.length];
            int number = 10 + (int)(Math.random() * 90); // Random number between 10-99
            names.add(prefix + number);
        }
        
        return names;
    }
}
