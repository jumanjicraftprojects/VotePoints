package dev.appeazethecheese.votepoints;

import dev.appeazethecheese.votepoints.data.HibernateUtil;
import dev.appeazethecheese.votepoints.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.ufactions.customcrates.CustomCrates;

import java.util.logging.Logger;

public final class VotePoints extends JavaPlugin {

    public static Logger logger;
    private static VotePoints instance;

    public static VotePoints getInstance(){
        return instance;
    }

    private Placeholders placeholders;

    @Override
    public void onEnable() {
        try{
            // Plugin startup logic
            instance = this;
            saveDefaultConfig();

            logger = this.getLogger();
            HibernateUtil.init();

            var vpManager = new VpPlayerManager();
            this.getServer().getServicesManager().register(VpPlayerManager.class, vpManager, this, ServicePriority.Normal);

            Bukkit.getPluginManager().registerEvents(new EventHandler(), this);

            var vpCommandClass = new VotePointsCommand();
            var vpCommand = this.getCommand("votepoints");
            vpCommand.setExecutor(vpCommandClass);
            vpCommand.setTabCompleter(vpCommandClass);

            var vpTopCommandClass = new VpTopCommand();
            var vpTopCommand = this.getCommand("vptop");
            vpTopCommand.setExecutor(vpTopCommandClass);
            vpTopCommand.setTabCompleter(vpTopCommandClass);

            var customCrates = (CustomCrates)Bukkit.getPluginManager().getPlugin("CustomCrates");

            var voteShopCommandClass = new VoteShopCommand(customCrates);
            var voteShopCommand = this.getCommand("voteshop");
            voteShopCommand.setExecutor(voteShopCommandClass);
            voteShopCommand.setTabCompleter(voteShopCommandClass);

            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                this.placeholders = new Placeholders();
                if(placeholders.canRegister()){
                    placeholders.register();
                }
                else {
                    logger.warning("Failed to register Placeholder API Expansion.");
                }
            }
        } catch (Throwable e){
            logger.severe("Failed to load VotePoints");
            e.printStackTrace();
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try{
            this.getServer().getServicesManager().unregister(VpPlayerManager.class);
        }catch (Throwable ignored) {}

        try{
            if(placeholders != null && placeholders.isRegistered()) {
                placeholders.unregister();
            }
        }catch (Throwable ignored) {}

        try{
            HibernateUtil.shutdown();
        }catch(Throwable ignored) {}
    }
}
