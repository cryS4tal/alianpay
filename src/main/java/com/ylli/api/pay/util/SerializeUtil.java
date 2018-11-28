package com.ylli.api.pay.util;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
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
     * 商户号：yyyyMMdd + userType + serialValue
     */
    public String getCode(Integer type) {

        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        StringBuffer sb = new StringBuffer(dateStr);

        return sb.append(type).append(StringUtils.leftPad(String.valueOf(getValue()), 6, "0")).toString();
    }

    //todo 对于商户订单是否需要不同支付通道对应不同表？
    /**
     * 商户订单号：yyyyMMddHHmmss + 支付通道标识 + leftPad(userId,7,0) + leftPad(billId,8,0)
     *
     * @return
     */
    public String generateOrderNo(String channel,Long userId, Long billId) {

        return new StringBuffer()
                .append(new SimpleDateFormat("yyyyMMddHHmmss").format(java.sql.Date.from(Instant.now())))
                .append(channel)
                .append(StringUtils.leftPad(String.valueOf(userId), 7, "0"))
                .append(StringUtils.leftPad(String.valueOf(billId), 8, "0"))
                .toString();
    }

}
