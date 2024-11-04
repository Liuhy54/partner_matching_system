package com.hai_friend.hai_friend_backend.model.enums;

/**
 * 队伍状态枚举
 */
public enum teamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私密"),
    SECRET(2, "加密");

    private int value;

    private String text;

    public static teamStatusEnum getEnumByValue(Integer value){
        if (value == null) {
            return null;
        }
        teamStatusEnum[] values = teamStatusEnum.values();
        for (com.hai_friend.hai_friend_backend.model.enums.teamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    teamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
