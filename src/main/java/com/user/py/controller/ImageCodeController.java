package com.user.py.controller;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import com.user.py.annotation.CurrentLimiting;
import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.constant.CacheConstants;
import com.user.py.mode.constant.ImageConstants;
import com.user.py.utils.RedisCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author ice
 * @Date 2023/3/16 11:00
 * @Description: 生成验证码
 */
@RestController
public class ImageCodeController {
    @Resource
    private RedisCache redisCache;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    @CurrentLimiting
    public B<Map<String, String>> getCodeS(HttpServletResponse response) {
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        Map<String, String> hashMap = new HashMap<>(2);
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        CircleCaptcha captcha;
        try {
            captcha = CaptchaUtil.createCircleCaptcha(112, 38, 4, 3);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        String code = captcha.getCode();
        String imageBase64Data = captcha.getImageBase64Data();
        redisCache.setCacheObject(verifyKey, code, ImageConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        hashMap.put("uuid", uuid);
        hashMap.put("img", imageBase64Data);
        return B.ok(hashMap);
    }
}
