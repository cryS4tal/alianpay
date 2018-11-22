package com.ylli.api.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.UserAppMapper;
import com.ylli.api.user.model.UserApp;
import com.ylli.api.user.model.UserAppDetail;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAppService {

    @Autowired
    UserAppMapper userAppMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    AuthSession authSession;

    @Transactional
    public void createApp(Long userId, String appName) {
        UserApp userApp = new UserApp();
        userApp.userId = userId;
        userApp.appName = appName;
        userApp.appId = generateAppId();
        userApp.status = true;
        userAppMapper.insertSelective(userApp);
    }

    public String generateAppId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public Object getApps(Long userId, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<UserApp> page = (Page<UserApp>) userAppMapper.selectApps(userId);

        DataList<UserAppDetail> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();

        List<UserAppDetail> list = new ArrayList<>();
        //转换成appModel 元素待定
        for (int i = 0; i < page.size(); i++) {
            UserAppDetail detail = new UserAppDetail();
            UserApp userApp = page.get(i);
            modelMapper.map(userApp, detail);
            detail.nickname = accountMapper.selectByPrimaryKey(userApp.userId).nickname;
            list.add(detail);
        }
        dataList.dataList = list;
        return dataList;
    }

    public List<UserApp> getApp(Long userId) {
        UserApp userApp = new UserApp();
        userApp.userId = userId;
        List<UserApp> list = userAppMapper.select(userApp);
        return list;
    }

    @Transactional
    public void appSwitch(Long userId, String appId, Boolean status, boolean admin) {
        if (!admin && authSession.getAuthId() != userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        UserApp userApp = new UserApp();
        userApp.appId = appId;
        userApp.userId = userId;
        userApp = userAppMapper.selectOne(userApp);
        if (userApp == null) {
            throw new AwesomeException(Config.ERROR_APP_NOT_FOUND);
        }
        userApp.status = status;
        userAppMapper.updateByPrimaryKeySelective(userApp);
    }

    @Transactional
    public void removeApp(long id) {
        //todo 前置账单查询，存在进行中得app 不能删除。
        if (false) {
            throw new AwesomeException(Config.ERROR_APP_IN_USERD);
        }
        UserApp userApp = userAppMapper.selectByPrimaryKey(id);
        if (userApp == null) {
            throw new AwesomeException(Config.ERROR_APP_NOT_FOUND);
        }
        if (userApp.userId != authSession.getAuthId()) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        userAppMapper.delete(userApp);
    }


    /*public static void main(String[] args) {
        UserAppService service = new UserAppService();
        System.out.println(service.generateAppId());
    }*/
}
