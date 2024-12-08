package dev.appeazethecheese.votepoints.commands;

import dev.appeazethecheese.votepoints.VotePoints;
import dev.appeazethecheese.votepoints.VpPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.IntStream;

public class VotePointsCommand implements CommandExecutor, TabCompleter {

    private VpPlayerManager vpManager;

    public VotePointsCommand(){
        vpManager = Bukkit.getServicesManager().load(VpPlayerManager.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)){
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return true;
        }

        var isAdmin = player.hasPermission("votepoints.admin");

        if(args.length == 0 || args[0].equalsIgnoreCase("bal")){
            if(isAdmin && args.length >= 2){
                var targetPlayer = Bukkit.getOfflinePlayer(args[1]);
                if(targetPlayer.getUniqueId().version() == 3){
                    player.sendMessage(ChatColor.RED + "No account with the username " + args[1] + " could be found.");
                }
                else {
                    var points = vpManager.getPoints(targetPlayer.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GREEN + " has " + ChatColor.RED + points + ChatColor.GREEN + " vote points.");
                }
            }
            else {
                var points = vpManager.getPoints(player);
                player.sendMessage(ChatColor.GREEN + "You have " + ChatColor.RED + String.valueOf(points) + ChatColor.GREEN + " vote points.");
            }
        }
        else if(args[0].equalsIgnoreCase("help")){
            player.sendMessage(getHelpText(label, isAdmin));
        }
        else if(args[0].equalsIgnoreCase("pay")){
            if(args.length < 3){
                player.sendMessage(getHelpText(label, isAdmin));
                return true;
            }

            var force = args.length >= 4 && args[3].equalsIgnoreCase("force");
            var targetPlayer = force ? Bukkit.getOfflinePlayer(args[1]) : Bukkit.getPlayer(args[1]);

            if(targetPlayer == null || targetPlayer.getUniqueId().version() == 3){
                if(force){
                    player.sendMessage(ChatColor.RED + "No player with that username exists.");
                }
                else{
                    player.sendMessage(ChatColor.RED + "No player with that username is currently online. If you want to pay them anyway, you can use " + ChatColor.YELLOW + "/votepoints pay " + args[1] + " " + args[2] + " force" + ChatColor.RED + ".");
                }
            }
            else{
                int pointAmount;
                try{
                    pointAmount = Integer.parseInt(args[2]);
                } catch (Exception e){
                    player.sendMessage(ChatColor.RED + args[2] + " is not a valid point amount.");
                    return true;
                }

                if(pointAmount <= 0){
                    player.sendMessage(ChatColor.RED + "You must enter a point amount of 1 or more.");
                    return true;
                }

                vpManager.removePoints(player, pointAmount);
                vpManager.addPoints(targetPlayer.getUniqueId(), pointAmount);

                player.sendMessage(ChatColor.GREEN + "You paid " + ChatColor.YELLOW + targetPlayer.getName() + " " + ChatColor.RED + pointAmount + ChatColor.GREEN + " vote points.");
                if(targetPlayer.isOnline()){
                    targetPlayer.getPlayer().sendMessage(ChatColor.GREEN + "You have received " + ChatColor.RED + pointAmount + ChatColor.GREEN + " vote points from " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + "!");
                }
            }
        }
        else if(isAdmin && args[0].equalsIgnoreCase("add")){
            var force = args.length >= 4 && args[3].equalsIgnoreCase("force");
            var targetPlayer = force ? Bukkit.getOfflinePlayer(args[1]) : Bukkit.getPlayer(args[1]);

            if(targetPlayer == null || targetPlayer.getUniqueId().version() == 3){
                if(force){
                    player.sendMessage(ChatColor.RED + "No player with that username exists.");
                }
                else{
                    player.sendMessage(ChatColor.RED + "No player with that username is currently online. If you want to add points anyway, you can use " + ChatColor.YELLOW + "/votepoints pay " + args[1] + " " + args[2] + " force" + ChatColor.RED + ".");
                }
            }
            else {
                int pointAmount;
                try {
                    pointAmount = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + args[2] + " is not a valid point amount.");
                    return true;
                }

                if (pointAmount <= 0) {
                    player.sendMessage(ChatColor.RED + "You must enter a point amount of 1 or more.");
                    return true;
                }

                vpManager.addPoints(targetPlayer.getUniqueId(), pointAmount);

                player.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.RED + pointAmount + ChatColor.GREEN + " vote points to " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GREEN + ".");
                if (targetPlayer.isOnline()) {
                    targetPlayer.getPlayer().sendMessage(ChatColor.GREEN + "An admin has given you " + ChatColor.RED + pointAmount + ChatColor.GREEN + " vote points!");
                }
            }
        }
        else if(isAdmin && args[0].equalsIgnoreCase("remove")){
            var force = args.length >= 4 && args[3].equalsIgnoreCase("force");
            var targetPlayer = force ? Bukkit.getOfflinePlayer(args[1]) : Bukkit.getPlayer(args[1]);

            if(targetPlayer == null || targetPlayer.getUniqueId().version() == 3){
                if(force){
                    player.sendMessage(ChatColor.RED + "No player with that username exists.");
                }
                else{
                    player.sendMessage(ChatColor.RED + "No player with that username is currently online. If you want to add points anyway, you can use " + ChatColor.YELLOW + "/votepoints pay " + args[1] + " " + args[2] + " force" + ChatColor.RED + ".");
                }
            }
            else {
                int pointAmount;
                try {
                    pointAmount = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + args[2] + " is not a valid point amount.");
                    return true;
                }

                if (pointAmount <= 0) {
                    player.sendMessage(ChatColor.RED + "You must enter a point amount of 1 or more.");
                    return true;
                }

                var success = vpManager.removePoints(targetPlayer.getUniqueId(), pointAmount);
                if(!success){
                    player.sendMessage(ChatColor.RED + "They don't have that many points.");
                    return true;
                }

                player.sendMessage(ChatColor.GREEN + "Took " + ChatColor.RED + pointAmount + ChatColor.GREEN + " vote points from " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GREEN + ".");
                if (targetPlayer.isOnline()) {
                    targetPlayer.getPlayer().sendMessage(ChatColor.GREEN + "An admin has taken " + ChatColor.RED + pointAmount + ChatColor.GREEN + " vote points from you.");
                }
            }
        }
        else if (isAdmin && args[0].equalsIgnoreCase("set")) {
            var force = args.length >= 4 && args[3].equalsIgnoreCase("force");
            var targetPlayer = force ? Bukkit.getOfflinePlayer(args[1]) : Bukkit.getPlayer(args[1]);

            if(targetPlayer == null || targetPlayer.getUniqueId().version() == 3){
                if(force){
                    player.sendMessage(ChatColor.RED + "No player with that username exists.");
                }
                else{
                    player.sendMessage(ChatColor.RED + "No player with that username is currently online. If you want to add points anyway, you can use " + ChatColor.YELLOW + "/votepoints pay " + args[1] + " " + args[2] + " force" + ChatColor.RED + ".");
                }
            }
            else {
                int pointAmount;
                try {
                    pointAmount = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + args[2] + " is not a valid point amount.");
                    return true;
                }

                vpManager.setPoints(targetPlayer.getUniqueId(), pointAmount);

                player.sendMessage(ChatColor.GREEN + "Set " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GREEN + "'s vote points to " + ChatColor.RED + pointAmount + ChatColor.GREEN + ".");
                if (targetPlayer.isOnline()) {
                    targetPlayer.getPlayer().sendMessage(ChatColor.GREEN + "An admin has set your vote points to " + ChatColor.RED + pointAmount + ChatColor.GREEN + ".");
                }
            }
        }
        else{
            player.sendMessage(getHelpText(label, isAdmin));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return null;
        var isAdmin = player.hasPermission("votepoints.admin");

        if(args.length == 1){
            var list = new ArrayList<>(Arrays.asList("pay", "bal", "help"));
            if(isAdmin){
                list.addAll(Arrays.asList("add", "remove", "set"));
            }
            return list;
        }
        if(args.length == 2){
            if(args[0].equalsIgnoreCase("pay")
                || (isAdmin
                    && (args[0].equalsIgnoreCase("bal")
                        || args[0].equalsIgnoreCase("add")
                        || args[0].equalsIgnoreCase("remove")
                        || args[0].equalsIgnoreCase("set")))){
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        if(args.length == 3){
            if(args[0].equalsIgnoreCase("pay")) {
                var points = vpManager.getPoints(player);
                if (points > 0) {
                    return IntStream.range(1, points + 1).mapToObj(String::valueOf).toList();
                }
            }
        }
        if(args.length == 4){
            if(args[0].equalsIgnoreCase("pay")
                || (isAdmin
                    && (args[0].equalsIgnoreCase("add")
                        || args[0].equalsIgnoreCase("remove")
                        || args[0].equalsIgnoreCase("set"))))
                return List.of("force");
        }

        return new ArrayList<>();
    }

    private String getHelpText(String label, boolean isAdmin){
        var usage = "Usage:\n";

        if(isAdmin){
            usage +=
                    ChatColor.YELLOW + "/%1$s bal [{playerName}]: " + ChatColor.RESET + "See how many vote points you or another player has.\n" +
                    ChatColor.YELLOW + "/%1$s add {playerName} {amount} [force]: " + ChatColor.RESET + "Add some points to a player.\n" +
                    ChatColor.YELLOW + "/%1$s remove {playerName} {amount} [force]: " + ChatColor.RESET + "Remove some points from a player.\n" +
                    ChatColor.YELLOW + "/%1$s set {playerName} {amount} [force]: " + ChatColor.RESET + "Set how many vote points a player has.\n";
        }
        else {
            usage += ChatColor.YELLOW + "/%1$s bal: " + ChatColor.RESET + "See how many vote points you have.\n";
        }
        usage +=
                ChatColor.YELLOW + "/%1$s pay {playerName} {amount} [force]: " + ChatColor.RESET + "Send some points to another player.\n" +
                ChatColor.YELLOW + "/%1$s help: " + ChatColor.RESET + "Display this help menu";
        return String.format(usage, label);
    }
}
