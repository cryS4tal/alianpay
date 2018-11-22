package com.ylli.api.auth.service;

import com.ylli.api.auth.Config;
import com.ylli.api.auth.mapper.AccountPasswordMapper;
import com.ylli.api.auth.model.AccountPassword;
import com.ylli.api.base.exception.AwesomeException;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PwdService {

    @Autowired
    AccountPasswordMapper accountPasswordMapper;

    @Transactional
    public void forgetPwd(Long userId, String oldPwd, String newPwd) {
        AccountPassword password = accountPasswordMapper.selectByPrimaryKey(userId);
        if (password == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        if (!BCrypt.checkpw(oldPwd, password.password)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
        password.password = BCrypt.hashpw(newPwd, BCrypt.gensalt());
        password.modifyTime = Timestamp.from(Instant.now());
        accountPasswordMapper.updateByPrimaryKeySelective(password);
    }
}
