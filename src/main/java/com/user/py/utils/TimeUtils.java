package com.user.py.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author ice
 * @date 2022/9/15 18:03
 */

public class TimeUtils {
    /**
     * 距离当天还有多少秒
     * @param currentDate
     * @return
     */
    public static Integer getRemainSecondsOneDay(Date currentDate) {
        //使用plusDays加传入的时间加1天，将时分秒设置成0
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                        ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        //使用ChronoUnit.SECONDS.between方法，传入两个LocalDateTime对象即可得到相差的秒数
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return (int) seconds;
    }
}
