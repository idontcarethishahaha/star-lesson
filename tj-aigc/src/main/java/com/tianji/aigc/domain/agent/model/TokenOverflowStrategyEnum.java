package com.tianji.aigc.domain.agent.model;

public enum TokenOverflowStrategyEnum {

    NONE,

    SLIDING_WINDOW,

    SUMMARIZE;

    public static boolean isValid(String value) {
        try {
            TokenOverflowStrategyEnum.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static TokenOverflowStrategyEnum fromString(String value) {
        if (value == null) {
            return NONE;
        }

        try {
            return TokenOverflowStrategyEnum.valueOf(value);
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
