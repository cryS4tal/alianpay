package com.ylli.api.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.UserSettlementMapper;
import com.ylli.api.user.model.UserChargeInfo;
import com.ylli.api.user.model.UserOwnInfo;
import com.ylli.api.user.model.UserSettlement;
import java.sql.Timestamp;
import java.time.Instant;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSettlementService {

    @Autowired
    UserSettlementMapper userSettlementMapper;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    public Object saveUserInfo(UserOwnInfo ownInfo) {
        UserSettlement settlement = userSettlementMapper.selectByUserId(ownInfo.userId);
        if (settlement == null) {
            settlement = new UserSettlement();
            modelMapper.map(ownInfo, settlement);
            userSettlementMapper.insertSelective(settlement);
        } else {
            modelMapper.map(ownInfo, settlement);
            settlement.modifyTime = Timestamp.from(Instant.now());
            userSettlementMapper.updateByPrimaryKeySelective(settlement);
        }
        return userSettlementMapper.selectByPrimaryKey(settlement.id);
    }


    @Transactional
    public Object saveChargeInfo(UserChargeInfo userChargeInfo) {
        UserSettlement settlement = userSettlementMapper.selectByUserId(userChargeInfo.userId);
        if (settlement == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        settlement.chargeType = userChargeInfo.chargeType;
        settlement.chargeRate = userChargeInfo.chargeRate;
        settlement.modifyTime = Timestamp.from(Instant.now());
        userSettlementMapper.updateByPrimaryKeySelective(settlement);
        return userSettlementMapper.selectByPrimaryKey(settlement.id);
    }

    public Object getUserList(Long userId, String name, String identityCard, String bankcardNumber,
                              String reservedPhone, String openBank, String subBank, Timestamp beginTime,
                              Timestamp endTime, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<UserSettlement> page = (Page<UserSettlement>) userSettlementMapper.selectByCondition(userId, name, identityCard,
                bankcardNumber, reservedPhone, openBank, subBank, beginTime, endTime);
        DataList<UserSettlement> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    public UserSettlement getUserInfo(Long userId) {
        return userSettlementMapper.selectByUserId(userId);
    }

    @Transactional
    public void removeUserInfo(long id) {
        userSettlementMapper.deleteByPrimaryKey(id);
    }
}
