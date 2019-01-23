package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchAgencyMapper;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.mapper.SysAppMapper;
import com.ylli.api.mch.model.IsAgency;
import com.ylli.api.mch.model.MchAgency;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.mapper.MchBankPayRateMapper;
import com.ylli.api.pay.model.MchBankPayRate;
import com.ylli.api.pay.service.BankPayService;
import com.ylli.api.pay.service.BillService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MchAgencyService {

    @Autowired
    MchAgencyMapper mchAgencyMapper;

    @Autowired
    SysAppMapper sysAppMapper;

    @Autowired
    RateService rateService;

    @Autowired
    BankPayService bankPayService;

    @Autowired
    MchBaseMapper mchBaseMapper;

    @Autowired
    MchBankPayRateMapper mchBankPayRateMapper;

    @Autowired
    BillService billService;

    @Autowired
    AccountMapper accountMapper;

    public static final Integer pay = 1;
    public static final Integer bankPay = 2;

    @Transactional
    public void addSub(Long mchId, Long subId, Integer type) {

        if (mchId.longValue() == subId.longValue()) {
            throw new AwesomeException(Config.ERROR_FORMAT.format("代理商不能设置自己为子账户"));
        }
        if (accountMapper.selectByPrimaryKey(mchId) == null) {
            throw new AwesomeException(Config.ERROR_FORMAT.format("代理商" + mchId + "账户不存在"));
        }
        if (accountMapper.selectByPrimaryKey(subId) == null) {
            throw new AwesomeException(Config.ERROR_FORMAT.format("子账户" + subId + "账户不存在"));
        }
        //mch_id check.
        MchAgency mch = new MchAgency();
        mch.subId = mchId;
        mch.type = type;
        mch = mchAgencyMapper.selectOne(mch);
        if (mch != null) {
            throw new AwesomeException(Config.ERROR_SUB_FORBIDDEN.format(mch.mchId));
        }
        //sub_id check
        MchAgency sub = new MchAgency();
        sub.subId = subId;
        sub.type = type;
        sub = mchAgencyMapper.selectOne(sub);
        if (sub != null) {
            throw new AwesomeException(Config.ERROR_SUB_BAD_REQUEST.format(sub.mchId));
        }

        MchAgency exist = new MchAgency();
        exist.mchId = mchId;
        exist.subId = subId;
        exist.type = type;
        exist = mchAgencyMapper.selectOne(exist);
        if (exist == null) {
            if (pay.intValue() == type) {
                //支付
                Long alipay = sysAppMapper.selectByCode("alipay").id;
                Long wx = sysAppMapper.selectByCode("wx").id;

                //支付宝
                Integer supAlipayRate = rateService.getRate(mchId, alipay);
                Integer subAlipayRate = rateService.getRate(subId, alipay);
                if (supAlipayRate > subAlipayRate) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("代理商：" + mchId + " 支付宝费率(" + String.format("%.2f", (supAlipayRate / 100.0)) + "%" + ") 大于子账户支付宝费率（" + String.format("%.2f", (subAlipayRate / 100.0)) + "%）"));
                }
                //微信
                Integer supWxRate = rateService.getRate(mchId, wx);
                Integer subWxRate = rateService.getRate(subId, wx);
                if (supWxRate > subWxRate) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("代理商：" + mchId + " 微信费率(" + String.format("%.2f", (supWxRate / 100.0)) + "%" + ") 大于子账户微信费率（" + String.format("%.2f", (subWxRate / 100.0)) + "%）"));
                }
                exist = new MchAgency();
                exist.mchId = mchId;
                exist.subId = subId;
                exist.type = type;
                exist.alipayRate = subAlipayRate - supAlipayRate;
                exist.wxRate = subWxRate - supWxRate;
                mchAgencyMapper.insertSelective(exist);

            } else if (bankPay.intValue() == type) {
                //代付
                MchBankPayRate supRate = bankPayService.getBankPayRate(mchId);
                MchBankPayRate subRate = bankPayService.getBankPayRate(subId);
                if (supRate == null) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("请先设置代理商：" + mch + " 代付结算费率"));
                }
                if (subRate == null) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("请先设置子账户:" + subId + " 代付结算费率"));
                }
                if (supRate.rate > subRate.rate) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("代理商:" + mchId + "代付费率（" + String.format("%.2f", (supRate.rate / 100.0)) + "%）大于子账户代付费率（" + String.format("%.2f", (subRate.rate / 100.0)) + "%）"));
                }

                exist = new MchAgency();
                exist.mchId = mchId;
                exist.subId = subId;
                exist.type = type;
                exist.bankRate = subRate.rate - supRate.rate;
                mchAgencyMapper.insertSelective(exist);

            } else {
                throw new AwesomeException(Config.ERROR_FORMAT.format("代理商类型错误"));
            }
        }
    }


    public MchAgency getPaySupper(Long mchId) {
        MchAgency mchAgency = new MchAgency();
        mchAgency.subId = mchId;
        mchAgency.type = pay;
        return mchAgencyMapper.selectOne(mchAgency);
    }

    public MchAgency getBankSupper(Long mchId) {
        MchAgency mchAgency = new MchAgency();
        mchAgency.subId = mchId;
        mchAgency.type = bankPay;
        return mchAgencyMapper.selectOne(mchAgency);
    }


    public Object agencyList(Integer type, Long mchId, Long subId, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<MchAgency> page = (Page<MchAgency>) mchAgencyMapper.agencyList(type, mchId, subId);

        DataList<MchAgency> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();

        Long alipay = sysAppMapper.selectByCode("alipay").id;
        Long wx = sysAppMapper.selectByCode("wx").id;
        page.stream().forEach(item -> {
            item.mchName = mchBaseMapper.selectByMchId(item.mchId).mchName;
            item.subName = mchBaseMapper.selectByMchId(item.subId).mchName;

            item.supAlipayRate = rateService.getRate(item.mchId, alipay);
            item.subAlipayRate = rateService.getRate(item.subId, alipay);

            item.supWxRate = rateService.getRate(item.mchId, wx);
            item.subWxRate = rateService.getRate(item.subId, wx);

            item.supRate = Optional.ofNullable(mchBankPayRateMapper.selectByMchId(item.mchId)).map(i -> i.rate).orElse(0);
            item.subRate = Optional.ofNullable(mchBankPayRateMapper.selectByMchId(item.subId)).map(i -> i.rate).orElse(0);

        });
        dataList.dataList = page;
        return dataList;
    }

    @Transactional
    public void delete(Long id) {
        mchAgencyMapper.deleteByPrimaryKey(id);
    }

    public boolean regPay(List<Long> mchIds, long authId) {
        MchAgency mchAgency = new MchAgency();
        mchAgency.mchId = authId;
        mchAgency.type = pay;
        List<Long> subs = mchAgencyMapper.select(mchAgency).stream().map(i -> i.subId).collect(Collectors.toList());
        subs.add(authId);
        if (subs.containsAll(mchIds)) {
            return true;
        }
        return false;
    }

    public boolean regBankPay(List<Long> mchIds, long authId) {
        MchAgency mchAgency = new MchAgency();
        mchAgency.mchId = authId;
        mchAgency.type = bankPay;
        List<Long> subs = mchAgencyMapper.select(mchAgency).stream().map(i -> i.subId).collect(Collectors.toList());
        subs.add(authId);
        if (subs.containsAll(mchIds)) {
            return true;
        }
        return false;
    }


    public Object isAgency(long authId, Integer type) {
        IsAgency isAgency = new IsAgency();

        MchAgency mchAgency = new MchAgency();
        mchAgency.mchId = authId;
        mchAgency.type = type;
        List<MchAgency> list = mchAgencyMapper.select(mchAgency);
        if (list.size() == 0) {
            isAgency.isAgency = false;
        } else {
            isAgency.isAgency = true;
            isAgency.subIds = list.stream().map(i ->i.subId).collect(Collectors.toList());
        }
        return isAgency;
    }
}
