package com.ylli.api.pay.util;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 生成系统序列号.
     */
    private static final String SERIAL_KEY = "serial";
    private static final String SERIAL_VALUE = "0";

    @PostConstruct
    void init() {
        String serialValue = redisTemplate.opsForValue().get(SERIAL_KEY);

        if (Strings.isNullOrEmpty(serialValue)) {
            redisTemplate.opsForValue().set(SERIAL_KEY, SERIAL_VALUE);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void autoReset() {
        redisTemplate.delete(SERIAL_KEY);
        init();
    }

    public Long getValue() {
        return redisTemplate.opsForValue().increment(SERIAL_KEY, 1);
    }

    /**
     * 订单号生成。
     */
    public String generateSysOrderId() {

        String dateStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        StringBuffer sb = new StringBuffer(dateStr);
        return sb.append(StringUtils.leftPad(String.valueOf(getValue()), 8, "0")).toString();
    }

    /**
     * 订单号生成。
     */
    public String generateSysOrderId20() {

        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        StringBuffer sb = new StringBuffer(dateStr);
        return sb.append(StringUtils.leftPad(String.valueOf(getValue()), 6, "0")).toString();
    }




}
