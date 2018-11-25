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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

            bill.accountNo = accountNo;
            bill.accountName = accountName;
            bill.mobileNo = mobileNo;
            bill.bankNo = bankNo;
            bill.userType = userType;
            bill.accountType = accountType;
            bill.memo = memo;
            xfBillMapper.insertSelective(bill);

            bill = xfBillMapper.selectOne(bill);
            bill.orderNo = generateOrderNo(userId, bill.id);
            xfBillMapper.updateByPrimaryKeySelective(bill);
        }
        if (bill.status != XfBill.NEW) {
            //case status return
            String msg = bill.status == XfBill.ING ? "请求已接受" : bill.status == XfBill.FINISH ? "订单已完成" : "订单已取消";
            return getResJson("001", msg, null);
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
            try {
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYYMMDDhhmmss").parse(data.tradeTime).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

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

            // todo 返回数据.
            return getResJson("000", "", new Object());
        } else if (response.code.equals("99001")) {
            //查询订单
            return getResJson("002", "接口调用异常", null);
        } else {
            //通用错误返回.
            return getResJson("003", response.message, null);
        }
    }

    /**
     * code: 000 - success
     * code: 001 - 非法的订单状态
     * msg: 请求已接受
     * msg: 订单已完成
     * msg: 订单已取消
     * code: 002 - 接口调用异常
     * code: 003 - 其他
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

    @Transactional
    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        InputStream inputStream;
        OutputStream outputStream = null;
        try {
            //读取参数
            StringBuffer sb = new StringBuffer();
            inputStream = request.getInputStream();
            String s;
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
            inputStream.close();
            outputStream = response.getOutputStream();
            if (sb.length() == 0) {
                outputStream.write(getResStr("success").getBytes("UTF-8"));
                return;
            }
            //verify sign
            boolean flag = xfClient.verify(sb.toString());

            if (flag) {


                //xml to map
                /*Map<String, String> map = WXPayUtil.xmlToMap(sb.toString());
                //verify return info
                String outTradeNo = map.get("out_trade_no");
                PayTradeRecord record = payRecordMapper.selectForUpdate(outTradeNo);
                if (record == null) {
                    outputStream.write(
                            getResXml(false, "商户订单校验错误").getBytes("UTF-8"));
                    LOGGER.info(map.toString());
                    return;
                }
                //allow repeated check
                if (record.state == PayTradeRecord.FINISH) {
                    outputStream.write(getResXml(true, "OK").getBytes("UTF-8"));
                    return;
                }

                //check money
                Bill bill = billMapper.selectByPrimaryKey(record.billId);
                if (bill == null || !bill.money.equals(Integer.parseInt(map.get("total_fee")))) {
                    //金额校验失败废除流水
                    record.state = PayTradeRecord.CANCEL;
                    payRecordMapper.updateByPrimaryKeySelective(record);
                    outputStream.write(
                            getResXml(false, "金额校验错误").getBytes("UTF-8"));
                    LOGGER.info(map.toString());
                    return;
                }

                //默认支付成功
                record.state = PayTradeRecord.FINISH;
                record.payTime = Timestamp.from(Instant.now());
                record.transactionId = map.get("transaction_id");
                payRecordMapper.updateByPrimaryKeySelective(record);

                bill.state = Bill.FINISH;
                bill.modifyTime = Timestamp.from(Instant.now());
                billMapper.updateByPrimaryKeySelective(bill);

                //展期
                if (record.extensionId != 0) {
                    extensionConfirm(record);
                    outputStream.write(getResXml(true, "OK").getBytes("UTF-8"));
                    return;
                }
                //借条支付
                iouConfirm(record.iouId, outTradeNo, outputStream);
                outputStream.write(getResXml(true, "OK").getBytes("UTF-8"));
                return;*/
            }


            outputStream.write(getResStr("签名校验失败").getBytes("UTF-8"));
        } catch (Exception ex) {
            ex.getMessage();
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getResStr(String str) {
        return str.toUpperCase();
    }
}
