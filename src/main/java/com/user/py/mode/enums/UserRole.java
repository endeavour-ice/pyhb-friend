package com.user.py.mode.enums;

/**
 * @Author ice
 * @Date 2023/3/18 11:42
 * @Description: TODO
 */
public enum UserRole {
    /**
     * 普通用户
     */
    NORMAL(0,"普通用户"),
    /**
     * 管理员
     */
    ADMIN(1,"管理员"),

    /**
     * 测试
     */
    TEST(2,"测试");
    private final int key;
    private final String name;

    UserRole(int key,String name) {
        this.key = key;
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
