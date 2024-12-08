package dev.appeazethecheese.votepoints;

import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EventHandler implements Listener {
    private VpPlayerManager vpManager;

    private Set<String> pluginCommands = new HashSet<>();

    public EventHandler(){
        vpManager = Bukkit.getServicesManager().load(VpPlayerManager.class);

        var commands = PluginCommandYamlParser.parse(VotePoints.getInstance());
        pluginCommands.addAll(commands.stream().map(x -> x.getName().toLowerCase()).toList());
        pluginCommands.addAll(commands.stream().flatMap(x -> x.getAliases().stream()).toList());
    }

    @org.bukkit.event.EventHandler
    public void onPlayerVote(VotifierEvent args){
        var vote = args.getVote();
        var player = Bukkit.getOfflinePlayer(vote.getUsername());
        if(player.getUniqueId().version() == 3 ) return;

        vpManager.addPoints(player.getUniqueId(), 1);
        vpManager.incrementTotalVotes(player.getUniqueId());

        String displayName = player.getName();
        if(player.isOnline()){
            var onlinePlayer = player.getPlayer();
            displayName = onlinePlayer.getDisplayName();
            onlinePlayer.sendMessage(ChatColor.GREEN + "You've received " + ChatColor.RED + "1" + ChatColor.GREEN + " vote point for voting on " + ChatColor.YELLOW + vote.getServiceName() + ChatColor.GREEN + "!");

        }

        Util.broadcastExcept(ChatColor.YELLOW + displayName + ChatColor.GREEN + " has received " + ChatColor.RED + "1" + ChatColor.GREEN + " vote point for voting on " + ChatColor.YELLOW + vote.getServiceName() + ChatColor.GREEN + "!", player.getUniqueId());
    }

    @org.bukkit.event.EventHandler
    public void commandPreprocess(PlayerCommandPreprocessEvent event){
        var parts = event.getMessage().split(" ");
        var label = parts[0].replace("/", "");

        if(pluginCommands.contains(label.toLowerCase())){
            event.setCancelled(true);
            var command = VotePoints.getInstance().getCommand(label);

            if(command.testPermission(event.getPlayer())){
                var args = Arrays.stream(parts).skip(1).toArray(String[]::new);
                var executor = command.getExecutor();

                executor.onCommand(event.getPlayer(), command, label, args);
            }
        }
    }

    @org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
    public void tabComplete(TabCompleteEvent event){
        var parts = event.getBuffer().split(" ");
        var label = parts[0].replace("/", "");

        if(pluginCommands.contains(label.toLowerCase())){
            var command = VotePoints.getInstance().getCommand(label);

            if(command.testPermission(event.getSender())){
                var tabComplete = command.getTabCompleter();
                if(tabComplete != null){
                    var args = new ArrayList<>(Arrays.stream(parts).skip(1).toList());
                    if(event.getBuffer().endsWith(" "))
                        args.add("");

                    var completions = tabComplete.onTabComplete(event.getSender(), command, label, args.toArray(String[]::new));
                    event.setCompletions(completions);
                }
            }
        }
    }
}
