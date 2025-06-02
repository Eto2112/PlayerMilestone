package lbvn.eto2112.playermilestone;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerMilestone extends JavaPlugin implements Listener {
    private FileConfiguration config;
    private Map<String, Long> cooldowns;

    @Override
    public void onEnable() {
        // Save default config if not exists
        saveDefaultConfig();
        config = getConfig();
        cooldowns = new HashMap<>();

        // Check if PlaceholderAPI is present
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("PlaceholderAPI not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register the listener for PlayerJoinEvent
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkMilestones();
    }

    private void checkMilestones() {
        String playerCountPlaceholder = config.getString("player-count_placeholder", "%server_online%");
        String playerCountStr = PlaceholderAPI.setPlaceholders(null, playerCountPlaceholder);
        int playerCount;
        try {
            playerCount = Integer.parseInt(playerCountStr);
        } catch (NumberFormatException e) {
            getLogger().warning("Invalid player count from placeholder: " + playerCountPlaceholder);
            return;
        }

        // Loop through milestones
        for (String key : config.getConfigurationSection("").getKeys(false)) {
            if (!key.startsWith("milestone-")) continue;

            int requirement = config.getInt(key + ".player-requirement", 0);
            String command = config.getString(key + ".command", "");
            long cooldown = config.getLong(key + ".cooldown", 0) * 1000; // Convert to milliseconds

            if (requirement == 0 || command.isEmpty()) continue;

            // Check cooldown
            long currentTime = System.currentTimeMillis();
            if (cooldowns.containsKey(key) && currentTime < cooldowns.get(key)) {
                continue;
            }

            // Check if player count meets requirement
            if (playerCount >= requirement) {
                // Execute command
                getServer().dispatchCommand(getServer().getConsoleSender(), command);
                getLogger().info("Executed command for " + key + ": " + command);
                // Set cooldown
                cooldowns.put(key, currentTime + cooldown);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("playermilestone")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /playermilestone <reload|version>");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("playermilestone.reload")) {
                    sender.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }
                reloadConfig();
                config = getConfig();
                cooldowns.clear(); // Reset cooldowns on reload
                sender.sendMessage("§aConfiguration reloaded successfully!");
                return true;
            }

            if (args[0].equalsIgnoreCase("version")) {
                if (!sender.hasPermission("playermilestone.version")) {
                    sender.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }
                sender.sendMessage("§aPlayerMilestone version: " + getDescription().getVersion());
                return true;
            }

            sender.sendMessage("Usage: /playermilestone <reload|version>");
            return true;
        }
        return false;
    }
}