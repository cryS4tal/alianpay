package com.ylli.api.phone.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.CheckPhone;
import com.ylli.api.phone.Config;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by RexQian on 2017/4/21.
 */
@Service
public class SmsService {
    private static Logger LOGGER = LoggerFactory.getLogger(SmsService.class);

    JpushSmsClient client;

    @Autowired
    LuosimaoClient luosimaoClient;

    //@Value("${jpush.app_key}")
    String appKey;

    //@Value("${jpush.app_secret}")
    String appSecret;

    //@Value("${jpush.sms_temp_id}")
    Integer smsTempId;

    @PostConstruct
    void init() {
        client = new JpushSmsClient(appKey, appSecret);
    }

    @Autowired
    StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_FREQ = "sms:phone:%s:freq";
    private static final String REDIS_KEY_MSG = "sms:phone:%s:msg";

    private static class VerifyMessage {
        public String messageId;
        public Integer failCount;
        public String code;

        public VerifyMessage() {
            failCount = 0;
        }
    }

    //enable=false时, 短信发送和验证
    //@Value("${sms.verify_code.enable}")
    public boolean enable; //seconds

    //@Value("${sms.luosimao.enable}")
    public boolean luosimaoEnable;

    //短信验证码发送间隔
    //@Value("${sms.verify_code.interval}")
    public int verifyCodeInterval; //seconds

    //短信验证码验证出错次数 超过错误次数 要求重发
    //@Value("${sms.verify_code.error_count}")
    public int verifyErrorCount;

    //短信验证码验证超时时间
    //@Value("${sms.verify_code.timeout}")
    public int verifyTimeout; //seconds


    public void sendVerifyCode(String phone, String response) {
        if (!CheckPhone.isSimplePhone(phone)) {
            throw new AwesomeException(Config.ERROR_INVALID_PHONE);
        }
        if (!enable) {
            return;
        }
        if (luosimaoEnable) {
            luosimaoClient.verify(response);
        }

        checkThenResetFreq(phone);
        String msgId = client.sendCode(phone, smsTempId.toString());
        resetVerify(phone, msgId);
    }

    private void checkThenResetFreq(String phone) {
        String value = redisTemplate.opsForValue().get(String.format(REDIS_KEY_FREQ, phone));
        if (value != null) {
            throw new AwesomeException(Config.ERROR_OUT_OF_FREQ);
        }
        redisTemplate.opsForValue().set(String.format(REDIS_KEY_FREQ, phone), "0",
                verifyCodeInterval, TimeUnit.SECONDS);
    }

    private void resetVerify(String phone, String messageId) {
        String key = String.format(REDIS_KEY_MSG, phone);
        VerifyMessage message = new VerifyMessage();
        message.messageId = messageId;
        redisTemplate.opsForValue().set(key, new Gson().toJson(message),
                verifyTimeout, TimeUnit.SECONDS);
    }

    private VerifyMessage getMessage(String phone) {
        String key = String.format(REDIS_KEY_MSG, phone);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return new Gson().fromJson(value, VerifyMessage.class);
    }

    private void verifyFailed(String phone, VerifyMessage message) {
        message.failCount++;
        if (message.failCount > verifyErrorCount) {
            removeVerify(phone);
        }
    }

    private void removeVerify(String phone) {
        redisTemplate.delete(String.format(REDIS_KEY_MSG, phone));
    }

    private void saveVerifyCode(String phone, VerifyMessage message, String code) {
        String key = String.format(REDIS_KEY_MSG, phone);
        message.code = code;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, new Gson().toJson(message), ttl, TimeUnit.SECONDS);
        }
    }

    /**
     * @throws AwesomeException 验证码不正确
     */
    public void checkVerifyCode(String phone, String code) throws AwesomeException {
        //apple 审核
        if ("13093480651".equals(phone)) {
            return;
        }
        if (!CheckPhone.isSimplePhone(phone)) {
            throw new AwesomeException(Config.ERROR_INVALID_PHONE);
        }
        if (Strings.isNullOrEmpty(code)) {
            throw new AwesomeException(Config.ERROR_INVALID_CODE);
        }
        if (!enable) {
            return;
        }

        VerifyMessage message = getMessage(phone);
        if (message == null) {
            throw new AwesomeException(Config.ERROR_EXPIRED_CODE);
        }
        if (!Strings.isNullOrEmpty(message.code)) {
            if (code.equals(message.code)) {
                return;
            }
            throw new AwesomeException(Config.ERROR_CODE);
        }

        boolean isVerify = client.verifyCode(message.messageId, code);
        if (!isVerify) {
            verifyFailed(phone, message);
            throw new AwesomeException(Config.ERROR_CODE);
        } else {
            saveVerifyCode(phone, message, code);
        }
    }
}
