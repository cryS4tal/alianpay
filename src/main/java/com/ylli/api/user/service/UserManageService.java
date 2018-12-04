package com.ylli.api.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.mapper.PhoneAuthMapper;
import com.ylli.api.auth.model.PhoneAuth;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.UserKeyMapper;
import com.ylli.api.user.mapper.UserSettlementMapper;
import com.ylli.api.user.model.MchInfo;
import com.ylli.api.user.model.MchInfoDetail;
import com.ylli.api.user.model.UserSettlement;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserManageService {


    @Autowired
    PhoneAuthMapper phoneAuthMapper;

    @Autowired
    UserKeyMapper userKeyMapper;

    @Autowired
    UserSettlementMapper userSettlementMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserSettlementMapper userSettlementMapper;

    public DataList<MchInfo> getAccountList(String phone, String mchId, int offset, int limit) {
        /**
         * 目前先支持 商户号（id）,手机号搜索。
         */
        PageHelper.offsetPage(offset, limit);
        Page<PhoneAuth> page = (Page<PhoneAuth>) phoneAuthMapper.selectByQuery(phone, mchId);

        DataList<MchInfo> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();

        List<MchInfo> list = new ArrayList<>();
        for (int i = 0; i < page.size(); i++) {
            list.add(userInfoConvert(page.get(i)));
        }
        dataList.dataList = list;
        return dataList;
    }

    public MchInfo userInfoConvert(PhoneAuth auth) {
        MchInfo info = new MchInfo();
        info.mchId = auth.id;
        info.phone = auth.phone;

        info.secret = userKeyMapper.getKeyById(auth.id);
        UserSettlement settlement = userSettlementMapper.selectByUserId(auth.id);
        info.chargeType = settlement.chargeType;
        info.chargeRate = settlement.chargeRate;
        info.createTime = auth.createTime;
        return info;
    }

    public MchInfoDetail getAccountDetail(Long mchId) {
        PhoneAuth auth = phoneAuthMapper.selectByPrimaryKey(mchId);
        if (auth == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        MchInfo info = userInfoConvert(auth);
        MchInfoDetail detail = new MchInfoDetail();
        modelMapper.map(info, detail);
        detail.settlement = userSettlementMapper.selectByUserId(auth.id);
        return detail;
    }
}
