package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.mapper.PhoneAuthMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.PhoneAuth;
import com.ylli.api.auth.service.PasswordService;
import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.auth.service.PhoneAuthService;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.mapper.MchSubMapper;
import com.ylli.api.mch.model.MchAppDetail;
import com.ylli.api.mch.model.MchBase;
import com.ylli.api.mch.model.MchSub;
import com.ylli.api.mch.model.MchSubDto;
import com.ylli.api.model.base.DataList;
import com.ylli.api.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MchSubService {
    @Autowired
    MchSubMapper mchSubMapper;
    @Autowired
    AccountMapper accountMapper;
    @Autowired
    PhoneAuthMapper phoneAuthMapper;
    @Autowired
    PermissionService permissionService;
    @Autowired
    PhoneAuthService phoneAuthService;
    @Autowired
    PasswordService passwordService;
    @Autowired
    AppService appService;
    @Autowired
    WalletService walletService;
    @Autowired
    MchBaseService userBaseService;
    @Autowired
    MchBaseMapper mchBaseMapper;

    public Object getSubAccounts(Long mchId, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        DataList<MchSubDto> dataList = new DataList<>();
        if (mchId != null) {
            Page<MchSubDto> page = (Page<MchSubDto>) mchSubMapper.selectSubAccounts(mchId);
            dataList.dataList = page.getResult();
            return dataList;
        }
        Page<MchSubDto> page = (Page<MchSubDto>) accountMapper.getCanSubAccount();
        page.stream().forEach(item -> {
            Long id = item.mchId;
            PhoneAuth phoneAuth = phoneAuthMapper.selectByPrimaryKey(id);
            if (null != phoneAuth)
                item.phone = phoneAuth.phone;
            MchBase userBase = mchBaseMapper.selectByMchId(id);
            if (null != userBase)
                item.mchName = userBase.mchName;
        });
        dataList.dataList = page.getResult();
        return dataList;
    }

    public MchSub getBysSubId(Long mchId) {
        MchSub mchSub = new MchSub();
        mchSub.subId = mchId;
        return mchSubMapper.selectOne(mchSub);
    }

    public void addSubMch(Long[] subIds, Long mchId) {
        if(null!=subIds){
            for(int i=0,l=subIds.length;i<l;i++){
                Long subId=subIds[i];
                MchSub mchSub = new MchSub();
                mchSub.subId = subId;
                MchSub mchSubDto = mchSubMapper.selectOne(mchSub);
                if (null != mchSubDto)
                    throw new AwesomeException(Config.ERROR_MCH_SUB);
                Account accountSup = accountMapper.selectByPrimaryKey(mchId);
                Account accountSub = accountMapper.selectByPrimaryKey(subId);
                if (null == accountSub || null == accountSup)
                    throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);

                if (!rateCheck(mchId, subId)) {
                    throw new AwesomeException(Config.ERROR_MCH_SUB_RATE);
                }
                mchSub.mchId = mchId;
                mchSubMapper.insertSelective(mchSub);
            }
        }
    }

    private boolean rateCheck(Long mchId, Long subId) {
        List<MchAppDetail> mchRates = appService.getMchRate(mchId);
        List<MchAppDetail> subRates = appService.getMchRate(subId);
        for (MchAppDetail userAppDetail : mchRates) {
            for (MchAppDetail subRate : subRates) {
                if (userAppDetail.appId == subRate.appId && userAppDetail.rate >= subRate.rate) {
                    return false;
                }
            }
        }
        return true;
    }

    public void delSubMch(long subId) {
        MchSub mchSub = new MchSub();
        mchSub.subId = subId;
        MchSub chekSub = mchSubMapper.selectOne(mchSub);
        if (null == chekSub) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        mchSubMapper.delete(chekSub);
    }
}
