package dev.appeazethecheese.votepoints.data.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "PlayerPoints", uniqueConstraints = {
        @UniqueConstraint(columnNames = "playerUuid")
})
public class PlayerPointsEntity {
    @Id
    @Column(name = "PlayerUuid", nullable = false, updatable = false)
    private UUID playerUuid;

    @Column(name = "VotePoints", nullable = false)
    private int votePoints = 0;

    @Column(name = "TotalVotes", nullable = false)
    private int totalVotes = 0;

    private PlayerPointsEntity(){

    }

    public PlayerPointsEntity(UUID playerUuid){
        this.playerUuid = playerUuid;
    }

    public UUID getPlayerUuid(){
        return playerUuid;
    }

    public int getVotePoints(){
        return votePoints;
    }

    public void setVotePoints(int votePoints){
        this.votePoints = votePoints;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public void incrementTotalVotes() {
        this.totalVotes++;
    }
}
