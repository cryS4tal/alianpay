package com.ylli.api.auth.service;

import com.google.common.base.Strings;
import com.ylli.api.auth.Config;
import com.ylli.api.auth.mapper.PasswordMapper;
import com.ylli.api.auth.model.Password;
import com.ylli.api.base.exception.AwesomeException;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordService {

    @Autowired
    PasswordMapper passwordMapper;

    @Transactional
    public void forgetPwd(Long userId, String oldPwd, String newPwd) {
        Password password = passwordMapper.selectByPrimaryKey(userId);
        if (password == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        if (!BCrypt.checkpw(oldPwd, password.password)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
        password.password = BCrypt.hashpw(newPwd, BCrypt.gensalt());
        password.modifyTime = Timestamp.from(Instant.now());
        passwordMapper.updateByPrimaryKeySelective(password);
    }

    @Transactional
    public Password init(Long id, String pwd) {
        Password password = new Password();
        password.id = id;
        password.password = BCrypt.hashpw(pwd, BCrypt.gensalt());
        passwordMapper.insertSelective(password);
        return passwordMapper.selectByPrimaryKey(password.id);
    }


    public void checkpw(Long id, String pwd) {
        Password password = passwordMapper.selectByPrimaryKey(id);

        if (Strings.isNullOrEmpty(pwd) || !BCrypt.checkpw(pwd, password.password)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
    }
}
