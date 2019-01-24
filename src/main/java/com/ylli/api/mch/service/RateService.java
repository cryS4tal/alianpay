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
import com.ylli.api.mch.model.MchAgency;
import com.ylli.api.mch.model.MchRate;
import com.ylli.api.mch.model.MchRateDetail;
import com.ylli.api.mch.model.SysApp;
import static com.ylli.api.mch.service.MchAgencyService.pay;
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
            MchRate item = apps.apps.get(i);
            ServiceUtil.checkNotEmptyIgnore(apps.apps.get(i), true);
            if (sysAppMapper.selectByPrimaryKey(item.appId) == null) {
                throw new AwesomeException(Config.ERROR_APP_NOT_FOUND);
            }
            MchRate exist = new MchRate();
            exist.appId = item.appId;
            exist.mchId = item.mchId;
            exist = mchRateMapper.selectOne(exist);

            if (exist == null) {
                mchRateMapper.insertSelective(item);
            } else {
                item.id = exist.id;
                mchRateMapper.updateByPrimaryKeySelective(item);
            }

            /**
             * rate check
             * 是否是代理商？yes 更新所有 mch_agency 费率差。出现小于0 throw
             *
             * 是否子账户？ yes 更新所有 mch_agency 费率差。出现小于0 throw
             */
            // 是否是代理商？
            MchAgency sup = new MchAgency();
            sup.mchId = item.mchId;
            sup.type = pay;
            List<MchAgency> subs = mchAgencyMapper.select(sup);
            if (subs.size() > 0) {
                //item.appId =1 支付宝，= 2 微信
                //遍历每一个 子账户
                subs.stream().forEach(sub -> {
                    //更新支付宝，微信费率差

                    //获得zi账户费率信息
                    Integer subRate = getRate(sub.subId, item.appId);

                    if (item.appId == 1) {
                        //支付宝
                        sub.alipayRate = subRate - item.rate;
                        if (sub.alipayRate < 0) {
                            throw new AwesomeException(Config.ERROR_FORMAT.format(new StringBuffer("当前子账户")
                                    .append(sub.subId).append("支付宝费率")
                                    .append(String.format("%.2f", (subRate / 100.0))).append("%")
                                    .append("大于代理商").append(item.mchId).append("支付宝费率")
                                    .append(String.format("%.2f", (item.rate / 100.0))).append("%").toString()
                            ));
                        }
                    } else if (item.appId == 2) {
                        //微信
                        sub.wxRate = subRate - item.rate;
                        if (sub.wxRate < 0) {
                            throw new AwesomeException(Config.ERROR_FORMAT.format(new StringBuffer("当前子账户")
                                    .append(sub.subId).append("微信费率")
                                    .append(String.format("%.2f", (subRate / 100.0))).append("%")
                                    .append("大于代理商").append(item.mchId).append("微信费率")
                                    .append(String.format("%.2f", (item.rate / 100.0))).append("%").toString()
                            ));
                        }
                    }
                    mchAgencyMapper.updateByPrimaryKeySelective(sub);
                });
            }
            //是否是子账户
            MchAgency sub = new MchAgency();
            sub.subId = item.mchId;
            sub.type = pay;
            sub = mchAgencyMapper.selectOne(sub);
            if (sub != null) {

                //获得代理商费率信息
                Integer supRate = getRate(sub.mchId, item.appId);

                if (item.appId == 1) {
                    //支付宝
                    sub.alipayRate = item.rate - supRate;
                    if (sub.alipayRate < 0) {
                        throw new AwesomeException(Config.ERROR_FORMAT.format(new StringBuffer("当前代理商")
                                .append(sub.mchId).append("支付宝费率")
                                .append(String.format("%.2f", (supRate / 100.0))).append("%")
                                .append("小于子账户").append(item.mchId).append("支付宝费率")
                                .append(String.format("%.2f", (item.rate / 100.0))).append("%").toString()
                        ));
                    }
                } else if (item.appId == 2) {
                    //微信
                    sub.wxRate = item.rate - supRate;
                    if (sub.wxRate < 0) {
                        throw new AwesomeException(Config.ERROR_FORMAT.format(new StringBuffer("当前代理商")
                                .append(sub.mchId).append("微信费率")
                                .append(String.format("%.2f", (supRate / 100.0))).append("%")
                                .append("小于子账户").append(item.mchId).append("微信费率")
                                .append(String.format("%.2f", (item.rate / 100.0))).append("%").toString()
                        ));
                    }
                }
                mchAgencyMapper.updateByPrimaryKeySelective(sub);
            }
            list.add(convert(item));
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
