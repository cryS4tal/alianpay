package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.mch.model.Mch;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MchManageService {

    @Autowired
    AccountMapper accountMapper;


    @Autowired
    ModelMapper modelMapper;

    /*public DataList<MchInfo> getAccountList(String phone, String mchId, int offset, int limit) {
        */

    /**
     * 目前先支持 商户号（id）,手机号搜索。
     *//*
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
    }*/

    /*public MchInfo userInfoConvert(PhoneAuth auth) {
        MchInfo info = new MchInfo();
        info.mchId = auth.id;
        info.phone = auth.phone;

        info.secret = userKeyMapper.getKeyById(auth.id);

        info.createTime = auth.createTime;
        return info;
    }*/

    /*public MchInfoDetail getAccountDetail(Long mchId) {
        PhoneAuth auth = phoneAuthMapper.selectByPrimaryKey(mchId);
        if (auth == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        MchInfo info = userInfoConvert(auth);
        MchInfoDetail detail = new MchInfoDetail();
        modelMapper.map(info, detail);
        //detail.settlement = userSettlementMapper.selectByUserId(auth.id);
        return detail;
    }*/
    public Object mchList(String phone, String mchId, String mchName, Integer auditState, String mchState, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<Mch> page = (Page<Mch>) accountMapper.selectByQuery(phone, mchId, mchName, auditState, mchState);


        return null;
    }
}
