package com.ylli.api.mch.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchSubMapper;
import com.ylli.api.mch.mapper.SysAppMapper;
import com.ylli.api.mch.model.MchSub;
import com.ylli.api.pay.model.MchBankPayRate;
import com.ylli.api.pay.service.BankPayService;
import com.ylli.api.pay.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MchSubService {

    @Autowired
    MchSubMapper mchSubMapper;

    @Autowired
    SysAppMapper sysAppMapper;

    @Autowired
    RateService rateService;

    @Autowired
    BankPayService bankPayService;

    @Autowired
    BillService billService;

    public static final Integer pay = 1;
    public static final Integer bankPay = 2;

    @Transactional
    public void addSub(Long mchId, Long subId, Integer type) {

        if (mchId.longValue() == subId.longValue()) {
            throw new AwesomeException(Config.ERROR_FORMAT.format("代理商不能设置自己为子账户"));
        }

        //mch_id check.
        MchSub mch = new MchSub();
        mch.subId = mchId;
        mch.type = type;
        mch = mchSubMapper.selectOne(mch);
        if (mch != null) {
            throw new AwesomeException(Config.ERROR_SUB_FORBIDDEN.format(mch.mchId));
        }
        //sub_id check
        MchSub sub = new MchSub();
        sub.subId = subId;
        sub.type = type;
        sub = mchSubMapper.selectOne(sub);
        if (sub != null) {
            throw new AwesomeException(Config.ERROR_SUB_BAD_REQUEST.format(sub.mchId));
        }

        MchSub exist = new MchSub();
        exist.mchId = mchId;
        exist.subId = subId;
        exist.type = type;
        exist = mchSubMapper.selectOne(exist);
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
                exist = new MchSub();
                exist.mchId = mchId;
                exist.subId = subId;
                exist.type = type;
                exist.alipayRate = subAlipayRate - supAlipayRate;
                exist.wxRate = subWxRate - supWxRate;
                mchSubMapper.insertSelective(exist);

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

                exist = new MchSub();
                exist.mchId = mchId;
                exist.subId = subId;
                exist.type = type;
                exist.bankRate = subRate.rate - supRate.rate;
                mchSubMapper.insertSelective(exist);

            } else {
                throw new AwesomeException(Config.ERROR_FORMAT.format("代理商类型错误"));
            }
        }
    }


    public MchSub getPaySupper(Long mchId) {
        MchSub mchSub = new MchSub();
        mchSub.subId = mchId;
        mchSub.type = pay;
        return mchSubMapper.selectOne(mchSub);
    }

    public MchSub getBankSupper(Long mchId) {
        MchSub mchSub = new MchSub();
        mchSub.subId = mchId;
        mchSub.type = bankPay;
        return mchSubMapper.selectOne(mchSub);
    }
}
