package dev.appeazethecheese.votepoints;

import dev.appeazethecheese.votepoints.data.HibernateUtil;
import dev.appeazethecheese.votepoints.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class VotePoints extends JavaPlugin {

    public static Logger logger;
    private static VotePoints instance;

    public static VotePoints getInstance(){
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        logger = this.getLogger();
        HibernateUtil.init();
        Bukkit.getPluginManager().registerEvents(new EventHandler(), this);

        var vpManager = new VpPlayerManager();
        this.getServer().getServicesManager().register(VpPlayerManager.class, vpManager, this, ServicePriority.Normal);

        var vpCommandClass = new VotePointsCommand();
        var vpCommand = this.getCommand("votepoints");
        vpCommand.setExecutor(vpCommandClass);
        vpCommand.setTabCompleter(vpCommandClass);

        var vpTopCommandClass = new VpTopCommand();
        var vpTopCommand = this.getCommand("vptop");
        vpTopCommand.setExecutor(vpTopCommandClass);
        vpTopCommand.setTabCompleter(vpTopCommandClass);

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
