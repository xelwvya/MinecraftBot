package com.minecraft.bots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;
import java.util.UUID;

/**
 * Represents a fake player (bot) in the game.
 * Uses a villager entity as the base since we can't create actual player entities.
 */
public class FakePlayer {
    private final JavaPlugin plugin;
    private final String name;
    private final UUID uuid;
    private final Villager entity;
    private boolean isActive;
    private final Random random;

    /**
     * Create a new fake player
     * @param plugin Plugin instance
     * @param name Player name
     * @param location Spawn location
     */
    public FakePlayer(JavaPlugin plugin, String name, Location location) {
        this.plugin = plugin;
        this.name = name;
        this.uuid = UUID.randomUUID();
        this.isActive = true;
        this.random = new Random();
        
        // Spawn a villager entity to represent the player
        this.entity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        
        // Configure the entity to look like a player
        setupEntity();
    }
    
    /**
     * Configure the entity to look and behave like a player
     */
    private void setupEntity() {
        // Set custom name
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        
        // Make it not despawn
        entity.setPersistent(true);
        
        // Disable AI so we can control it manually
        entity.setAI(false);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        
        // Remove trades
        entity.setRecipes(null);
        
        // Make it look more like a player (holding items)
        switch (random.nextInt(5)) {
            case 0:
                // Sword
                entity.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
                break;
            case 1:
                // Pickaxe
                entity.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_PICKAXE));
                break;
            case 2:
                // Axe
                entity.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_AXE));
                break;
            case 3:
                // Food
                entity.getEquipment().setItemInMainHand(new ItemStack(Material.BREAD, random.nextInt(10) + 1));
                break;
            case 4:
                // Block
                Material[] blocks = {
                    Material.DIRT, Material.STONE, Material.OAK_LOG, Material.OAK_PLANKS, 
                    Material.COBBLESTONE
                };
                entity.getEquipment().setItemInMainHand(
                    new ItemStack(blocks[random.nextInt(blocks.length)], random.nextInt(64) + 1)
                );
                break;
        }
        
        // Random armor (50% chance)
        if (random.nextBoolean()) {
            entity.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
            entity.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        }
    }
    
    /**
     * Make the bot jump
     */
    public void jump() {
        if (!isActive || entity == null || !entity.isValid()) return;
        
        // Apply upward velocity to simulate jumping
        entity.setVelocity(new Vector(0, 0.5, 0));
    }
    
    /**
     * Make the bot look in a random direction
     */
    public void lookRandom() {
        if (!isActive || entity == null || !entity.isValid()) return;
        
        // Calculate a random direction
        float yaw = random.nextFloat() * 360;
        entity.setRotation(yaw, 0);
    }
    
    /**
     * Make the bot look at the nearest player
     */
    public void lookAtNearestPlayer() {
        if (!isActive || entity == null || !entity.isValid()) return;
        
        // Get nearby players
        Collection<Entity> nearbyEntities = entity.getNearbyEntities(20, 20, 20);
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Entity e : nearbyEntities) {
            if (e instanceof Player) {
                double distance = e.getLocation().distanceSquared(entity.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = (Player) e;
                }
            }
        }
        
        if (closest != null) {
            // Make the bot face the player
            Location botLoc = entity.getLocation();
            Location playerLoc = closest.getLocation();
            
            double dx = playerLoc.getX() - botLoc.getX();
            double dz = playerLoc.getZ() - botLoc.getZ();
            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
            
            entity.setRotation(yaw, 0);
        }
    }
    
    /**
     * Make the bot sneak
     * @param sneaking Whether to sneak or not
     */
    public void sneak(boolean sneaking) {
        // Villagers can't actually sneak, but we could change appearance in some way
        // if the server version supports it
    }
    
    /**
     * Make the bot swing arm (attack animation)
     */
    public void swingArm() {
        // Cannot actually swing arms with villagers, but we can move them
        // Implement this if needed using packets or other methods
    }
    
    /**
     * Make the bot walk randomly
     */
    public void startWalking() {
        if (!isActive || entity == null || !entity.isValid()) return;
        
        // Choose a random direction
        float yaw = random.nextFloat() * 360;
        entity.setRotation(yaw, 0);
        
        // Calculate movement direction based on yaw
        double radians = Math.toRadians(yaw);
        double dx = -Math.sin(radians) * 0.2;
        double dz = Math.cos(radians) * 0.2;
        
        // Set velocity to move in that direction
        entity.setVelocity(new Vector(dx, 0, dz));
    }
    
    /**
     * Stop the bot from walking
     */
    public void stopWalking() {
        if (!isActive || entity == null || !entity.isValid()) return;
        
        // Stop movement
        entity.setVelocity(new Vector(0, 0, 0));
    }
    
    /**
     * Remove the bot from the world
     */
    public void remove() {
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
        isActive = false;
    }
    
    /**
     * Check if the bot is still active
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive && entity != null && entity.isValid();
    }
    
    /**
     * Get the bot's UUID
     * @return UUID of the bot
     */
    public UUID getUUID() {
        return uuid;
    }
    
    /**
     * Get the bot's name
     * @return Name of the bot
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the bot's entity
     * @return Entity representing the bot
     */
    public Villager getEntity() {
        return entity;
    }
    
    /**
     * Get the bot's location
     * @return Current location of the bot
     */
    public Location getLocation() {
        return entity != null ? entity.getLocation() : null;
    }
}
