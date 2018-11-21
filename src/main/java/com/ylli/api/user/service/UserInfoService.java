package com.ylli.api.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.UserInfoMapper;
import com.ylli.api.user.model.UserChargeInfo;
import com.ylli.api.user.model.UserInfo;
import com.ylli.api.user.model.UserOwnInfo;
import java.sql.Timestamp;
import java.time.Instant;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    public Object saveUserInfo(UserOwnInfo ownInfo) {
        UserInfo userInfo = userInfoMapper.selectByUserId(ownInfo.userId);
        if (userInfo == null) {
            userInfo = new UserInfo();
            modelMapper.map(ownInfo, userInfo);
            userInfoMapper.insertSelective(userInfo);
        } else {
            modelMapper.map(ownInfo, userInfo);
            userInfo.modifyTime = Timestamp.from(Instant.now());
            userInfoMapper.updateByPrimaryKeySelective(userInfo);
        }
        return userInfoMapper.selectByPrimaryKey(userInfo.id);
    }


    @Transactional
    public Object saveChargeInfo(UserChargeInfo userChargeInfo) {
        UserInfo userInfo = userInfoMapper.selectByUserId(userChargeInfo.userId);
        if (userInfo == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        modelMapper.map(userChargeInfo, userInfo);
        userInfo.modifyTime = Timestamp.from(Instant.now());
        userInfoMapper.updateByPrimaryKeySelective(userInfo);
        return userInfoMapper.selectByPrimaryKey(userInfo.id);
    }

    public Object getUserList(Long userId, String name, String identityCard, String bankcardNumber,
                              String reservedPhone, String openBank, String subBank, Timestamp beginTime,
                              Timestamp endTime, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<UserInfo> page = (Page<UserInfo>) userInfoMapper.selectByCondition(userId, name, identityCard,
                bankcardNumber, reservedPhone, openBank, subBank, beginTime, endTime);
        DataList<UserInfo> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    public Object getUserInfo(Long userId) {
        return userInfoMapper.selectByUserId(userId);
    }
}
