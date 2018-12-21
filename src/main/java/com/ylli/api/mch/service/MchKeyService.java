package com.ylli.api.mch.service;

import com.ylli.api.mch.mapper.MchKeyMapper;
import com.ylli.api.mch.model.MchKey;
import com.ylli.api.mch.model.MchKeyRes;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MchKeyService {

    @Autowired
    MchKeyMapper userKeyMapper;

    @Transactional
    public MchKeyRes saveKey(Long userId, String secretKey) {
        MchKey userKey = new MchKey();
        userKey.userId = userId;
        userKey = userKeyMapper.selectOne(userKey);
        if (userKey == null) {
            userKey = new MchKey();
            userKey.userId = userId;
            userKey.secretKey = secretKey;
            userKeyMapper.insertSelective(userKey);
        } else {
            userKey.secretKey = secretKey;
            userKeyMapper.updateByPrimaryKeySelective(userKey);
        }
        MchKeyRes res = new MchKeyRes();
        res.key = userKeyMapper.getKeyById(userId);
        return res;
    }

    public MchKey getKeyByUserId(Long userId) {
        MchKey userKey = new MchKey();
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

    public MchKeyRes getKey(Long userId) {
        String key = userKeyMapper.getKeyById(userId);
        MchKeyRes res = new MchKeyRes();
        res.key = key;
        return res;
    }

    public MchKeyRes randomKey() {
        MchKeyRes res = new MchKeyRes();
        res.key = UUID.randomUUID().toString().replaceAll("-", "");
        return res;
    }
}
