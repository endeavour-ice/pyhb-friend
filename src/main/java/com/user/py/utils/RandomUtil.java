package com.user.py.utils;

import java.util.Random;

/**
 * @Author ice
 * @Date 2022/9/27 11:46
 * @PackageName:com.user.util.utils
 * @ClassName: RandomUtil
 * @Description: TODO
 * @Version 1.0
 */
public class RandomUtil {
    /**
     * @return 4 位数验证码
     */
    public static String getRandomFour() {
        return getRandom(2);
    }
    /**
     * @return 6 位数验证码
     */
    public static String getRandomSix() {
        return getRandom(3);
    }

    private static String getRandom(int num) {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int j = 0; j < num; j++) {
            int nextInt = random.nextInt(99);
            if (nextInt < 10) {
                nextInt += 10;
            }
            code.append(nextInt);
        }
        return code.toString();
    }
}
