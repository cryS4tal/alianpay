package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.mapper.PasswordMapper;
import com.ylli.api.auth.mapper.PhoneAuthMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.Password;
import com.ylli.api.auth.model.PhoneAuth;
import com.ylli.api.auth.service.PasswordService;
import com.ylli.api.auth.service.PhoneAuthService;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchAgentMapper;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.model.*;
import com.ylli.api.model.base.DataList;
import com.ylli.api.sys.model.SysChannel;
import com.ylli.api.sys.service.ChannelService;
import com.ylli.api.wallet.service.WalletService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MchManageService {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    ChannelService channelService;

    @Autowired
    PasswordMapper passwordMapper;

    @Autowired
    PasswordService passwordService;
    @Autowired
    AppService appService;
    @Autowired
    PhoneAuthService phoneAuthService;
    @Autowired
    MchBaseService mchBaseService;
    @Autowired
    PhoneAuthMapper phoneAuthMapper;
    @Autowired
    MchAgentMapper mchAgentMapper;
    @Autowired
    MchBaseMapper mchBaseMapper;

    public Object mchList(String phone, String mchId, String mchName, Integer auditState, String mchState, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<Mch> page = (Page<Mch>) accountMapper.selectByQuery(phone, mchId, mchName, auditState, mchState);
        DataList<Mch> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        page.stream().forEach(item -> {
            item.money = walletService.getOwnWallet(item.mchId).total;
            SysChannel channel = channelService.getCurrentChannel(item.mchId);
            item.channelId = channel.id;
            item.channelName = channel.name;
        });
        dataList.dataList = page;
        return dataList;
    }

    @Transactional
    public void mchEnable(Long mchId, Boolean open) {
        Account account = accountMapper.selectByPrimaryKey(mchId);
        if (account == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        account.state = open ? Account.STATE_ENABLE : Account.STATE_DISABLE;
        accountMapper.updateByPrimaryKeySelective(account);
    }

    @Transactional
    public void resetPwd(Long mchId, String pwd) {
        Account account = accountMapper.selectByPrimaryKey(mchId);
        if (account == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        if (account.state.equals(Account.STATE_DISABLE)) {
            throw new AwesomeException(Config.ERROR_MCH_DISABLE);
        }
        Password password = passwordMapper.selectByPrimaryKey(mchId);
        if (password == null) {
            passwordService.init(mchId, pwd);
        } else {
            password.password = BCrypt.hashpw(pwd, BCrypt.gensalt());
            password.modifyTime = Timestamp.from(Instant.now());
            passwordMapper.updateByPrimaryKeySelective(password);
        }
    }

    @Transactional
    public void createAgent(String phone, String password, List<MchApp> userApps, String mchName) {
        Account account = phoneAuthService.getByPhone(phone);
        if (account != null) {
            throw new AwesomeException(com.ylli.api.auth.Config.ERROR_PHONE_NAME);
        }
        account = phoneAuthService.create(phone);
        Long id = account.id;
        passwordService.init(id, password);
        walletService.init(id);
        mchBaseService.init(id, mchName);
        userApps.stream().forEach(u -> {
            u.mchId = id;
        });
        Apps apps = new Apps();
        apps.apps = userApps;
        appService.setUserRate(apps);
        MchAgent agent = new MchAgent();
        agent.mchId = id;
        agent.mchName = mchName;
        agent.linkPhone = phone;
        mchAgentMapper.insertSelective(agent);
    }

    @Transactional
    public void updateAgent(String phone, String password, List<MchApp> userApps, String mchName, Long mchId) {
        MchAgent agent = new MchAgent();
        agent.mchId = mchId;
        MchAgent checkAgent = mchAgentMapper.selectOne(agent);
        if (null == checkAgent)
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);

        if (!phone.equals(checkAgent.linkPhone)) {
            PhoneAuth phoneAuth = new PhoneAuth();
            phoneAuth.id = mchId;
            phoneAuth.phone = phone;
            phoneAuthMapper.updateByPrimaryKeySelective(phoneAuth);
            agent.linkPhone = phone;
        }
        if (!Strings.isNullOrEmpty(password)) {
            Password pwd = new Password();
            pwd.id = mchId;
            pwd.password = BCrypt.hashpw(password, BCrypt.gensalt());
            passwordMapper.updateByPrimaryKeySelective(pwd);
        }
        if (!mchName.equals(checkAgent.mchName)) {
            MchBase userBase = new MchBase();
            userBase.id = mchId;
            userBase.linkPhone = phone;
            userBase.mchName = mchName;
            mchBaseMapper.updateByPrimaryKeySelective(userBase);
            agent.mchName = mchName;
        }
        mchAgentMapper.updateByPrimaryKeySelective(agent);
        Apps apps = new Apps();
        apps.apps = userApps;
        appService.setUserRate(apps);
    }

    public Object getAgents(int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<MchAgentDto> page = (Page<MchAgentDto>)mchAgentMapper.getAgents();
        page.stream().forEach(item -> {
            List<MchAppDetail> details = (List<MchAppDetail>) appService.getMchApp(item.mchId);
            item.userAppDetail = details;
        });
        DataList<MchAgentDto> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page.getResult();
        return dataList;
    }
}
