package com.ylli.api.user.service;

import com.google.common.base.Strings;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.UserBaseMapper;
import com.ylli.api.user.model.UserBase;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
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

        UserBase exist = new UserBase();
        exist.userId = userBase.userId;
        exist = userBaseMapper.selectOne(exist);
        if (exist != null) {
            /**
             * state null 待审核
             * 1 pass
             * 0 fail
             */
            if (exist.state == UserBase.PASS) {
                throw new AwesomeException(Config.ERROR_AUDIT_PASS);
            }
            throw new AwesomeException(Config.ERROR_AUDIT_ING);
        }
        userBase.state = null;
        userBase.merchantNo = getCode(userBase.userType);
        userBaseMapper.insertSelective(userBase);
    }


    public UserBase selectByMerchantNo(String merchantNo) {
        UserBase base = new UserBase();
        base.merchantNo = merchantNo;
        return userBaseMapper.selectOne(base);
    }

    @Transactional
    public Object audit(Long userId, Integer state) {
        UserBase userBase = new UserBase();
        userBase.userId = userId;
        userBase = userBaseMapper.selectOne(userBase);
        if (userBase == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        userBase.state = state;
        userBase.modifyTime = Timestamp.from(Instant.now());
        userBaseMapper.updateByPrimaryKeySelective(userBase);
        return userBaseMapper.selectByPrimaryKey(userBase);
    }
}
