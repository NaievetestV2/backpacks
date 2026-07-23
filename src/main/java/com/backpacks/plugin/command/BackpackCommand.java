package com.backpacks.plugin.command;

import com.backpacks.plugin.BackpacksPlugin;
import com.backpacks.plugin.backpack.BackpackManager;
import com.backpacks.plugin.backpack.BackpackData;
import com.backpacks.plugin.backpack.BackpackTier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class BackpackCommand implements CommandExecutor, TabCompleter {

    private final BackpackManager manager;

    public BackpackCommand(BackpackManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("backpacks.give")) {
                sender.sendMessage("§cNo permission");
                return true;
            }
            if (args.length < 3) return false;
            Player target = sender.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found");
                return true;
            }
            BackpackTier tier = BackpackTier.fromKey(args[2].toLowerCase());
            if (tier == null) {
                sender.sendMessage("§cInvalid tier");
                return true;
            }
            ItemStack backpack = BackpackData.createItem(tier);
            UUID id = BackpackData.readId(backpack);
            if (id != null) {
                manager.register(new com.backpacks.plugin.backpack.BackpackData(id, tier));
            }
            target.getInventory().addItem(backpack);
            manager.save();
            BackpacksPlugin.unlockRecipes(target);
            sender.sendMessage("§aGave " + capitalize(tier.key()) + " backpack to " + target.getName());
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§aConfig reloaded (stub)");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.stream(BackpackTier.values()).map(t -> t.key()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
