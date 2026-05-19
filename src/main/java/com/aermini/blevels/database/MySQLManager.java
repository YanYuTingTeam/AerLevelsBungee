package com.aermini.blevels.database;

import com.aermini.blevels.AerLevelsBungee;
import java.sql.*;
import java.util.UUID;

public class MySQLManager {
    private final AerLevelsBungee plugin;
    private Connection connection;
    private String address;
    private String database;
    private String username;
    private String password;

    public MySQLManager(AerLevelsBungee plugin) {
        this.plugin = plugin;
        loadDatabaseConfig();
    }

    private void loadDatabaseConfig() {
        address = plugin.getConfigManager().getConfig().getString("database.address", "127.0.0.1:3306");
        database = plugin.getConfigManager().getConfig().getString("database.database", "minecraft");
        username = plugin.getConfigManager().getConfig().getString("database.username", "root");
        password = plugin.getConfigManager().getConfig().getString("database.password", "");
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        String url = "jdbc:mysql://" + address + "/" + database
                + "?useSSL=false&characterEncoding=utf8&serverTimezone=UTC&autoReconnect=true&maxReconnects=3&connectTimeout=10000";
        connection = DriverManager.getConnection(url, username, password);
        plugin.getLogger().info("数据库连接成功");
        startHeartbeat();
    }

    public void disconnect() throws SQLException {
        stopHeartbeat();
        if (connection != null && !connection.isClosed()) {
            connection.close();
            plugin.getLogger().info("数据库连接断开");
        }
    }

    private int heartbeatTaskId = -1;

    // 数据库心跳
    private void startHeartbeat() {
        if (heartbeatTaskId != -1) {
            return;
        }
        heartbeatTaskId = plugin.getProxy().getScheduler().schedule(plugin, () -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("SELECT 1");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("已严肃泵衰竭 -> " + e.getMessage());
            }
        }, 5, 5, java.util.concurrent.TimeUnit.MINUTES).getId();
    }

    // 心脏骤停.
    private void stopHeartbeat() {
        if (heartbeatTaskId != -1) {
            plugin.getProxy().getScheduler().cancel(heartbeatTaskId);
            heartbeatTaskId = -1;
        }
    }

    // aicode
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            plugin.getLogger().warning("数据库连接断开. 尝试重连");
            connect();
        }
        try {
            if (!connection.isValid(3)) {
                plugin.getLogger().warning("数据库连接无效. 尝试重连");
                connection.close();
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("数据库连接验证失败. 尝试重连 " + e.getMessage());
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
            connect();
        }
        return connection;
    }

    public int getPlayerLevelByName(String playerName) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                "SELECT level FROM aerlevels_data WHERE player_name = ?")) {
            pstmt.setString(1, playerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("level");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("获取玩家等级失败 -> " + e.getMessage());
        }
        return -1;
    }

    public int getPlayerLevelByUUID(String uuidString) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                "SELECT level FROM aerlevels_data WHERE uuid = ?")) {
            pstmt.setString(1, uuidString);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("level");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("获取玩家等级失败 -> " + e.getMessage());
        }
        return -1;
    }

    public int getPlayerLevel(UUID uuid) {
        return getPlayerLevelByUUID(uuid.toString());
    }
}
