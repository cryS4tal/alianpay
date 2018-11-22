package com.ylli.api.auth.service;

import com.ylli.api.auth.Config;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.mapper.PhoneAuthMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.PhoneAuth;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.CheckPhone;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by RexQian on 2017/4/21.
 */
@Service
public class PhoneAuthService {

    @Autowired
    PhoneAuthMapper phoneAuthMapper;

    @Autowired
    AccountService accountService;

    @Autowired
    ModelMapper modelMapper;

    //@Autowired
    //OAuth2WechatMapper oAuth2WechatMapper;

    @Autowired
    AccountMapper accountMapper;

    @Transactional
    public Account create(String phone) {
        if (!CheckPhone.isSimplePhone(phone)) {
            throw new AwesomeException(Config.ERROR_INVALID_PHONE);
        }
        Account account = accountService.create(phone, null);
        //bindPhone(account.id, phone);
        create(account.id, phone);
        return account;
    }

    @Transactional
    public PhoneAuth create(Long id, String phone) {
        PhoneAuth auth = new PhoneAuth();
        auth.id = id;
        auth.phone = phone;
        phoneAuthMapper.insertSelective(auth);
        return phoneAuthMapper.selectByPrimaryKey(id);
    }

    /*@Transactional
    public Long bindPhone(long accountId, String phone) {

        */

    /**
     * 微信用户第一次进入，绑定手机（此时手机应该没有绑定），可以任意绑定已存在手机，新注册手机
     * 已绑定手机用户更改绑定手机，只允许更改未被绑定的手机
     *//*
        PhoneAuth orgPhone = phoneAuthMapper.selectByPrimaryKey(accountId);

        PhoneAuth phoneAuth = new PhoneAuth();
        phoneAuth.phone = phone;
        phoneAuth = phoneAuthMapper.selectOne(phoneAuth);

        if (orgPhone == null) {
            //无限制
            if (phoneAuth == null) {
                PhoneAuth insert = new PhoneAuth();
                insert.id = accountId;
                insert.phone = phone;
                phoneAuthMapper.insertSelective(insert);
                return accountId;
            } else {
                //解绑之前的微信，重新绑定现在的微信指向phone.id
                //OAuth2Wechat orgWx = oAuth2WechatMapper.selectByPrimaryKey(phoneAuth.id);
                oAuth2WechatMapper.deleteByPrimaryKey(phoneAuth.id);
                oAuth2WechatMapper.updateId(phoneAuth.id, accountId);

                Account orgAcc = accountMapper.selectByPrimaryKey(phoneAuth.id);
                Account nowAcc = accountMapper.selectByPrimaryKey(accountId);
                orgAcc.nickname = nowAcc.nickname;
                orgAcc.avatar = nowAcc.avatar;
                accountMapper.updateByPrimaryKeySelective(orgAcc);
                accountMapper.deleteByPrimaryKey(nowAcc);

                return phoneAuth.id;
            }
        } else {
            //只允许绑定未绑定过的手机
            if (phoneAuth != null) {
                throw new AwesomeException(Config.ERROR_PHONE_BINDED);
            }
            orgPhone.phone = phone;
            phoneAuthMapper.updateByPrimaryKeySelective(orgPhone);
            return accountId;
        }
    }*/

   /* public void unBindPhone(long accountId, String phone) {
        PhoneAuth phoneAuth = phoneAuthMapper.selectByPrimaryKey(accountId);
        if (phoneAuth == null) {
            throw new AwesomeException(Config.ERROR_NOT_BIND_PHONE);
        }
        if (!phoneAuth.phone.equals(phone)) {
            throw new AwesomeException(Config.ERROR_PHONE_NOT_MATCH);
        }
        phoneAuthMapper.deleteByPrimaryKey(accountId);
    }*/
    public Account getByPhone(String phone) {
        PhoneAuth phoneAuth = new PhoneAuth();
        phoneAuth.phone = phone;
        phoneAuth = phoneAuthMapper.selectOne(phoneAuth);
        if (phoneAuth == null) {
            return null;
        }
        return accountService.getById(phoneAuth.id);
    }

    public boolean isPhoneBind(long accountId) {
        return phoneAuthMapper.selectByPrimaryKey(accountId) != null;
    }

    public PhoneAuth getByAccountId(long accountId) {
        return phoneAuthMapper.selectByPrimaryKey(accountId);
    }
}
