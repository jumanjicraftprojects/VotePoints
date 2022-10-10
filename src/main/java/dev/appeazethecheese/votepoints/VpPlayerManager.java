package dev.appeazethecheese.votepoints;

import dev.appeazethecheese.votepoints.data.HibernateUtil;
import dev.appeazethecheese.votepoints.data.entities.PlayerPointsEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VpPlayerManager {

    public int getTotalVotes(Player player){
        return getTotalVotes(player.getUniqueId());
    }

    public int getTotalVotes(UUID playerId){
        try(var session = HibernateUtil.getSession()){
            var entity = session.get(PlayerPointsEntity.class, playerId);
            if(entity == null) return 0;

            return entity.getTotalVotes();
        }
    }

    public int getPoints(Player player){
        return getPoints(player.getUniqueId());
    }

    public int getPoints(UUID playerId){
        try(var session = HibernateUtil.getSession()){
            var entity = session.get(PlayerPointsEntity.class, playerId);

            if(entity == null) return 0;
            return entity.getVotePoints();
        }
    }

    public void setPoints(Player player, int points){
        setPoints(player.getUniqueId(), points);
    }

    public void setPoints(UUID playerId, int points){
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

    public void addPoints(Player player, int points){
        addPoints(player.getUniqueId(), points);
    }

    public void addPoints(UUID playerId, int points){
        setPoints(playerId, getPoints(playerId) + points);
    }

    public boolean removePoints(Player player, int points){
        return removePoints(player.getUniqueId(), points);
    }

    public boolean removePoints(UUID playerId, int points){
        var current = getPoints(playerId);
        if(points > current) return false;
        addPoints(playerId, -points);
        return true;
    }
}
