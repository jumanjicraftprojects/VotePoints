package dev.appeazethecheese.votepoints;

import com.vexsoftware.votifier.model.VotifierEvent;
import dev.appeazethecheese.votepoints.data.HibernateUtil;
import dev.appeazethecheese.votepoints.data.entities.PlayerPointsEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import java.util.logging.Level;

public class EventHandler implements Listener {
    @org.bukkit.event.EventHandler
    public void onPlayerVote(VotifierEvent args){
        var vote = args.getVote();
        var player = Bukkit.getOfflinePlayer(vote.getUsername());
        if(player.getUniqueId().version() == 3 ) return;

        var uuid = player.getUniqueId();
        try(var session = HibernateUtil.getSession()){
            var transaction = session.beginTransaction();

            var entity = session.get(PlayerPointsEntity.class, uuid);
            if(entity == null) {
                entity = new PlayerPointsEntity(uuid);
                session.persist(entity);
            }

            entity.setVotePoints(entity.getVotePoints() + 1);
            entity.incrementTotalVotes();
            transaction.commit();
        }

        Util.broadcastExcept(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has received " + ChatColor.RED + "1" + ChatColor.GREEN + " vote point for voting on " + ChatColor.YELLOW + vote.getServiceName() + ChatColor.GREEN + "!", player.getUniqueId());
        if(player.isOnline()){
            player.getPlayer().sendMessage(ChatColor.GREEN + "You've received " + ChatColor.RED + "1" + ChatColor.GREEN + " vote point for voting on " + ChatColor.YELLOW + vote.getServiceName() + ChatColor.GREEN + "!");
        }
    }
}
