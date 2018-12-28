package com.ylli.api.third.pay.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ucf.sdk.UcfForOnline;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.model.MchKey;
import com.ylli.api.mch.service.MchBaseService;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.mapper.BankPayOrderMapper;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.PayOrderRes;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.BankPayClient;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.Config;
import com.ylli.api.third.pay.mapper.XfBillMapper;
import com.ylli.api.third.pay.model.CreditPay;
import com.ylli.api.third.pay.model.Data;
import com.ylli.api.third.pay.model.NotifyRes;
import com.ylli.api.third.pay.model.WagesPayResponse;
import com.ylli.api.third.pay.model.XfBill;
import com.ylli.api.third.pay.model.XfPaymentResponse;
import com.ylli.api.wallet.service.WalletService;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableAsync
public class XianFenService {

    private static Logger LOGGER = LoggerFactory.getLogger(XianFenService.class);

    @Autowired
    XfClient xfClient;

    @Autowired
    WalletService walletService;

    @Autowired
    XfBillMapper xfBillMapper;

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    BankPayOrderMapper bankPayOrderMapper;

    @Autowired
    BankPayClient bankPayClient;

    @Value("${xf.pay.mer_pri_key}")
    public String mer_pri_key;

    @Value("${xf.pay.xf_pub_key}")
    public String xf_pub_key;

