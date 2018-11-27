package com.ylli.api.xfpay.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ucf.sdk.UcfForOnline;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.user.model.UserBase;
import com.ylli.api.user.model.UserKey;
import com.ylli.api.user.service.UserBaseService;
import com.ylli.api.user.service.UserKeyService;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.service.WalletService;
import com.ylli.api.xfpay.Config;
import com.ylli.api.xfpay.mapper.XfBillMapper;
import com.ylli.api.xfpay.model.CreditPay;
import com.ylli.api.xfpay.model.Data;
import com.ylli.api.xfpay.model.WagesPayResponse;
import com.ylli.api.xfpay.model.XfBill;
import com.ylli.api.xfpay.model.XfPaymentResponse;
import com.ylli.api.xfpay.util.SignUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class XfPayService {

    private static Logger LOGGER = LoggerFactory.getLogger(XfPayService.class);

    @Autowired
    XfClient xfClient;

    @Autowired
    WalletService walletService;

    @Autowired
    XfBillMapper xfBillMapper;

    @Autowired
    UserBaseService userBaseService;

    @Autowired
    UserKeyService userKeyService;

    @Value("${xf.pay.mer_pri_key}")
    public String mer_pri_key;

    @Value("${xf.pay.xf_pub_key}")
    public String xf_pub_key;

    @Transactional
    public Object wagesPay(Long userId, Integer amount, String accountNo, String accountName, String mobileNo,
                           String bankNo, Integer userType, Integer accountType, String memo, String orderNo) throws Exception {

        if (amount == null) {
            return getResJson("A001", "金额不能为空", null);
        }
        if (Strings.isNullOrEmpty(accountNo)) {
            return getResJson("A001", "银行卡号不能为空", null);
        }
        if (Strings.isNullOrEmpty(accountName)) {
            return getResJson("A001", "持卡人姓名不能为空", null);
        }
        if (Strings.isNullOrEmpty(bankNo)) {
            return getResJson("A001", "银行编码不能为空", null);
        }
        if (userType == null) {
            return getResJson("A001", "用户类型不能为空", null);
        }
        if (orderNo == null) {
            return getResJson("A001", "商户订单号不能为空", null);
        }

        Wallet wallet = walletService.getOwnWallet(userId);
        if (wallet.avaliableMoney < amount) {
            //余额不足
            return getResJson("A002", "余额不足", null);
            //throw new AwesomeException(Config.ERROR_BALANCE_NOT_ENOUGH);
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
            //默认规则
            bill.accountType = accountType == null ? (userType == 2 ? 4 : 1) : accountType;
            bill.memo = memo;
            xfBillMapper.insertSelective(bill);

            bill = xfBillMapper.selectOne(bill);
            bill.orderNo = generateOrderNo(userId, bill.id);
            xfBillMapper.updateByPrimaryKeySelective(bill);

            //钱包金额变更
            //wallet.avaliableMoney = wallet.avaliableMoney - amount;
            //wallet.abnormalMoney = wallet.abnormalMoney + amount;
            boolean flag = walletService.preOrder(userId, amount);
            if (flag) {
                return getResJson("A003", "用户钱包数据异常，请联系管理员", null);
            }
        }
        if (bill.status != XfBill.NEW) {
            //case status return
            String msg = bill.status == XfBill.ING ? "请求已接受" : bill.status == XfBill.FINISH ? "订单已完成" : "订单已取消";
            return getResJson("A004", msg, null);
        }

        String str = xfClient.agencyPayment(bill.orderNo, amount, accountNo, accountName, mobileNo, bankNo, userType, accountType, memo);

        XfPaymentResponse response = new Gson().fromJson(str, XfPaymentResponse.class);
        //加密后的业务数据
        String bizData = UcfForOnline.decryptData(str, mer_pri_key);

        /**
         * 99000 - 接口调用成功
         * 99001 - 接口调用异常
         * 其他返回码，接口调用失败，可置订单为失败
         */
        if (response.code.equals("99000")) {
            //交易成功返回订单数据
            Data data = new Gson().fromJson(bizData, Data.class);

            bill.superNo = data.tradeNo;
            if (data.tradeTime != null) {
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYYMMDDhhmmss").parse(data.tradeTime).getTime());
            }

            if (data.status != null && data.status.toUpperCase().equals("S")) {
                walletService.finishOrder(bill.id, amount);

                bill.status = XfBill.FINISH;
                bill.resCode = data.resCode;
                bill.resMessage = data.resMessage;
                xfBillMapper.updateByPrimaryKeySelective(bill);

                return getResJson("A000", "交易成功", null);
            }
            if (data.status != null && data.status.toUpperCase().equals("F")) {
                walletService.failedOrder(bill.id, amount);

                bill.status = XfBill.FAIL;
                bill.resCode = data.resCode;
                bill.resMessage = data.resMessage;
                xfBillMapper.updateByPrimaryKeySelective(bill);

                return getResJson("A005", data.resMessage, null);
            }
            if (data.status != null && data.status.toUpperCase().equals("I")) {
                bill.status = XfBill.ING;

                bill.resCode = data.resCode;
                bill.resMessage = data.resMessage;
                xfBillMapper.updateByPrimaryKeySelective(bill);

                return getResJson("A006", "交易进行中", null);
            }

            //兼容status 没有返回值.
            //具体情况未知
            return getResJson("A999", data.resCode + data.resMessage, null);

        } else if (response.code.equals("99001")) {

            return getResJson(response.code, response.message, null);
        } else {
            //通用错误返回.
            //具体原因 @see 先锋支付网关返回码.
            //对下游服务商隐藏先锋返回message，统一返回请求失败
            return getResJson(response.code, "请求失败", null);
        }
    }

    /**
     * code:
     * A000 -  交易成功
     * A001 -  参数非法.  具体见 message
     * A002 -  余额不足.
     * A003 -  用户余额数据异常（丢失）
     * A004 -  订单重复提交. 具体见 message
     * A005 -  业务处理失败（并非一定是交易失败） 具体见message.
     * A006 -  交易正在进行中
     * <p>
     * A100 -  商户不存在
     * A101 -  商户资格待审核
     * A102 -  请先上传商户私钥
     * <p>
     * A200 -  签名校验错误
     * <p>
     * A999 -  未知错误.
     * <p>
     * A为系统自己业务逻辑处理
     * ——————————————————————————
     * 其余 参加先锋业务返回码 与 网关返回码
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
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "text/html;charset=UTF-8");

        //接收参数
        Map parameters = request.getParameterMap();// 保存request中参数的临时变量
        Map<String, String> params = new HashMap<String, String>();// 参与签名业务字段集合
        Iterator paiter = parameters.keySet().iterator();
        while (paiter.hasNext()) {
            String key = paiter.next().toString();
            String[] values = (String[]) parameters.get(key);
            params.put(key, values[0]);
        }

        PrintWriter writer = response.getWriter();
        try {
            //报文参数验证签名
            if (UcfForOnline.verify(JSONObject.toJSONString(params), xf_pub_key)) {

                String code = params.get("code").toString();

                //解密业务数据
                String decryptData = UcfForOnline.decryptData(JSONObject.toJSONString(params), mer_pri_key);

                //服务调用成功
                if ("99000".equals(code) && !StringUtils.isEmpty(params.get("data").toString())) {

                    Data data = new Gson().fromJson(decryptData, Data.class);
                    XfBill xfBill = new XfBill();
                    xfBill.orderNo = data.merchantNo;
                    xfBill = xfBillMapper.selectOne(xfBill);
                    if (xfBill == null) {
                        writer.write("订单不存在");
                        return;
                    }
                    if (data.tradeTime != null) {
                        xfBill.tradeTime = new Timestamp(new SimpleDateFormat("YYYYMMDDhhmmss").parse(data.tradeTime).getTime());
                    }

                    if (data.status != null && data.status.toUpperCase().equals("S")) {
                        walletService.finishOrder(xfBill.id, xfBill.amount);

                        xfBill.status = XfBill.FINISH;
                        xfBill.resCode = data.resCode;
                        xfBill.resMessage = data.resMessage;
                        xfBillMapper.updateByPrimaryKeySelective(xfBill);

                        writer.write(getResStr("SUCCESS"));
                    }
                    if (data.status != null && data.status.toUpperCase().equals("F")) {
                        walletService.failedOrder(xfBill.id, xfBill.amount);

                        xfBill.status = XfBill.FAIL;
                        xfBill.resCode = data.resCode;
                        xfBill.resMessage = data.resMessage;
                        xfBillMapper.updateByPrimaryKeySelective(xfBill);

                        writer.write("FAIL");
                    }
                    if (data.status != null && data.status.toUpperCase().equals("I")) {
                        xfBill.status = XfBill.ING;

                        xfBill.resCode = data.resCode;
                        xfBill.resMessage = data.resMessage;
                        xfBillMapper.updateByPrimaryKeySelective(xfBill);

                        writer.write("ING");
                    }
                } else if ("99001".equals(code) && !StringUtils.isEmpty(params.get("data").toString())) {
                    decryptData = params.get("code").toString() + "|" + params.get("message").toString();
                    //System.out.println("返回数据：" + decryptData);

                    writer.write(decryptData);
                } else {
                    Data data = new Gson().fromJson(decryptData, Data.class);

                    XfBill xfBill = new XfBill();
                    xfBill.orderNo = data.merchantNo;
                    xfBill = xfBillMapper.selectOne(xfBill);
                    if (xfBill == null) {
                        writer.write(getResStr("订单不存在"));
                        return;
                    }
                    xfBill.status = XfBill.FAIL;

                    xfBill.resCode = data.resCode;
                    xfBill.resMessage = data.resMessage;
                    xfBill.modifyTime = Timestamp.from(Instant.now());
                    xfBillMapper.updateByPrimaryKeySelective(xfBill);
                    // todo 加入 通知第三方.

                    writer.write(getResStr("FAIL"));
                }
            } else {
                writer.write("先锋报文签名验证：验签失败");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    public String getResStr(String str) {
        return str.toUpperCase();
    }

    @Transactional
    public Object orderQuery(Long userId, String orderNo) throws Exception {
        if (Strings.isNullOrEmpty(orderNo)) {
            throw new AwesomeException(Config.ERROR_ORDERNO_NOT_EMPTY);
        }
        XfBill bill = new XfBill();
        bill.orderNo = orderNo;
        bill = xfBillMapper.selectOne(bill);
        if (bill == null) {
            throw new AwesomeException(Config.ERROR_ORDER_NOT_FOUND);
        }
        String str = xfClient.orderQuery(orderNo);

        XfPaymentResponse response = new Gson().fromJson(str, XfPaymentResponse.class);
        //加密后的业务数据
        String bizData = UcfForOnline.decryptData(str, mer_pri_key);

        if (response.code.equals("99000")) {

            //交易成功返回订单数据
            Data data = new Gson().fromJson(bizData, Data.class);

            bill.superNo = data.tradeNo;
            if (data.tradeTime != null) {
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYYMMDDhhmmss").parse(data.tradeTime).getTime());
            }

            if (data.status != null && data.status.toUpperCase().equals("S")) {
                if (bill.status == XfBill.FINISH) {
                    return getResJson("A000", "交易成功", null);
                }
                walletService.finishOrder(bill.id, bill.amount);

                bill.status = XfBill.FINISH;
                bill.resCode = data.resCode;
                bill.resMessage = data.resMessage;
                xfBillMapper.updateByPrimaryKeySelective(bill);

                return getResJson("A000", "交易成功", null);
            }
            if (data.status != null && data.status.toUpperCase().equals("F")) {
                if (bill.status == XfBill.FAIL) {
                    bill.resCode = data.resCode;
                    bill.resMessage = data.resMessage;
                    xfBillMapper.updateByPrimaryKeySelective(bill);

                    //业务处理失败. 具体返回先锋message.
                    return getResJson("A005", data.resMessage, null);
                }
                walletService.failedOrder(bill.id, bill.amount);

                bill.status = XfBill.FAIL;
                bill.resCode = data.resCode;
                bill.resMessage = data.resMessage;
                xfBillMapper.updateByPrimaryKeySelective(bill);

                return getResJson("A005", data.resMessage, null);
            }
            if (data.status != null && data.status.toUpperCase().equals("I")) {
                if (bill.status == XfBill.ING) {
                    return getResJson("A006", "交易进行中", null);
                }

                bill.status = XfBill.ING;
                bill.resCode = data.resCode;
                bill.resMessage = data.resMessage;
                xfBillMapper.updateByPrimaryKeySelective(bill);

                //原样返回先锋支付业务code.message
                return getResJson("A006", "交易进行中", null);
            }

            //兼容status 没有返回值.
            //具体情况未知
            return getResJson("A999", data.resCode + data.resMessage, null);

        } else if (response.code.equals("99001")) {

            return getResJson(response.code, response.message, null);
        } else {
            //通用错误返回.
            //具体原因 @see 先锋支付网关返回码.
            //对下游服务商隐藏先锋返回message，统一返回请求失败
            return getResJson(response.code, "请求失败", null);
        }
    }

    public Object wagesPayNoAuth(CreditPay pay) throws Exception {
        if (pay.merchantNo == null) {
            return getResJson("A001", "商户号不能为空", pay);
        }
        UserBase userBase = userBaseService.selectByMerchantNo(pay.merchantNo);
        if (userBase == null) {
            return getResJson("A100", "商户不存在", null);
        }
        if (userBase.state != UserBase.PASS) {
            return getResJson("A1001", "商户资格待审核", null);
        }
        UserKey userKey = userKeyService.getKeyByUserId(userBase.userId);
        if (userKey == null) {
            return getResJson("A102", "请先上传商户私钥", null);
        }
        String sign = sign(pay, userKey.secretKey);
        boolean flag = sign.equals(pay.sign);
        if (flag) {
            wagesPay(userBase.userId, pay.amount, pay.accountNo, pay.accountName, pay.mobileNo, pay.bankNo, pay.userType, pay.accountType, pay.memo, pay.orderNo);
        } else {
            return getResJson("A200", "签名校验错误", null);
        }
        return null;
    }

    public String sign(CreditPay pay, String secret) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(pay);
        return SignUtil.generateSignature(map, secret);
    }
}
