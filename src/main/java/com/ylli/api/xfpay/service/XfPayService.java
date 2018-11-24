package com.ylli.api.xfpay.service;

import com.google.gson.Gson;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.service.WalletService;
import com.ylli.api.xfpay.Config;
import com.ylli.api.xfpay.mapper.XfBillMapper;
import com.ylli.api.xfpay.model.Data;
import com.ylli.api.xfpay.model.WagesPayResponse;
import com.ylli.api.xfpay.model.XfBill;
import com.ylli.api.xfpay.model.XfPaymentResponse;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class XfPayService {

    @Autowired
    XfClient xfClient;

    @Autowired
    WalletService walletService;

    @Autowired
    XfBillMapper xfBillMapper;

    @Transactional
    public Object wagesPay(Long userId, Integer amount, String accountNo, String accountName, String mobileNo,
                           String bankNo, Integer userType, Integer accountType, String memo, String orderNo) {

        Wallet wallet = walletService.getOwnWallet(userId);
        if (wallet.avaliableMoney < amount) {
            throw new AwesomeException(Config.ERROR_BALANCE_NOT_ENOUGH);
        }

        XfBill bill = new XfBill();
        bill.subNo = orderNo;
        bill = xfBillMapper.selectOne(bill);
        if (bill == null) {
            bill = new XfBill();
            bill.amount = amount;
            bill.userId = userId;
            bill.subNo = orderNo;
            bill.status = XfBill.NEW;
            xfBillMapper.insertSelective(bill);

            bill = xfBillMapper.selectOne(bill);
            bill.orderNo = generateOrderNo(userId, bill.id);
            xfBillMapper.updateByPrimaryKeySelective(bill);
        }
        if (bill.status != XfBill.NEW) {
            //case status return
            //ing msg:"请求已处理"
            //finish msg:"请求已完成"
            //cancel msg:"订单已取消"
            //todo
            return getResJson("001", "请求已处理", null);
        }

        XfPaymentResponse response = xfClient.agencyPayment(bill.orderNo, amount, accountNo, accountName, mobileNo, bankNo, userType, accountType, memo);
        /**
         * 99000 - 接口调用成功
         * 99001 - 接口调用异常
         * 其他返回码，接口调用失败，可置订单为失败
         */
        if (response.code.equals("99000")) {
            //交易成功返回订单数据
            Data data = new Gson().fromJson(response.data, Data.class);
            bill.superNo = data.tradeNo;
            //data.tradeTime 交易完成时间 暂时未记录
            if (data.status != null && data.status.toUpperCase().equals("S")) {
                bill.status = XfBill.FINISH;
            }
            if (data.status != null && data.status.toUpperCase().equals("F")) {
                bill.status = XfBill.FAIL;
            }
            if (data.status != null && data.status.toUpperCase().equals("I")) {
                bill.status = XfBill.ING;
            }
            xfBillMapper.updateByPrimaryKeySelective(bill);

        } else if (response.code.equals("99001")) {
            //查询订单


        } else {

        }


        return null;

    }

    /**
     * code: 000 - success
     * code: 001 - 订单状态异常
     */
    public String getResJson(String code, String msg, Object object) {
        WagesPayResponse response = new WagesPayResponse();
        response.code = code;
        response.message = msg;
        response.object = object;
        return new Gson().toJson(response);
    }

    /**
     * yyyyMMddHHmmss + leftPad(userId,8,0) + leftPad(billId,8,0)
     *
     * @return
     */
    public String generateOrderNo(Long userId, Long billId) {

        return new StringBuffer()
                .append(new SimpleDateFormat("yyyyMMddHHmmss").format(Date.from(Instant.now())))
                .append(StringUtils.leftPad(String.valueOf(userId), 8, "0"))
                .append(StringUtils.leftPad(String.valueOf(billId), 8, "0"))
                .toString();
    }
}
