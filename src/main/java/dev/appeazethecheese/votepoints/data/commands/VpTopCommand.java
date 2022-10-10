package dev.appeazethecheese.votepoints.data.commands;

import dev.appeazethecheese.votepoints.data.HibernateUtil;
import dev.appeazethecheese.votepoints.data.entities.PlayerPointsEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VpTopCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)){
            sender.sendMessage("This command must be run as a player.");
            return true;
        }

        final int playersPerPage = 10;

        if(args.length == 0 || args[0].equalsIgnoreCase("votes")){
            int page = 1;
            if(args.length >= 2){
                var pageStr = args[1];
                try{
                    page = Integer.parseInt(pageStr);
                }catch (Exception e){
                    player.sendMessage(ChatColor.RED + args[1] + " is not a valid page number");
                    return true;
                }
            }

            if(page <= 0){
                player.sendMessage(ChatColor.RED + "Page must be 1 or greater.");
            }

            var offset = playersPerPage * (page - 1);
            long count;

            try(var session = HibernateUtil.getSession()){
                var builder = session.getCriteriaBuilder();
                var query = builder.createQuery(Long.class);
                var root = query.from(PlayerPointsEntity.class);
                query.where(builder.notEqual(root.get("totalVotes"), 0)).select(builder.count(root));
                count = session.createQuery(query).getSingleResult();
            }

            if(offset >= count){
                offset = (int)(count / playersPerPage);
            }
            var actualPage = (offset / playersPerPage) + 1;

            List<PlayerPointsEntity> players;
            try(var wrapper = HibernateUtil.getQueryWrapper(PlayerPointsEntity.class)){
                var root = wrapper.root();
                var builder = wrapper.builder();

                wrapper.criteria().where(builder.notEqual(root.get("totalVotes"), 0)).orderBy(builder.desc(root.get("totalVotes")));
                players = wrapper.query().setFirstResult(offset).setMaxResults(playersPerPage).getResultList();
            }

            var lines = new ArrayList<String>();
            lines.add("" + ChatColor.GREEN + ChatColor.UNDERLINE + "Total Votes Leaderboard");
            lines.add("");
            lines.add("" + ChatColor.GREEN + "Page " + ChatColor.RED + actualPage + " of " + ((count / playersPerPage) + 1));
            for(var i = 0; i < players.size(); i++) {
                var pointPlayer = players.get(i);
                var playerName = Bukkit.getOfflinePlayer(pointPlayer.getPlayerUuid()).getName();
                lines.add(ChatColor.GREEN + String.valueOf(i + 1) + ". " + ChatColor.YELLOW + playerName + ChatColor.RESET + ": " + ChatColor.RED + pointPlayer.getTotalVotes());
            }

            player.sendMessage(String.join("\n" + ChatColor.RESET, lines));
        }
        else if(args[0].equalsIgnoreCase("points")){
            int page = 1;
            if(args.length >= 2){
                var pageStr = args[1];
                try{
                    page = Integer.parseInt(pageStr);
                }catch (Exception e){
                    player.sendMessage(ChatColor.RED + args[1] + " is not a valid page number");
                    return true;
                }
            }

            if(page <= 0){
                player.sendMessage(ChatColor.RED + "Page must be 1 or greater.");
            }

            var offset = playersPerPage * (page - 1);
            long count;

            try(var session = HibernateUtil.getSession()){
                var builder = session.getCriteriaBuilder();
                var query = builder.createQuery(Long.class);
                var root = query.from(PlayerPointsEntity.class);
                query.where(builder.notEqual(root.get("votePoints"), 0)).select(builder.count(root));
                count = session.createQuery(query).getSingleResult();
            }

            if(offset >= count){
                offset = (int)(count / playersPerPage);
            }
            var actualPage = (offset / playersPerPage) + 1;

            List<PlayerPointsEntity> players;
            try(var wrapper = HibernateUtil.getQueryWrapper(PlayerPointsEntity.class)){
                var root = wrapper.root();
                var builder = wrapper.builder();

                wrapper.criteria().where(builder.notEqual(root.get("votePoints"), 0)).orderBy(builder.desc(root.get("votePoints")));
                players = wrapper.query().setFirstResult(offset).setMaxResults(playersPerPage).getResultList();
            }

            var lines = new ArrayList<String>();
            lines.add("" + ChatColor.GREEN + ChatColor.UNDERLINE + "Vote Points Leaderboard");
            lines.add("");
            lines.add("" + ChatColor.GREEN + "Page " + ChatColor.RED + actualPage + " of " + ((count / playersPerPage) + 1));
            for(var i = 0; i < players.size(); i++) {
                var pointPlayer = players.get(i);
                var playerName = Bukkit.getOfflinePlayer(pointPlayer.getPlayerUuid()).getName();
                lines.add(ChatColor.GREEN + String.valueOf(i + 1) + ". " + ChatColor.YELLOW + playerName + ChatColor.RESET + ": " + ChatColor.RED + pointPlayer.getVotePoints());
            }

            player.sendMessage(String.join("\n" + ChatColor.RESET, lines));
        }
        else{
            player.sendMessage(ChatColor.RED + "Usage: /vptop [votes|points]");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1){
            return List.of("votes", "points");
        }
        return new ArrayList<>();
    }
}
