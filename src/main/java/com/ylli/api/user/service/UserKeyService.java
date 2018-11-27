package com.ylli.api.user.service;

import com.ylli.api.user.mapper.UserKeyMapper;
import com.ylli.api.user.model.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserKeyService {

    @Autowired
    UserKeyMapper userKeyMapper;

    @Transactional
    public void saveKey(Long userId, String secretKey) {
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
    }

    public UserKey getKeyByUserId(Long userId) {
        UserKey userKey = new UserKey();
        userKey.userId = userId;
        return userKeyMapper.selectOne(userKey);
    }
}
