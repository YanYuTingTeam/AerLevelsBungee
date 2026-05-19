package com.aermini.blevels;

import com.aermini.blevels.config.ConfigManager;
import com.aermini.blevels.database.MySQLManager;
import net.md_5.bungee.api.plugin.Plugin;
import java.sql.SQLException;

public class AerLevelsBungee extends Plugin {
    private static AerLevelsBungee instance;
    private ConfigManager configManager;
    private MySQLManager mysqlManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        try {
            mysqlManager = new MySQLManager(this);
            mysqlManager.connect();
        } catch (SQLException e) {
            getLogger().severe("数据库连接失败 -> " + e.getMessage());
            return;
        }

        getLogger().info("AerLevelsBungee 已启用");
    }

    @Override
    public void onDisable() {
        if (mysqlManager != null) {
            try {
                mysqlManager.disconnect();
            } catch (SQLException e) {
                getLogger().severe("数据库断开失败 -> " + e.getMessage());
            }
        }
        getLogger().info("AerLevelsBungee 已禁用");
    }

    public static AerLevelsBungee getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MySQLManager getMysqlManager() {
        return mysqlManager;
    }

    public int getPlayerLevel(String playerName) {
        if (mysqlManager == null) {
            return -1;
        }
        return mysqlManager.getPlayerLevelByName(playerName);
    }

    public int getPlayerLevelByUUID(String uuid) {
        if (mysqlManager == null) {
            return -1;
        }
        return mysqlManager.getPlayerLevelByUUID(uuid);
    }
}
