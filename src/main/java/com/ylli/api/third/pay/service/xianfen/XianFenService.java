package com.ylli.api.third.pay.service.xianfen;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ucf.sdk.UcfForOnline;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.model.MchKey;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.mapper.BankPayOrderMapper;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.PayOrderRes;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.BankPayClient;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.Config;
import com.ylli.api.third.pay.modelVo.NotifyRes;
import com.ylli.api.third.pay.modelVo.xianfen.Data;
import com.ylli.api.third.pay.modelVo.xianfen.XianFenResponse;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.SysPaymentLogMapper;
import com.ylli.api.wallet.mapper.WalletLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.SysPaymentLog;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.model.WalletLog;
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
    WalletLogMapper walletLogMapper;

    @Autowired
    WalletMapper walletMapper;

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    BankPayOrderMapper bankPayOrderMapper;

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    SysPaymentLogMapper logMapper;

    @Autowired
    BankPayClient bankPayClient;

    @Value("${xf.pay.mer_pri_key}")
    public String mer_pri_key;

    @Value("${xf.pay.xf_pub_key}")
    public String xf_pub_key;

    /**
     * 商户代付api method
     *
     * @param sysOrderId  系统订单号
     * @param money       代付金额
     * @param accNo       收款账户
     * @param accName     姓名
     * @param mobile      手机
     * @param payType     1-对私，2-对公
     * @param accType     1-借记卡，2-贷记卡
     * @param mchId       商户号
     * @param secretKey   商户密钥
     * @param mchOrderId  商户订单号
     * @param chargeMoney 手续费
     */
    @Transactional
    public Object createXianFenOrder(String sysOrderId, Integer money, String accNo, String accName, String mobile,
                                     Integer payType, Integer accType, Long mchId, String secretKey, String mchOrderId,
                                     Integer chargeMoney) throws Exception {

        String str = xfClient.agencyPayment(sysOrderId, money, accNo, accName, mobile, "SDPB", payType, accType, null);

        XianFenResponse response = new Gson().fromJson(str, XianFenResponse.class);
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
                        payOrder.tradeTime = new Timestamp(new SimpleDateFormat("YYYYMMDDhhmmss").parse(data.tradeTime).getTime());
                    } catch (Exception e) {
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

                    //进行中暂时不去通知下游商户

                    return success(mchOrderId, sysOrderId, money, payOrder.status, secretKey);
                }
            }
            LOGGER.error("sysOrderId = " + sysOrderId + " ,XianFen response exception: [ code = " + data.resCode + ", message = " + data.resMessage + " ]");
            //{"status":"F","resMessage":"暂不支持该银行","resCode":"00041"}
            //[ code = 00063, message = 银行系统升级中，请您稍后再试 ]
            /**
             * 下单失败,case处理各种异常情况
             */
            if (("00041").equals(data.resCode) || ("00063").equals(data.resCode)) {
                payOrder.status = BankPayOrder.FAIL;
                payOrder.msg = data.resMessage;
                bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);
                return new Response("A010", data.resMessage);
            } else {
                //应答失败，状态置为进行中，扣除钱包代付池。等待轮询结果
                payOrder.status = BankPayOrder.ING;
                payOrder.superOrderId = data.tradeNo;
                bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                //扣除金额.(代付金额+手续费)
                walletService.decrReservoir(mchId, (money + chargeMoney));

                return success(mchOrderId, sysOrderId, money, payOrder.status, secretKey);
            }
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
        //response.sign = SignUtil.generateSignature(SignUtil.objectToMap(response), secretKey);
        return response;
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

                Data data = new Gson().fromJson(decryptData, Data.class);
                /**
                 * flag - true: 系统内部；false - 商户api
                 */
                Boolean flag = data.merchantNo.startsWith(SysPaymentLog.XIANFEN);

                //服务调用成功
                if ("99000".equals(code) && !StringUtils.isEmpty(params.get("data").toString())) {
                    if (flag) {
                        Long cashLogId = Long.parseLong(data.merchantNo.replace(SysPaymentLog.XIANFEN, ""));
                        CashLog cashLog = cashLogMapper.selectByPrimaryKey(cashLogId);
                        if (cashLog == null) {
                            LOGGER.error("XianFen notify empty, cashLogId: " + cashLogId);
                            writer.write(getResStr("404 not found"));
                            return;
                        }
                        //防止重复返回，只处理进行中日志。
                        if (cashLog.state != CashLog.PROCESS) {
                            return;
                        }

                        if (data.status != null && data.status.toUpperCase().equals("S")) {
                            //更新提现请求状态
                            cashLog.state = CashLog.FINISH;
                            cashLog.msg = data.resMessage;
                            cashLogMapper.updateByPrimaryKeySelective(cashLog);

                            writer.write(getResStr("SUCCESS"));
                        }
                        if (data.status != null && data.status.toUpperCase().equals("F")) {
                            cashLog.state = CashLog.FAILED;
                            cashLog.msg = data.resMessage;
                            cashLogMapper.updateByPrimaryKeySelective(cashLog);
                            //回滚金额
                            walletService.cashFail(cashLog.mchId, cashLog.money);

                            writer.write(getResStr("SUCCESS"));
                        }
                        if (data.status != null && data.status.toUpperCase().equals("I")) {
                            //do nothing.

                            writer.write(data.resCode + "|" + data.resMessage);
                        }

                    } else {

                        BankPayOrder payOrder = bankPayOrderMapper.selectBySysOrderId(data.merchantNo);

                        if (payOrder == null) {
                            LOGGER.error("XianFen notify empty, sysOrderId: " + data.merchantNo);
                            writer.write(getResStr("404 not found"));
                            return;
                        }
                        //若订单在下单返回时已经处理成功失败。不再去进行相应得逻辑。
                        if (payOrder.status != BankPayOrder.ING) {
                            return;
                        }
                        if (data.tradeTime != null) {
                            payOrder.tradeTime = new Timestamp(new SimpleDateFormat("YYYYMMDDhhmmss").parse(data.tradeTime).getTime());
                        }

                        if (data.status != null && data.status.toUpperCase().equals("S")) {
                            payOrder.status = BankPayOrder.FINISH;
                            bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                            //加入代付分润计算
                            walletService.incrBankBonus(payOrder.mchId, payOrder.money);

                            //发送异步通知
                            if (!Strings.isNullOrEmpty(payOrder.notifyUrl)) {
                                String params1 = generateRes(payOrder.money, payOrder.mchOrderId, payOrder.sysOrderId, payOrder.status, payOrder.tradeTime);
                                bankPayClient.sendNotify(payOrder.sysOrderId, payOrder.notifyUrl, params1, true);
                            }
                            writer.write(getResStr("SUCCESS"));
                        }
                        if (data.status != null && data.status.toUpperCase().equals("F")) {
                            payOrder.status = BankPayOrder.FAIL;
                            payOrder.msg = data.resMessage;
                            bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                            //订单状态为ing时 已扣除金额
                            walletService.incrReservoir(payOrder.mchId, (payOrder.money + payOrder.chargeMoney));

                            //发送异步通知
                            if (!Strings.isNullOrEmpty(payOrder.notifyUrl)) {
                                String params1 = generateRes(payOrder.money, payOrder.mchOrderId, payOrder.sysOrderId, payOrder.status, payOrder.tradeTime);
                                bankPayClient.sendNotify(payOrder.sysOrderId, payOrder.notifyUrl, params1, true);
                            }
                            writer.write(getResStr("SUCCESS"));
                        }
                        if (data.status != null && data.status.toUpperCase().equals("I")) {
                            //do nothing.

                            writer.write(data.resCode + "|" + data.resMessage);
                        }


                    }
                } else if ("99001".equals(code) && !StringUtils.isEmpty(params.get("data").toString())) {
                    //code=99001，接口调用异常，无法确认订单状态
                    decryptData = params.get("code").toString() + "|" + params.get("message").toString();
                    writer.write(decryptData);
                } else {
                    //当作失败处理.

                    if (!flag) {
                        // 进入商户api
                        BankPayOrder payOrder = bankPayOrderMapper.selectBySysOrderId(data.merchantNo);
                        payOrder.status = BankPayOrder.FAIL;
                        payOrder.superOrderId = data.tradeNo;
                        payOrder.msg = data.resMessage;
                        bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                        //金额回滚(代付金额+手续费)
                        //订单状态为ing时 已扣除金额
                        walletService.incrReservoir(payOrder.mchId, (payOrder.money + payOrder.chargeMoney));

                        //发送异步通知
                        if (!Strings.isNullOrEmpty(payOrder.notifyUrl)) {
                            String params1 = generateRes(payOrder.money, payOrder.mchOrderId, payOrder.sysOrderId, payOrder.status, payOrder.tradeTime);
                            bankPayClient.sendNotify(payOrder.sysOrderId, payOrder.notifyUrl, params1, true);
                        }

                    } else {
                        //系统内部
                        Long cashLogId = Long.parseLong(data.merchantNo.replace(SysPaymentLog.XIANFEN, ""));
                        CashLog cashLog = cashLogMapper.selectByPrimaryKey(cashLogId);
                        cashLog.state = CashLog.FAILED;
                        cashLog.msg = data.resMessage;
                        cashLogMapper.updateByPrimaryKeySelective(cashLog);
                        //回滚金额
                        walletService.cashFail(cashLog.mchId, cashLog.money);
                    }
                    writer.write(getResStr("SUCCESS"));
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


    /**
     * 系统内部提现使用.
     * 先锋不同于PingAn，平安不会自动发送异步回调。所以加入SYS_PAYMENT_LOG 来更新状态.
     * 先锋含有主动回调。固不再去使用SYS_PAYMENT_LOG 去主动轮询结果
     *
     * @param cashLogId      提现日志id，由于测试时暂用了很多自增序列。故修改规则为 "XianFen".append(cashLogId) 作为提现单号
     * @param money          金额
     * @param bankcardNumber 卡号
     * @param name           姓名
     * @param reservedPhone  预留手机
     * @param payType        代付类型
     * @param accType        账户类型
     */
    @Transactional
    @Deprecated
    public void createXianFenOrder(Long cashLogId, Integer money, String bankcardNumber, String name, String reservedPhone, Integer payType, Integer accType) throws Exception {

        //测试单时占用了很多，不能直接使用原生id
        String merchantNo = new StringBuffer(SysPaymentLog.XIANFEN).append(cashLogId).toString();
        String str = xfClient.agencyPayment(merchantNo, money, bankcardNumber, name, reservedPhone, "SDPB", payType, accType, null);

        XianFenResponse response = new Gson().fromJson(str, XianFenResponse.class);
        //加密后的业务数据
        String bizData = UcfForOnline.decryptData(str, mer_pri_key);

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
            CashLog log = cashLogMapper.selectByPrimaryKey(cashLogId);
            //应答码，00000 成功
            if (data.resCode.equals("00000")) {

                if (data.status != null && data.status.toUpperCase().equals("S")) {

                    //更新提现处理请求
                    log.state = CashLog.FINISH;
                    log.type = CashLog.XIANFENG;
                    cashLogMapper.updateByPrimaryKeySelective(log);

                }
                if (data.status != null && data.status.toUpperCase().equals("F")) {

                    //更新提现处理请求
                    log.state = CashLog.FAILED;
                    log.type = CashLog.XIANFENG;
                    log.msg = data.resMessage;
                    cashLogMapper.updateByPrimaryKeySelective(log);
                    //回滚金额
                    walletService.cashFail(log.mchId, log.money);
                }
                if (data.status != null && data.status.toUpperCase().equals("I")) {

                    //更新提现处理请求
                    log.state = CashLog.PROCESS;
                    log.type = CashLog.XIANFENG;
                    log.msg = data.resMessage;
                    cashLogMapper.updateByPrimaryKeySelective(log);
                }
            } else {
                //data 无返回状态。默认更新处理中。等待回调
                //更新提现处理请求
                log.state = CashLog.PROCESS;
                log.type = CashLog.XIANFENG;
                log.msg = data.resMessage;
                cashLogMapper.updateByPrimaryKeySelective(log);
            }
        } else if (response.code.equals("99001")) {
            LOGGER.error("cashLogId = " + cashLogId + " ,create XianFen order 99001: [ code = " + response.code + ", message = " + response.message + " ]");
            //接口调用异常，无法确认订单状态
            //暂时置订单状态为进行中。等待先锋异步轮询确定最终状态

            //更新提现处理请求
            CashLog log = cashLogMapper.selectByPrimaryKey(cashLogId);
            if (log != null) {
                log.state = CashLog.PROCESS;
                log.type = CashLog.XIANFENG;
                log.msg = new StringBuffer("response.code").append("|").append(response.message).toString();
                cashLogMapper.updateByPrimaryKeySelective(log);
            }
            //不回滚金额.等待异步回调 notify 最终决定。

        } else {
            LOGGER.error("cashLogId = " + cashLogId + " ,create XianFen order fail: [ code = " + response.code + ", message = " + response.message + " ]");
            //具体原因 @see 先锋支付网关返回码.

            //更新提现处理请求
            CashLog log = cashLogMapper.selectByPrimaryKey(cashLogId);
            if (log != null) {
                log.state = CashLog.FAILED;
                log.type = CashLog.XIANFENG;
                log.msg = new StringBuffer("response.code").append("|").append(response.message).toString();
                cashLogMapper.updateByPrimaryKeySelective(log);
            }
            //回滚金额
            walletService.cashFail(log.mchId, log.money);
        }

    }

    /*@Deprecated
    @Transactional
    public Object fail(String sysOrderId) throws Exception {
        BankPayOrder payOrder = bankPayOrderMapper.selectBySysOrderId(sysOrderId);

        if (payOrder == null) {
            return "payOrder not found";
        }
        //若订单在下单返回时已经处理成功失败。不再去进行相应得逻辑。
        if (payOrder.status != BankPayOrder.ING) {
            return "payOrder nor ing";
        }

        payOrder.status = BankPayOrder.FAIL;
        bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

        //订单状态为ing时 已扣除金额
        walletService.incrReservoir(payOrder.mchId, (payOrder.money + payOrder.chargeMoney));

        //发送异步通知
        if (!Strings.isNullOrEmpty(payOrder.notifyUrl)) {
            String params1 = generateRes(payOrder.money, payOrder.mchOrderId, payOrder.sysOrderId, payOrder.status, payOrder.tradeTime);
            bankPayClient.sendNotify(payOrder.sysOrderId, payOrder.notifyUrl, params1, true);
        }
        return "ok";
    }*/

    /**
     * 余额查询.
     *
     * @return
     */
    public Object balance() throws Exception {
        String str = xfClient.balance();
        XianFenResponse response = new Gson().fromJson(str, XianFenResponse.class);
        if (("99000").equals(response.code)) {
            //加密后的业务数据
            String bizData = UcfForOnline.decryptData(str, mer_pri_key);
            //{  返回原始数据.balance=账户余额/分，freezeAmount=出款在途金额（不可用），available=可用金额
            // "balance":"10352842",
            // "nonAdvance":"10352842",
            // "freezeAmount":"0",
            // "resCode":"00000",
            // "resMessage":"成功",
            // "available":"10352842",
            // "previousBalance":"1028657"}
            return bizData;
        } else {
            throw new AwesomeException(Config.ERROR.format(new StringBuffer(response.code).append("|").append(response.message)));
        }
    }

    @Transactional
    public void rechargeNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

                Data data = new Gson().fromJson(decryptData, Data.class);
                WalletLog walletLog = walletLogMapper.selectByPrimaryKey(Long.parseLong(data.merchantNo));

                //服务调用成功
                if ("99000".equals(code) && !StringUtils.isEmpty(params.get("data").toString())) {
                    if (walletLog == null) {
                        LOGGER.error("XianFen recharge notify empty, walletLogId: " + data.merchantNo);
                        writer.write(getResStr("404 not found"));
                        return;
                    }
                    //防止重复返回，只处理进行中日志。
                    if (walletLog.status != WalletLog.ING) {
                        return;
                    }

                    if (data.status != null && data.status.toUpperCase().equals("S")) {
                        //更新提现请求状态
                        walletLog.status = WalletLog.FINISH;
                        walletLogMapper.updateByPrimaryKeySelective(walletLog);

                        Wallet wallet = walletService.getOwnWallet(walletLog.mchId);
                        wallet.reservoir = wallet.reservoir + walletLog.money - 10 * 100;
                        walletMapper.updateByPrimaryKeySelective(wallet);

                        writer.write(getResStr("SUCCESS"));
                    }
                    if (data.status != null && data.status.toUpperCase().equals("F")) {
                        walletLog.status = WalletLog.FAIL;
                        walletLogMapper.updateByPrimaryKeySelective(walletLog);

                        writer.write(getResStr("SUCCESS"));
                    }
                    if (data.status != null && data.status.toUpperCase().equals("I")) {
                        //do nothing.

                        writer.write(data.resCode + "|" + data.resMessage);
                    }

                } else if ("99001".equals(code) && !StringUtils.isEmpty(params.get("data").toString())) {
                    //code=99001，接口调用异常，无法确认订单状态
                    decryptData = params.get("code").toString() + "|" + params.get("message").toString();
                    writer.write(decryptData);
                } else {
                    //当作失败处理.
                    walletLog.status = WalletLog.FAIL;
                    walletLogMapper.updateByPrimaryKeySelective(walletLog);
                }
                writer.write(getResStr("SUCCESS"));
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
}
