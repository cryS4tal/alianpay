package com.ylli.api.user.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.UserBaseMapper;
import com.ylli.api.user.model.UserBase;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBaseService {


    @Autowired
    UserBaseMapper userBaseMapper;

    @Autowired
    SerializeUtil serializeUtil;

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
        userBase.merchantNo = serializeUtil.getCode(userBase.userType);
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
