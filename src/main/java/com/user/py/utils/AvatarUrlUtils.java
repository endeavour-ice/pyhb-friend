package com.user.py.utils;

import cn.hutool.core.util.RandomUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/3/21 18:16
 * @Description: TODO
 */
public class AvatarUrlUtils {
    private static final List<String> url = Arrays.asList("https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211345104010.webp",
            "https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211345104014.webp",
            "https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211345515494.webp",
            "https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211345515497.webp",
            "https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211348014461.webp");
    public static String getRandomUrl() {
        int anInt = RandomUtil.randomInt(0, url.size());
        return url.get(anInt);
    }
}
