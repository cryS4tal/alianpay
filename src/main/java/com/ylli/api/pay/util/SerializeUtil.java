package com.ylli.api.pay.util;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.time.Instant;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SerializeUtil {

    @Autowired
    StringRedisTemplate redisTemplate;

    private static final String SERIAL_KEY = "serial";
    private static final String SERIAL_VALUE = "0";

    //先锋支付
    public static final String XF_PAY = "A";
    //易付宝
    public static final String YFB_PAY = "B";


    /**
     * 自动维护了一个自增序列，暂时没用到，先保留
     */
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
     * 商户订单号：yyyyMMddHHmmss + 支付通道标识 + leftPad(userId,7,0) + leftPad(billId,8,0)
     *
     * @return
     */
    public String generateOrderNo(String code, Long userId, Long billId) {

        return new StringBuffer()
                .append(new SimpleDateFormat("yyyyMMdd").format(java.sql.Date.from(Instant.now())))
                .append(code)
                .append(StringUtils.leftPad(String.valueOf(userId), 7, "0"))
                .append(StringUtils.leftPad(String.valueOf(billId), 8, "0"))
                .toString();
    }

}
