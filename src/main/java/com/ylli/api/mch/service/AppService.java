package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchAppMapper;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.mapper.SysAppMapper;
import com.ylli.api.mch.model.Apps;
import com.ylli.api.mch.model.MchApp;
import com.ylli.api.mch.model.MchAppDetail;
import com.ylli.api.mch.model.SysApp;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.service.PayService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppService {

    @Autowired
    MchAppMapper mchAppMapper;

    @Autowired
    SysAppMapper sysAppMapper;

    @Autowired
    MchBaseMapper userBaseMapper;

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
    public void removeApp(Long appId, Long mchId) {
        MchApp mchApp = new MchApp();
        mchApp.appId = appId;
        mchApp.mchId = mchId;
        mchAppMapper.delete(mchApp);
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

    public List<MchAppDetail> setUserRate(Apps apps) {

        List<MchAppDetail> list = new ArrayList<>();
        for (int i = 0; i < apps.apps.size(); i++) {
            MchApp mchApp = apps.apps.get(i);
            ServiceUtil.checkNotEmptyIgnore(apps.apps.get(i), true);
            if (sysAppMapper.selectByPrimaryKey(mchApp.appId) == null) {
                throw new AwesomeException(Config.ERROR_APP_NOT_FOUND);
            }
            MchApp exist = new MchApp();
            exist.appId = mchApp.appId;
            exist.mchId = mchApp.mchId;
            exist = mchAppMapper.selectOne(exist);
            if (exist == null) {
                mchAppMapper.insertSelective(mchApp);
            } else {
                mchApp.id = exist.id;
                mchAppMapper.updateByPrimaryKeySelective(mchApp);
            }
            list.add(convert(mchApp));
        }
        return list;
    }

    public MchAppDetail convert(MchApp mchApp) {
        MchAppDetail detail = new MchAppDetail();

        detail.id = mchApp.id;
        detail.appId = mchApp.appId;
        detail.appName = sysAppMapper.selectByPrimaryKey(mchApp.appId).appName;
        detail.mchId = mchApp.mchId;
        detail.mchName = userBaseMapper.selectByMchId(mchApp.mchId).mchName;
        detail.rate = mchApp.rate;
        detail.createTime = mchApp.createTime;
        return detail;
    }

    /**
     * 获得商户应用费率
     *
     * @return
     */
    public Integer getRate(Long mchId, Long appId) {
        MchApp mchApp = new MchApp();
        mchApp.mchId = mchId;
        mchApp.appId = appId;
        mchApp = mchAppMapper.selectOne(mchApp);
        if (mchApp == null) {
            return sysAppMapper.selectByPrimaryKey(appId).rate;
        } else {
            return mchApp.rate;
        }
    }

    /**
     * 根据 pay_type;trade_type 返回appid
     * 默认， v1.0  需要加上 payType 和 tradeType 与 sysApp 之间关联
     * 1 - 支付宝H5
     * 2 - 微信H5
     * 3 - 支付宝扫码
     * 4 - 微信扫码
     */
    public Long getAppId(String payType, String tradeType) {
        if (payType.equals(PayService.ALI) && tradeType.equals(PayService.WAP)) {
            return 1L;
        } else if (payType.equals(PayService.WX) && tradeType.equals(PayService.WAP)) {
            return 2L;
        } else if (payType.equals(PayService.ALI) && tradeType.equals(PayService.NATIVE)) {
            return 3L;
        } else if (payType.equals(PayService.WX) && tradeType.equals(PayService.NATIVE)) {
            return 4L;
        } else {
            return 0l;
        }
    }

    public Object getMchApp(Long mchId) {
        List<SysApp> list = sysAppMapper.selectAll();

        List<MchAppDetail> details = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            details.add(doSome(list.get(i), mchId));
        }
        String mchName = Optional.ofNullable(userBaseMapper.selectByMchId(mchId)).map(i ->i.mchName).orElse("");
        details.stream().forEach(i ->{
            i.mchId = mchId;
            i.mchName = mchName;
        });
        return details;
    }

    public MchAppDetail doSome(SysApp sysApp, Long mchId) {
        MchAppDetail detail = new MchAppDetail();
        MchApp mchApp = new MchApp();
        mchApp.mchId = mchId;
        mchApp.appId = sysApp.id;
        mchApp = mchAppMapper.selectOne(mchApp);
        if (mchApp == null) {
            detail.appId = sysApp.id;
            detail.appName = sysApp.appName;
            detail.rate = sysApp.rate;
            detail.isDefault = true;
        } else {
            detail.rate = mchApp.rate;
            detail.appId = mchApp.appId;
            detail.appName = sysApp.appName;
            detail.isDefault = false;
        }
        return detail;
    }
}
