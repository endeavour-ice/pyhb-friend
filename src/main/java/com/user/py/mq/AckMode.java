package com.user.py.mq;

public interface AckMode {

    // 自动确认，默认模式
    String NONE = "NONE";
    // 根据情况确认
    String AUTO = "AUTO";
    // 手动确认
    String MANUAL = "MANUAL";

}