    @Transactional
    public Object createXianFenOrder(String sysOrderId, Integer money, String accNo, String accName, String mobile,
                                     Integer payType, Integer accType, Long mchId, String secretKey, String mchOrderId,
                                     Integer chargeMoney) throws Exception {

        String str = xfClient.agencyPayment(sysOrderId, money, accNo, accName, mobile, "SDPB", payType, accType, null);

        XfPaymentResponse response = new Gson().fromJson(str, XfPaymentResponse.class);
        //加密后的业务数据
        String bizData = UcfForOnline.decryptData(str, mer_pri_key);

        BankPayOrder payOrder = bankPayOrderMapper.selectBySysOrderId(sysOrderId);
        /**
         * 99000 - 接口调用成功
         * 99001 - 接口调用异常
         * 其他返回码，接口调用失败，可置订单为失败
         *
         * 成功或者待确认均扣除钱包代付池（非失败），后续若订单状态为Ing,且同步结果为fail才进行金额回滚
         */
        if (response.code.equals("99000")) {
            //交易成功返回订单数据
            Data data = new Gson().fromJson(bizData, Data.class);

            //应答码，00000 成功
            if (data.resCode.equals("00000")) {

                if (data.status != null && data.status.toUpperCase().equals("S")) {
                    payOrder.status = BankPayOrder.FINISH;
                    payOrder.superOrderId = data.tradeNo;
                    try {
                        payOrder.tradeTime = (Timestamp) new SimpleDateFormat("YYYYMMDDhhmmss").parse(data.tradeTime);
                    } catch (Exception e){
                        payOrder.tradeTime = Timestamp.from(Instant.now());
                    }
                    bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                    //扣除金额.(代付金额+手续费)
                    walletService.decrReservoir(mchId, (money + chargeMoney));

                    //发送异步通知
                    if (!Strings.isNullOrEmpty(payOrder.notifyUrl)) {
                        String params = generateRes(payOrder.money, payOrder.mchOrderId, payOrder.sysOrderId, payOrder.status, payOrder.tradeTime);
                        bankPayClient.sendNotify(payOrder.sysOrderId, payOrder.notifyUrl, params, true);
                    }
                    return success(mchOrderId, sysOrderId, money, payOrder.status, secretKey);
                }
                if (data.status != null && data.status.toUpperCase().equals("F")) {
                    payOrder.status = BankPayOrder.FAIL;
                    payOrder.superOrderId = data.tradeNo;

                    bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                    //发送异步通知
                    if (!Strings.isNullOrEmpty(payOrder.notifyUrl)) {
                        String params = generateRes(payOrder.money, payOrder.mchOrderId, payOrder.sysOrderId, payOrder.status, payOrder.tradeTime);
                        bankPayClient.sendNotify(payOrder.sysOrderId, payOrder.notifyUrl, params, true);
                    }
                    return success(mchOrderId, sysOrderId, money, payOrder.status, secretKey);
                }
                if (data.status != null && data.status.toUpperCase().equals("I")) {
                    payOrder.status = BankPayOrder.ING;
                    payOrder.superOrderId = data.tradeNo;
                    bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                    //扣除金额.(代付金额+手续费)
                    walletService.decrReservoir(mchId, (money + chargeMoney));

                    //发送异步通知
                    if (!Strings.isNullOrEmpty(payOrder.notifyUrl)) {
                        String params = generateRes(payOrder.money, payOrder.mchOrderId, payOrder.sysOrderId, payOrder.status, payOrder.tradeTime);
                        bankPayClient.sendNotify(payOrder.sysOrderId, payOrder.notifyUrl, params, true);
                    }
                    return success(mchOrderId, sysOrderId, money, payOrder.status, secretKey);
                }
            }
            LOGGER.error("sysOrderId = " + sysOrderId + " ,XianFen response exception: [ code = " + data.resCode + ", message = " + data.resMessage + " ]");

            //应答失败，状态置为进行中，扣除钱包代付池。等待轮询结果
            payOrder.status = BankPayOrder.ING;
            payOrder.superOrderId = data.tradeNo;
            bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

            //扣除金额.(代付金额+手续费)
            walletService.decrReservoir(mchId, (money + chargeMoney));

            return success(mchOrderId, sysOrderId, money, payOrder.status, secretKey);
        } else if (response.code.equals("99001")) {
            //接口调用异常，无法确认订单状态
            //更新订单，获取不到父级订单号
            //暂时置订单状态为进行中。等待先锋异步轮询确定最终状态
            payOrder.status = BankPayOrder.ING;
            bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

            //扣除金额.(代付金额+手续费)
            walletService.decrReservoir(mchId, (money + chargeMoney));

            return success(mchOrderId, sysOrderId, money, payOrder.status, secretKey);
        } else {
            LOGGER.error("sysOrderId = " + sysOrderId + " ,create XianFen order fail: [ code = " + response.code + ", message = " + response.message + " ]");
            //通用错误返回.
            //具体原因 @see 先锋支付网关返回码.
            //对下游服务商隐藏先锋返回message，统一返回系统异常
            payOrder.status = BankPayOrder.FAIL;
            bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);
            return new Response("A011", "系统异常");
        }
    }

    /**
     * @param money      金额
     * @param mchOrderId 商户系统订单号
     * @param sysOrderId 系统订单号
     * @param status     状态
     * @param tradeTime  结算时间
     */
    public String generateRes(Integer money, String mchOrderId, String sysOrderId, Integer status, Timestamp tradeTime) throws Exception {
        NotifyRes res = new NotifyRes();
        res.money = money.toString();
        res.mchOrderId = mchOrderId;
        res.sysOrderId = sysOrderId;
        res.status = BankPayOrder.statusToString(status);
        if (tradeTime != null) {
            res.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tradeTime);
        }
        BankPayOrder order = bankPayOrderMapper.selectBySysOrderId(sysOrderId);
        MchKey key = mchKeyService.getKeyByUserId(order.mchId);

        Map<String, String> map = SignUtil.objectToMap(res);
        res.sign = SignUtil.generateSignature(map, key.secretKey);
        return new Gson().toJson(res);
    }


    public Response success(String mchOrderId, String sysOrderId, Integer money, Integer status, String secretKey) throws Exception {
        PayOrderRes res = new PayOrderRes();
        res.mchOrderId = mchOrderId;
        res.sysOrderId = sysOrderId;
        res.money = money;
        res.status = BankPayOrder.statusToString(status);
        Response response = new Response("A000", "成功", null, res);
        response.sign = SignUtil.generateSignature(SignUtil.objectToMap(response), secretKey);
        return response;
    }

    public String getResJson(String code, String msg, Object object) {
        WagesPayResponse response = new WagesPayResponse();
        response.code = code;
        response.message = msg;
        response.object = object;
        return new Gson().toJson(response);
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
                        //walletService.finishOrder(xfBill.id, xfBill.amount);

                        xfBill.status = XfBill.FINISH;
                        xfBill.resCode = data.resCode;
                        xfBill.resMessage = data.resMessage;
                        xfBillMapper.updateByPrimaryKeySelective(xfBill);

                        writer.write(getResStr("SUCCESS"));
                    }
                    if (data.status != null && data.status.toUpperCase().equals("F")) {
                        //walletService.failedOrder(xfBill.id, xfBill.amount);

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
                    //code=99001，接口调用异常，无法确认订单状态
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
                //walletService.finishOrder(bill.id, bill.amount);

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
                //walletService.failedOrder(bill.id, bill.amount);

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


    public String sign(CreditPay pay, String secret) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(pay);
        return SignUtil.generateSignature(map, secret);
    }
}
