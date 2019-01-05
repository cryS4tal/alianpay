package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.ylli.api.mch.service.AppService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.wallet.service.WalletService;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HRJFService {

    @Autowired
    BillService billService;

    @Autowired
    HRJFClient hrjfClient;

    @Autowired
    BillMapper billMapper;

    @Autowired
    AppService appService;

    @Autowired
    WalletService walletService;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {

        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        String value = (new BigDecimal(money).divide(new BigDecimal(100))).toString();

        String str = hrjfClient.createOrder(value, bill.sysOrderId, redirectUrl, reserve, mchId);

        return str;

    }

    @Transactional
    public String payNotify(String orderid, String opstate, String ovalue, String sign, String sysorderid, String systime, String attach, String msg) throws Exception {
        if (Strings.isNullOrEmpty(orderid) || Strings.isNullOrEmpty(opstate) || Strings.isNullOrEmpty(ovalue)
                || Strings.isNullOrEmpty(sign) || Strings.isNullOrEmpty(sysorderid) || Strings.isNullOrEmpty(systime)) {
            return "opstate=-1";
        }
        boolean flag = hrjfClient.signVerify(orderid, opstate, ovalue, sign);
        if (flag) {
            Bill bill = billService.selectBySysOrderId(orderid);
            if (bill == null) {
                return "opstate=-1";
            }

            if (opstate.equals("0") || opstate.equals("-3")) {
                if (bill.status != Bill.FINISH) {
                    bill.superOrderId = sysorderid;
                    bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYY/MM/DD hh:mm:ss").parse(systime).getTime());
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    bill.status = Bill.FINISH;
                    //todo msg暂时先记录实际交易金额/元 两位小数
                    bill.msg = ovalue;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money - bill.payCharge);
                }

            } else {
                bill.superOrderId = sysorderid;
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYY/MM/DD hh:mm:ss").parse(systime).getTime());
                bill.status = Bill.FAIL;
                //todo msg暂时先记录实际交易金额/元 两位小数
                bill.msg = ovalue;
                //bill.msg = msg;
                billMapper.updateByPrimaryKeySelective(bill);
            }

            //加入异步通知下游商户系统
            //params jsonStr.
            String params = payService.generateRes(
                    bill.money.toString(),
                    bill.mchOrderId,
                    bill.sysOrderId,
                    bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                    bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                    bill.reserve);

            payClient.sendNotify(bill.id, bill.notifyUrl, params, true);

            /*if (bill.isSuccess != null && bill.isSuccess) {
                return "opstate=0";
            } else {
                //隐藏真实返回.待商户系统返回success再做正确处理
                return "";
            }*/
            return "opstate=0";
        } else {
            return "opstate=-2";
        }
    }
}
