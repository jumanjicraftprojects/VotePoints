package dev.appeazethecheese.votepoints;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import dev.appeazethecheese.votepoints.data.HibernateUtil;
import dev.appeazethecheese.votepoints.data.entities.PlayerPointsEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VpPlayerManager {

    LoadingCache<UUID, PlayerPointsEntity> entityCache;

    VpPlayerManager(){
        entityCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(3, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<>() {
                            @Override
                            public @NotNull PlayerPointsEntity load(@NotNull UUID key) {
                                return getEntityFromDb(key);
                            }
                        }
                );
    }

    private PlayerPointsEntity getEntityFromDb(UUID playerId){
        try(var session = HibernateUtil.getSession()){
            var entity = session.get(PlayerPointsEntity.class, playerId);
            if(entity == null)
                return new PlayerPointsEntity(playerId);

            session.detach(entity);
            return entity;
        }
    }

    public void incrementTotalVotes(UUID playerId){
        try(var session = HibernateUtil.getSession()){
            var entity = session.get(PlayerPointsEntity.class, playerId);

            var transaction = session.beginTransaction();
            if(entity == null){
                entity = new PlayerPointsEntity(playerId);
                session.persist(entity);
            }

            entity.incrementTotalVotes();
            transaction.commit();

            session.detach(entity);
            entityCache.put(playerId, entity);
        }
    }

    public int getTotalVotes(Player player){
        return getTotalVotes(player.getUniqueId());
    }

    public int getTotalVotes(UUID playerId){
        var entity = entityCache.getUnchecked(playerId);
        return entity.getTotalVotes();
    }

    public int getPoints(Player player){
        return getPoints(player.getUniqueId());
    }

    public int getPoints(UUID playerId){
        var entity = entityCache.getUnchecked(playerId);
        return entity.getVotePoints();
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

            session.detach(entity);
            entityCache.put(playerId, entity);
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
