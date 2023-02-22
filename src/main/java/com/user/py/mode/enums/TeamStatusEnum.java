package com.user.py.mode.enums;

/**
 * @author ice
 * @date 2022/8/23 12:04
 */

public enum TeamStatusEnum {

    PUBLIC(0,"公开"),
    PRIVATE(1, "私有"),
    ENCRYPTION(2, "加密");
    private int value;
    private String text;


    public static TeamStatusEnum getTeamStatusByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (TeamStatusEnum teamStatusEnum : TeamStatusEnum.values()) {
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setText(String text) {
        this.text = text;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
}
