package com.ylli.api.mch.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchSubMapper;
import com.ylli.api.mch.mapper.SysAppMapper;
import com.ylli.api.mch.model.MchSub;
import com.ylli.api.pay.model.MchBankPayRate;
import com.ylli.api.pay.service.BankPayService;
import com.ylli.api.pay.service.BillService;
import java.util.List;
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

        //mch_id check.
        MchSub mch = new MchSub();
        mch.subId = mchId;
        mch.type = type;
        List<MchSub> list = mchSubMapper.select(mch);
        if (list.size() > 0) {
            throw new AwesomeException(Config.ERROR_SUB_FORBIDDEN.format(list.get(0).mchId));
        }
        //sub_id check
        MchSub sub = new MchSub();
        sub.subId = subId;
        mch.type = type;
        List<MchSub> list1 = mchSubMapper.select(sub);
        if (list1.size() > 0) {
            throw new AwesomeException(Config.ERROR_SUB_BAD_REQUEST.format(list1.get(0).mchId));
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
                Integer mchAlipayRate = rateService.getRate(mchId, alipay);
                Integer subAlipayRate = rateService.getRate(subId, alipay);
                if (mchAlipayRate > subAlipayRate) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("代理商：" + mchId + " 支付宝费率(" + String.format("%.2f", (mchAlipayRate / 100.0)) + "%" + ") 大于子账户支付宝费率（" + String.format("%.2f", (subAlipayRate / 100.0)) + "）"));
                }
                //微信
                Integer mchWxRate = rateService.getRate(mchId, wx);
                Integer subWxRate = rateService.getRate(subId, wx);
                if (mchWxRate > subWxRate) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("代理商：" + mchId + " 微信费率(" + String.format("%.2f", (mchWxRate / 100.0)) + "%" + ") 大于子账户微信费率（" + String.format("%.2f", (subWxRate / 100.0)) + "）"));
                }
                exist = new MchSub();
                exist.mchId = mchId;
                exist.subId = subId;
                exist.type = type;
                mchSubMapper.insertSelective(exist);

            } else if (bankPay.intValue() == type) {
                //代付
                MchBankPayRate mchRate = bankPayService.getBankPayRate(mchId);
                MchBankPayRate subRate = bankPayService.getBankPayRate(subId);
                if (mchRate == null) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("请先设置代理商：" + mch + " 代付结算费率"));
                }
                if (subRate == null) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("请先设置子账户:" + subId + " 代付结算费率"));
                }
                if (mchRate.rate > subRate.rate) {
                    throw new AwesomeException(Config.ERROR_FORMAT.format("代理商:" + mchId + "代付费率（" + String.format("%.2f", (mchRate.rate / 100.0)) + "%）大于子账户代付费率（" + String.format("%.2f", (subRate.rate / 100.0)) + "%）"));
                }

                exist = new MchSub();
                exist.mchId = mchId;
                exist.subId = subId;
                exist.type = type;
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
}
