package dev.appeazethecheese.votepoints.data;

import java.util.HashMap;
import java.util.Map;


import dev.appeazethecheese.votepoints.VotePoints;
import dev.appeazethecheese.votepoints.data.entities.PlayerPointsEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

public class HibernateUtil {

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    private static void constructSessionFactory(){
        shutdown();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder();

            var config = VotePoints.getInstance().getConfig();
            Map<String, Object> settings = new HashMap<>();
            settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
            settings.put(Environment.URL, "jdbc:mysql://" + config.getString("dbAddress") + ":" + config.getInt("dbPort") + "/" + config.getString("dbName") + "?autoReconnect=true&useSSL=false");
            settings.put(Environment.USER, config.getString("dbUsername"));
            settings.put(Environment.PASS, config.getString("dbPassword"));
            settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
            settings.put(Environment.HBM2DDL_AUTO, "update");
            settings.put(Environment.SHOW_SQL, false);

            // HikariCP settings

            // Maximum waiting time for a connection from the pool
            settings.put("hibernate.hikari.connectionTimeout", "20000");
            // Minimum number of ideal connections in the pool
            settings.put("hibernate.hikari.minimumIdle", "10");
            // Maximum number of actual connection in the pool
            settings.put("hibernate.hikari.maximumPoolSize", "20");
            // Maximum time that a connection is allowed to sit ideal in the pool
            settings.put("hibernate.hikari.idleTimeout", "300000");

            registryBuilder.applySettings(settings);

            registry = registryBuilder.build();
            MetadataSources sources = new MetadataSources(registry)
                    .addAnnotatedClass(PlayerPointsEntity.class);

            MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
            metadataBuilder.applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE);
            Metadata metadata = metadataBuilder.build();
            sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            if (registry != null) {
                StandardServiceRegistryBuilder.destroy(registry);
            }
            e.printStackTrace();
        }
    }

    public static void init(){
        if(sessionFactory == null)
            constructSessionFactory();
    }

    public static Session getSession(){
        return sessionFactory.openSession();
    }

    public static <T> QueryWrapper<T> getQueryWrapper(Class<T> type){
        return new QueryWrapper<>(type, sessionFactory.openSession());
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}