package com.aermini.blevels.api;

import com.aermini.blevels.AerLevelsBungee;

import java.util.UUID;

/**
 * AI api
 *
 * 使用示例：
 * <pre>
 * // 通过玩家名称获取等级
 * int level = AerLevelsAPI.getPlayerLevel("PlayerName");
 *
 * // 通过 UUID 获取等级
 * int level = AerLevelsAPI.getPlayerLevel(uuid);
 * </pre>
 */
public class AerLevelsAPI {

    /**
     * 通过玩家名称获取玩家等级
     *
     * @param playerName 玩家名称
     * @return 玩家等级，如果玩家不存在或插件未加载返回 -1
     */
    public static int getPlayerLevel(String playerName) {
        AerLevelsBungee plugin = AerLevelsBungee.getInstance();
        if (plugin == null) {
            return -1;
        }
        return plugin.getPlayerLevel(playerName);
    }

    /**
     * 通过 UUID 获取玩家等级
     *
     * @param uuid 玩家 UUID
     * @return 玩家等级，如果玩家不存在或插件未加载返回 -1
     */
    public static int getPlayerLevel(UUID uuid) {
        AerLevelsBungee plugin = AerLevelsBungee.getInstance();
        if (plugin == null) {
            return -1;
        }
        return plugin.getPlayerLevelByUUID(uuid.toString());
    }

    /**
     * 检查 AerLevelsBungee 是否已加载
     *
     * @return 如果插件已加载返回 true
     */
    public static boolean isLoaded() {
        return AerLevelsBungee.getInstance() != null;
    }
}
