package com.ylli.api.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.SysAppMapper;
import com.ylli.api.user.mapper.UserAppMapper;
import com.ylli.api.user.mapper.UserBaseMapper;
import com.ylli.api.user.model.Apps;
import com.ylli.api.user.model.SysApp;
import com.ylli.api.user.model.UserApp;
import com.ylli.api.user.model.UserAppDetail;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppService {

    @Autowired
    UserAppMapper userAppMapper;

    @Autowired
    SysAppMapper sysAppMapper;

    @Autowired
    UserBaseMapper userBaseMapper;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    public List<SysApp> createApp(Integer rate, String appName) {
        SysApp sysApp = new SysApp();
        sysApp.appName = appName;
        sysApp.rate = rate;
        sysApp.status = true;
        sysAppMapper.insertSelective(sysApp);

        return sysAppMapper.selectAll();
    }


    @Transactional
    public List<UserAppDetail> removeApp(long id, Long mchId) {

        UserApp userApp = userAppMapper.selectByPrimaryKey(id);
        if (userApp == null) {
            throw new AwesomeException(Config.ERROR_APP_NOT_FOUND);
        }
        if (userApp.mchId != mchId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_LESS);
        }

        userAppMapper.delete(userApp);
        List<UserApp> list = userAppMapper.selectAppsByMchId(mchId);
        List<UserAppDetail> details = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {

            details.add(convert(list.get(i)));
        }
        return details;
    }

    public DataList<SysApp> getSysApp(String appName, Boolean status, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<SysApp> page = (Page<SysApp>) sysAppMapper.getSysApp(appName, status);
        DataList<SysApp> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    @Transactional
    public List<SysApp> updateApp(SysApp app) {
        SysApp sysApp = sysAppMapper.selectByPrimaryKey(app.id);
        if (sysApp == null) {
            throw new AwesomeException(Config.ERROR_APP_NOT_FOUND);
        }
        modelMapper.map(app, sysApp);
        sysAppMapper.updateByPrimaryKeySelective(sysApp);
        return sysAppMapper.selectAll();
    }

    public List<UserAppDetail> setUserRate(Apps apps) {

        List<UserAppDetail> list = new ArrayList<>();
        for (int i = 0; i < apps.apps.size(); i++) {
            UserApp userApp = apps.apps.get(i);
            userAppMapper.insertSelective(userApp);

            list.add(convert(userApp));
        }
        return list;
    }

    public UserAppDetail convert(UserApp userApp) {
        UserAppDetail detail = new UserAppDetail();

        detail.appId = userApp.id;
        detail.appName = sysAppMapper.selectByPrimaryKey(userApp.appId).appName;
        detail.mchId = userApp.mchId;
        detail.mchName = userBaseMapper.selectByMchId(userApp.mchId).mchName;
        detail.rate = userApp.rate;
        detail.createTime = userApp.createTime;
        return detail;
    }
}
