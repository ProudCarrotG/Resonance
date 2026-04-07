package com.resonance.utils;

/**
 * 统一的 Redis Key 生成器
 */
public class RedisKeyBuilder {

    // 整个项目的根前缀
    private static final String PROJECT_PREFIX = "resonance:";

    public static final String ACTIVE_ROOMS_INDEX = "resonance:active_rooms_index";

    /**
     * 获取房间基本信息的 Key
     * @return 结果例如：resonance:room:12345
     */
    public static String getRoomKey(String roomId) {
        return PROJECT_PREFIX + "room:" + roomId;
    }

    /**
     * 获取房间人员名单(Set)的 Key
     */
    public static String getRoomUsersKey(String roomId) {
        return PROJECT_PREFIX + "room_users:" + roomId;
    }

    /**
     * 获取房间操作分布式锁的 Key
     */
    public static String getRoomLockKey(String roomId) {
        return PROJECT_PREFIX + "lock:room:" + roomId;
    }


    /**
     * 获取用户登录失败次数的 Key
     */
    public static String getLoginFailKey(String username) {
        return PROJECT_PREFIX + "login_fail:" + username;
    }
}
