package com.aermini.blevels.config;

import com.aermini.blevels.AerLevelsBungee;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

// aicode
public class ConfigManager {
    private final AerLevelsBungee plugin;
    private File configFile;
    private Configuration config;

    public ConfigManager(AerLevelsBungee plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("加载配置文件失败 -> " + e.getMessage());
        }

        InputStream defConfigStream = plugin.getResourceAsStream("config.yml");
        if (defConfigStream != null) {
            Configuration defConfig = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            config = mergeConfig(defConfig, config);
        }
    }

    public void saveDefaultConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResourceAsStream("config.yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, configFile.toPath());
                } else {
                    createDefaultConfig();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("保存默认配置失败 -> " + e.getMessage());
            }
        }
    }

    private void createDefaultConfig() {
        try {
            Configuration defaultConfig = new Configuration();
            defaultConfig.set("database.address", "127.0.0.1:3306");
            defaultConfig.set("database.database", "minecraft");
            defaultConfig.set("database.username", "root");
            defaultConfig.set("database.password", "");

            ConfigurationProvider.getProvider(YamlConfiguration.class).save(defaultConfig, configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("创建默认配置失败 -> " + e.getMessage());
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public Configuration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    private Configuration mergeConfig(Configuration defaults, Configuration config) {
        if (defaults == null) return config;
        if (config == null) return defaults;

        for (String key : defaults.getKeys()) {
            if (!config.contains(key)) {
                config.set(key, defaults.get(key));
            } else if (defaults.get(key) instanceof Configuration) {
                Object configValue = config.get(key);
                if (configValue instanceof Configuration) {
                    mergeConfig((Configuration) defaults.get(key), (Configuration) configValue);
                }
            }
        }
        return config;
    }
}
