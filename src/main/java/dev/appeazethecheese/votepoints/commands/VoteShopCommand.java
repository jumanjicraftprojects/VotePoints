package dev.appeazethecheese.votepoints.commands;

import dev.appeazethecheese.votepoints.VpPlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.ufactions.customcrates.CustomCrates;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VoteShopCommand implements CommandExecutor, TabCompleter {

    private CustomCrates customCrates;
    private static final String usage =
            ChatColor.GREEN + "Usage: \n" +
                    "  /%1$s health\n" +
                    "  /%1$s perks\n" +
                    "  /%1$s key (crate) [amount]\n" +
                    "  /%1$s pouch (crate) [amount]\n";

    private static final Map<String, Integer> costByCrate = new HashMap<>(){{
        put("End", 2);
        put("Smithing", 6);
    }};

    public VoteShopCommand(CustomCrates customCrates){
        this.customCrates = customCrates;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        var player = (Player) sender;
        var vpManager = Bukkit.getServicesManager().getRegistration(VpPlayerManager.class).getProvider();

        if(args.length == 0){
            sender.sendMessage(String.format(usage, label));
            return true;
        }

        if(args.length == 1){
            switch (args[0]){
                case "key":
                case "pouch":
                    player.sendMessage("Please specify a crate name.");
                    break;
                case "health":
                    player.performCommand("upgradehealth");
                    break;
                case "perks":
                    player.performCommand("permissionshopz");
                    break;
                default:
                    sender.sendMessage(String.format(usage, label));
            }
            return true;
        }

        if(args.length == 2){
            if(!args[0].equals("key") && !args[0].equals("pouch")){
                sender.sendMessage(String.format(usage, label));
                return true;
            }

            var ident = args[1];
            if(!costByCrate.containsKey(ident)){
                sender.sendMessage(ident + " either is not a valid crate or has not yet been added to the shop.");
                return true;
            }


            var message = "You are about to buy 1 " + ident + " crate " + args[0] + " for " + costByCrate.get(ident) + "VP.";
            var commandToRun = "/" + label + " " + args[0] + " " + args[1] + " confirm";
            sendClickableMessage(player, message, commandToRun);
        }

        if(args.length == 3){
            if(!args[0].equals("key") && !args[0].equals("pouch")){
                sender.sendMessage(String.format(usage, label));
                return true;
            }

            var ident = args[1];
            if(!costByCrate.containsKey(ident)){
                sender.sendMessage(ident + " either is not a valid crate or has not yet been added to the shop.");
                return true;
            }

            var amtStr = args[2];
            if(amtStr.equals("confirm")){
                var cost = costByCrate.get(ident);
                var vp = vpManager.getPoints(player);

                if(cost > vp){
                    player.sendMessage("You don't have enough to buy this crate. You need " + (cost - vp) + " more VP.");
                    return true;
                }

                var successful = false;
                switch (args[0]) {
                    case "key" -> successful = giveKey(player, ident, 1);
                    case "pouch" -> successful = givePouch(player, ident, 1);
                    default -> {
                        sender.sendMessage(String.format(usage, label));
                        return true;
                    }
                }

                if(successful){
                    player.sendMessage("You've bought 1 " + ident + " crate " + args[0] + " for " + cost + "VP.");
                    vpManager.removePoints(player, cost);
                }
                return true;
            }

            int amount;
            try{
                amount = Integer.parseInt(amtStr);
            }
            catch (Throwable e){
                player.sendMessage(amtStr + " is not a valid amount.");
                return true;
            }

            var cost = costByCrate.get(ident) * amount;
            var commandToRun = "/" + label + " " + args[0] + " " + args[1] + " " + amount + " confirm";
            var message = "You are about to buy " + amount + " " + ident + " crate " + args[0] + " for " + cost + "VP.";
            sendClickableMessage(player, message, commandToRun);
        }

        if(args.length >= 4){
            if(!args[0].equals("key") && !args[0].equals("pouch")){
                sender.sendMessage(String.format(usage, label));
                return true;
            }

            var ident = args[1];
            if(!costByCrate.containsKey(ident)){
                sender.sendMessage(ident + " either is not a valid crate or has not yet been added to the shop.");
                return true;
            }

            int amount;
            var amtStr = args[2];

            try{
                amount = Integer.parseInt(amtStr);
            }
            catch (Throwable e){
                player.sendMessage(amtStr + " is not a valid amount.");
                return true;
            }

            if(!args[3].equals("confirm")){
                sender.sendMessage(String.format(usage, label));
                return true;
            }

            var cost = costByCrate.get(ident) * amount;
            var vp = vpManager.getPoints(player);
            if(cost > vp){
                player.sendMessage("You don't have enough to buy that many crates. You need " + (cost - vp) + " more VP.");
                return true;
            }

            var successful = false;
            switch (args[0]) {
                case "key" -> successful = giveKey(player, ident, amount);
                case "pouch" -> successful = givePouch(player, ident, amount);
            }

            if(successful){
                vpManager.removePoints(player, cost);
                player.sendMessage("You've bought " + amount + " " + ident + " crate " + args[0] + " for " + cost + "VP.");
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var list = new ArrayList<String>();

        if(args.length == 1){
            list.addAll(Arrays.asList("health", "perks"));

            if(customCrates != null){
                list.addAll(Arrays.asList("key", "pouch"));
            }
        }

        if(args.length == 2){
            if(customCrates != null && (args[0].equals("key") || args[0].equals("pouch"))){
                //var crates = customCrates.getCratesManager().getCrates().stream().map(x -> x.getSettings().getIdentifier()).toList();
                var crates = costByCrate.keySet();
                list.addAll(crates);
            }
        }

        if(args.length == 3){
            if(customCrates != null){
                var numbers = IntStream.range(1, 10).toArray();
                list.addAll(Arrays.stream(numbers).mapToObj(String::valueOf).toList());
            }
        }

        return list;
    }

    private void sendClickableMessage(Player player, String message, String command){

        var msg = new ComponentBuilder(message + "\n")
                .append("[CONFIRM]")
                .color(ChatColor.GREEN)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to run " + ChatColor.YELLOW + ChatColor.BOLD + command)))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

        player.spigot().sendMessage(msg.create());
    }

    private boolean giveKey(Player player, String identifier, int amount){
        var crate = customCrates.getCratesManager().getCrate(identifier);

        if(crate == null){
            player.sendMessage("The " + identifier + " crate doesn't seem to exist. Feel free to blame Donny.");
            return false;
        }

        crate.giveKey(player, amount);
        return true;
    }

    private boolean givePouch(Player player, String identifier, int amount){
        var crate = customCrates.getCratesManager().getCrate(identifier);

        if(crate == null){
            player.sendMessage("The " + identifier + " crate doesn't seem to exist. Feel free to blame Donny.");
            return false;
        }

        crate.givePouch(player, amount);
        return true;
    }
}
