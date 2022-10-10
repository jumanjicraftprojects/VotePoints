package dev.appeazethecheese.votepoints;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

public class Util {
    public static void broadcastExcept(String message, UUID... playerIds){
        var online = Bukkit.getOnlinePlayers();
        var idList = Arrays.stream(playerIds).toList();
        for (var player : online) {
            if(!idList.contains(player.getUniqueId())) player.sendMessage(message);
        }

        VotePoints.logger.log(Level.INFO, "BROADCAST: " + message);
    }
}
