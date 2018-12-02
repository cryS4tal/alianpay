package com.ylli.api.user.service;

import com.ylli.api.user.mapper.UserKeyMapper;
import com.ylli.api.user.model.UserKey;
import com.ylli.api.user.model.UserKeyRes;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserKeyService {

    @Autowired
    UserKeyMapper userKeyMapper;

    @Transactional
    public UserKeyRes saveKey(Long userId, String secretKey) {
        UserKey userKey = new UserKey();
        userKey.userId = userId;
        userKey = userKeyMapper.selectOne(userKey);
        if (userKey == null) {
            userKey = new UserKey();
            userKey.userId = userId;
            userKey.secretKey = secretKey;
            userKeyMapper.insertSelective(userKey);
        } else {
            userKey.secretKey = secretKey;
            userKeyMapper.updateByPrimaryKeySelective(userKey);
        }
        UserKeyRes res = new UserKeyRes();
        res.key = userKeyMapper.getKeyById(userId);
        return res;
    }

    public UserKey getKeyByUserId(Long userId) {
        UserKey userKey = new UserKey();
        userKey.userId = userId;
        return userKeyMapper.selectOne(userKey);
    }

    /**
     * 代付
     * 通过商户号 查询商户私钥.
     *
     * @param mchId
     * @return
     */
    public String getKeyByMchId(String mchId) {
        String key = userKeyMapper.getKeyByMchId(mchId);
        return key;
    }

    /**
     * 支付
     * 根据用户id 查询商户私钥
     *
     * @param mchId
     * @return
     */
    public String getKeyById(Long mchId) {
        String key = userKeyMapper.getKeyById(mchId);
        return key;
    }

    public UserKeyRes getKey(Long userId) {
        String key = userKeyMapper.getKeyById(userId);
        UserKeyRes res = new UserKeyRes();
        res.key = key;
        return res;
    }

    public UserKeyRes randomKey() {
        UserKeyRes res = new UserKeyRes();
        res.key = UUID.randomUUID().toString().replaceAll("-", "");
        return res;
    }
}
