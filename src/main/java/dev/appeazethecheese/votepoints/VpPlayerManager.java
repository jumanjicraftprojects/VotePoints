package dev.appeazethecheese.votepoints;

import dev.appeazethecheese.votepoints.data.HibernateUtil;
import dev.appeazethecheese.votepoints.data.entities.PlayerPointsEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VpPlayerManager {

    public static int getTotalVotes(Player player){
        return getTotalVotes(player.getUniqueId());
    }

    public static int getTotalVotes(UUID playerId){
        try(var session = HibernateUtil.getSession()){
            var entity = session.get(PlayerPointsEntity.class, playerId);
            if(entity == null) return 0;

            return entity.getTotalVotes();
        }
    }

    public static int getPoints(Player player){
        return getPoints(player.getUniqueId());
    }

    public static int getPoints(UUID playerId){
        try(var session = HibernateUtil.getSession()){
            var entity = session.get(PlayerPointsEntity.class, playerId);

            if(entity == null) return 0;
            return entity.getVotePoints();
        }
    }

    public static void setPoints(Player player, int points){
        setPoints(player.getUniqueId(), points);
    }

    public static void setPoints(UUID playerId, int points){
        try(var session = HibernateUtil.getSession()){
            var entity = session.get(PlayerPointsEntity.class, playerId);

            var transaction = session.beginTransaction();
            if(entity == null){
                entity = new PlayerPointsEntity(playerId);
                session.persist(entity);
            }

            entity.setVotePoints(points);
            transaction.commit();
        }
    }

    public static void addPoints(Player player, int points){
        addPoints(player.getUniqueId(), points);
    }

    public static void addPoints(UUID playerId, int points){
        setPoints(playerId, getPoints(playerId) + points);
    }

    public static boolean removePoints(Player player, int points){
        return removePoints(player.getUniqueId(), points);
    }

    public static boolean removePoints(UUID playerId, int points){
        var current = getPoints(playerId);
        if(points > current) return false;
        addPoints(playerId, -points);
        return true;
    }
}
