package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchAgencyMapper;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.mapper.MchRateMapper;
import com.ylli.api.mch.mapper.SysAppMapper;
import com.ylli.api.mch.model.Apps;
import com.ylli.api.mch.model.MchRate;
import com.ylli.api.mch.model.MchRateDetail;
import com.ylli.api.mch.model.SysApp;
import com.ylli.api.model.base.DataList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RateService {

    @Autowired
    MchRateMapper mchRateMapper;

    @Autowired
    SysAppMapper sysAppMapper;

    @Autowired
    MchBaseMapper mchBaseMapper;

    @Autowired
    MchAgencyMapper mchAgencyMapper;

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
    public void removeRate(Long appId, Long mchId) {
        MchRate mchRate = new MchRate();
        mchRate.appId = appId;
        mchRate.mchId = mchId;
        mchRateMapper.delete(mchRate);
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

    @Transactional
    public List<MchRateDetail> setMchRate(Apps apps) {

        List<MchRateDetail> list = new ArrayList<>();
        for (int i = 0; i < apps.apps.size(); i++) {
            MchRate mchRate = apps.apps.get(i);
            ServiceUtil.checkNotEmptyIgnore(apps.apps.get(i), true);
            if (sysAppMapper.selectByPrimaryKey(mchRate.appId) == null) {
                throw new AwesomeException(Config.ERROR_APP_NOT_FOUND);
            }
            MchRate exist = new MchRate();
            exist.appId = mchRate.appId;
            exist.mchId = mchRate.mchId;
            exist = mchRateMapper.selectOne(exist);

            if (exist == null) {
                mchRateMapper.insertSelective(mchRate);
            } else {
                mchRate.id = exist.id;
                mchRateMapper.updateByPrimaryKeySelective(mchRate);
            }
            list.add(convert(mchRate));
        }
        return list;
    }

    public MchRateDetail convert(MchRate mchRate) {
        MchRateDetail detail = new MchRateDetail();

        detail.id = mchRate.id;
        detail.appId = mchRate.appId;
        detail.appName = sysAppMapper.selectByPrimaryKey(mchRate.appId).appName;
        detail.mchId = mchRate.mchId;
        detail.mchName = mchBaseMapper.selectByMchId(mchRate.mchId).mchName;
        detail.rate = mchRate.rate;
        detail.createTime = mchRate.createTime;
        return detail;
    }

    /**
     * 获得商户应用费率
     *
     * @return
     */
    public Integer getRate(Long mchId, Long appId) {
        MchRate mchRate = new MchRate();
        mchRate.mchId = mchId;
        mchRate.appId = appId;
        mchRate = mchRateMapper.selectOne(mchRate);
        if (mchRate == null) {
            return sysAppMapper.selectByPrimaryKey(appId).rate;
        } else {
            return mchRate.rate;
        }
    }

    /**
     * 1 - 支付宝 alipay
     * 2 - 微信 wx
     */
    public Long getAppId(String code) {
        return Optional.ofNullable(sysAppMapper.selectByCode(code)).map(i -> i.id).orElse(0L);
    }

    public Object getMchRate(Long mchId) {
        List<SysApp> list = sysAppMapper.selectAll();

        List<MchRateDetail> details = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            details.add(doSome(list.get(i), mchId));
        }
        String mchName = Optional.ofNullable(mchBaseMapper.selectByMchId(mchId)).map(i -> i.mchName).orElse("");
        details.stream().forEach(i -> {
            i.mchId = mchId;
            i.mchName = mchName;
        });
        return details;
    }

    /**
     * 查询商户费率。没有个性化设置，应用系统默认
     */
    public MchRateDetail doSome(SysApp sysApp, Long mchId) {
        MchRateDetail detail = new MchRateDetail();
        MchRate mchRate = new MchRate();
        mchRate.mchId = mchId;
        mchRate.appId = sysApp.id;
        mchRate = mchRateMapper.selectOne(mchRate);
        if (mchRate == null) {
            detail.appId = sysApp.id;
            detail.appName = sysApp.appName;
            detail.rate = sysApp.rate;
            detail.isDefault = true;
        } else {
            detail.rate = mchRate.rate;
            detail.appId = mchRate.appId;
            detail.appName = sysApp.appName;
            detail.isDefault = false;
        }
        return detail;
    }
}
