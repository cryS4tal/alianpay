package com.ylli.api.user.service;

import com.google.common.base.Strings;
import com.ylli.api.user.mapper.UserBaseMapper;
import com.ylli.api.user.model.UserBase;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBaseService {

    private static final String SERIAL_KEY = "serial";
    private static final String SERIAL_VALUE = "0";

    @Autowired
    UserBaseMapper userBaseMapper;

    @Autowired
    StringRedisTemplate redisTemplate;

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

    @Transactional
    public void register(UserBase userBase) {
        userBase.merchantNo = getCode(userBase.userType);
        userBaseMapper.insertSelective(userBase);
    }


}
