package com.user.py.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author ice
 * @date 2022/9/17 21:22
 */
@Component
public class ConstantPropertiesUtils implements InitializingBean {


    // 读取配置文件
    @Value("${aliyun.oss.file.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.file.keyid}")
    private String keyId;

    @Value("${aliyun.oss.file.keysecret}")
    private String keySecret;

    @Value("${aliyun.oss.file.bucketname}")
    private String bucketName;

    @Value(("${email.fromEmail}"))
    private String email;

    @Value(("${email.password}"))
    private String emailPassword;
    @Value(("${chatGpt.token}"))
    private String token;
    // 服务器地址
    public static String END_POINT;
    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String BUCKET_NAME;
    // 邮箱地址
    public static String EMAIL;
    public static String EMAILPASSWORD;
    // chatgpt
    public static String CG_TOKEN;
    @Override
    public void afterPropertiesSet() throws Exception {
        END_POINT = endpoint;
        ACCESS_KEY_ID = keyId;
        ACCESS_KEY_SECRET = keySecret;
        BUCKET_NAME = bucketName;
        EMAIL = email;
        EMAILPASSWORD = emailPassword;
        CG_TOKEN = token;
    }
}
