package dev.appeazethecheese.votepoints;

import dev.appeazethecheese.votepoints.data.HibernateUtil;
import dev.appeazethecheese.votepoints.data.commands.VotePointsCommand;
import dev.appeazethecheese.votepoints.data.commands.VpTopCommand;
import org.bukkit.Bukkit;
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

        var vpCommandClass = new VotePointsCommand();
        var vpCommand = this.getCommand("votepoints");
        vpCommand.setExecutor(vpCommandClass);
        vpCommand.setTabCompleter(vpCommandClass);

        var vpTopCommandClass = new VpTopCommand();
        var vpTopCommand = this.getCommand("vptop");
        vpTopCommand.setExecutor(vpTopCommandClass);
        vpTopCommand.setTabCompleter(vpTopCommandClass);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
