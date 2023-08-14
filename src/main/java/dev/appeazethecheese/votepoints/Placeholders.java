package dev.appeazethecheese.votepoints;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Placeholders extends PlaceholderExpansion {
    private VpPlayerManager vpManager;

    public Placeholders(){
        super();
        vpManager = Bukkit.getServicesManager().load(VpPlayerManager.class);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "votepoints";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AppeazeTheCheese";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if(params.equalsIgnoreCase("points")){
            return String.valueOf(vpManager.getPoints(player.getUniqueId()));
        }

        return null;
    }
}
