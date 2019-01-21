package com.ylli.api.third.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.model.MchKey;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.mapper.BankPayOrderMapper;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.PayOrderRes;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.BankPayClient;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.model.NotifyRes;
import com.ylli.api.third.pay.model.PingAnOrder;
import com.ylli.api.third.pay.model.PingAnOrderQuery;
import com.ylli.api.third.pay.util.TimeUtil;
import com.ylli.api.third.pay.util.XmlRequestUtil;
import com.ylli.api.third.pay.util.YQUtil;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.SysPaymentLogMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.SysPaymentLog;
import com.ylli.api.wallet.service.WalletService;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableAsync
public class PingAnService {

    private final static Logger LOGGER = LoggerFactory.getLogger(PingAnService.class);

    @Value("${b2bc.url}")
    public String url;

    @Autowired
    PingAnClient pingAnClient;

    @Autowired
    SysPaymentLogMapper logMapper;

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    BankPayOrderMapper bankPayOrderMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    BankPayClient bankPayClient;

    //企业签约帐号
    @Value("${acc.no}")
    public String acctNo;
    //单位代码
    @Value("${corp.id}")
    public String corpId;
    //银企代码
    @Value("${yqdm}")
    public String yqdm;

    /**
     * xml报文头
     */
    public final static String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    /**
     * 系统内部代付方法
     *
     * @param cashLogId      提现请求id，由于测试时暂用了很多自增序列。故修改规则为 "PingAn".append(cashLogId) 作为提现单号
     * @param inAcctNo
     * @param inAcctName
     * @param inAcctBankName
     * @param mobile
     * @param money
     */
    public void createPingAnOrder(Long cashLogId, String inAcctNo, String inAcctName, String inAcctBankName, String mobile, Integer money) {

        /**组装请求报文-start*/
        PingAnOrder order = new PingAnOrder();
        //required
        //测试单时占用了很多，不能直接使用原生id
        order.orderNumber = new StringBuffer(SysPaymentLog.PINGAN).append(cashLogId).toString();
        order.acctNo = acctNo;
        order.busiType = "00000";
        //金额转换-分转换为元
        order.tranAmount = (new BigDecimal(money).divide(new BigDecimal(100))).toString();
        order.inAcctNo = inAcctNo;
        order.inAcctName = inAcctName;
        //not required
        order.corpId = corpId;
        order.ccyCode = "RMB";
        order.inAcctBankName = inAcctBankName;
        order.mobile = mobile;
        order.inAcctBankNode = "";
        order.remark = "";
        order.inAcctProvinceName = "";
        order.inAcctCityName = "";

        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(order));

        String reqXml = YQUtil.asemblyPackets(yqdm, "KHKF03", xml);
        /**组装请求报文-end*/

        LOGGER.info("平安代付请求报文：" + reqXml);
        String res = pingAnClient.post(reqXml, url);

        /***处理返回结果-start*/
        if (res != null) {
            String code = StringUtils.substringBefore(res, ":").substring(87);
            if (!"000000".equals(code)) {
                //平安银行代发交易失败
                String msg = StringUtils.substringBefore(res.substring(94), "0").trim();

                //更新提现处理请求
                CashLog log = cashLogMapper.selectByPrimaryKey(cashLogId);
                if (log != null) {
                    log.state = CashLog.FAILED;
                    log.type = CashLog.PINGAN;
                    log.msg = msg;
                    cashLogMapper.updateByPrimaryKeySelective(log);
                }
                //回滚金额
                walletService.cashFail(log.mchId, log.money);

                LOGGER.error("平安代付失败：" + msg);
            } else {
                /**
                 *  or res.contains("交易受理成功")
                 */
                //A001010201010010343000045390000000000134KHKF03123450220181218033745YQTEST20181218033745000000:交易受理成功                                                                                 000001            00000000000<?xml version="1.0" encoding="UTF-8" ?><Result><OrderNumber>5111</OrderNumber><BussFlowNo>8043431812186167923315</BussFlowNo></Result>
                String returnXml = StringUtils.substringAfter(res, "?>");
                Document document = null;
                try {
                    document = DocumentHelper.parseText(returnXml);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
                //银行业务流水号
                String bussFlowNo = document.getRootElement().element("BussFlowNo").getText();
                //订单号
                String orderNumber = document.getRootElement().element("OrderNumber").getText();

                //更新提现处理请求
                CashLog log = cashLogMapper.selectByPrimaryKey(cashLogId);
                if (log != null) {
                    //平安受理成功，提现状态为处理中
                    log.state = CashLog.PROCESS;
                    log.type = CashLog.PINGAN;
                    //log.msg = msg;
                    cashLogMapper.updateByPrimaryKeySelective(log);
                }

                SysPaymentLog sysPaymentLog = new SysPaymentLog();
                sysPaymentLog.type = SysPaymentLog.TYPE_MCH;
                sysPaymentLog.orderId = orderNumber;
                logMapper.insertSelective(sysPaymentLog);

                LOGGER.info("平安代付受理成功，系统订单号：" + orderNumber + "\n银行业务流水号：" + bussFlowNo);
            }
            /***处理返回结果-end*/
        } else {
            //没有返回.暂时默认平安没有受理。cash_log 状态 保持不变，待处理
            LOGGER.error("平安代付返回res：null");
        }
    }

    /**
     * 用于提供给商户系统代付  外部使用
     *
     * @param sysOrderId     使用生成的系统订单号作为代付商户订单号
     * @param inAcctNo
     * @param inAcctName
     * @param inAcctBankName
     * @param mobile
     * @param money
     * @return
     */
    public Response createPingAnOrder(String sysOrderId, String inAcctNo, String inAcctName, String inAcctBankName,
                                      String mobile, Integer money, String secretKey, String mchOrderId,
                                      Integer chargeMoney, Long mchId) throws Exception {

        /**组装请求报文-start*/
        PingAnOrder order = new PingAnOrder();
        //required
        order.orderNumber = sysOrderId;
        order.acctNo = acctNo;
        order.busiType = "00000";
        //金额转换-分转换为元
        order.tranAmount = (new BigDecimal(money).divide(new BigDecimal(100))).toString();
        order.inAcctNo = inAcctNo;
        order.inAcctName = inAcctName;
        //not required
        order.corpId = corpId;
        order.ccyCode = "RMB";
        order.inAcctBankName = inAcctBankName;
        order.mobile = mobile;
        order.inAcctBankNode = "";
        order.remark = "";
        order.inAcctProvinceName = "";
        order.inAcctCityName = "";

        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(order));

        String reqXml = YQUtil.asemblyPackets(yqdm, "KHKF03", xml);
        /**组装请求报文-end*/

        LOGGER.info("平安代付请求报文：" + reqXml);
        String res = pingAnClient.post(reqXml, url);

        BankPayOrder payOrder = bankPayOrderMapper.selectBySysOrderId(sysOrderId);

        /***处理返回结果-start*/
        if (res != null) {
            String code = StringUtils.substringBefore(res, ":").substring(87);
            if (!"000000".equals(code)) {
                //平安银行代发交易失败
                String msg = StringUtils.substringBefore(res.substring(94), "0").trim();

                //更新订单
                payOrder.status = BankPayOrder.FAIL;
                bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                LOGGER.error("平安代付失败：" + msg);
                return new Response("A010", msg);
            } else {
                /**
                 *  or res.contains("交易受理成功")
                 */
                //A001010201010010343000045390000000000134KHKF03123450220181218033745YQTEST20181218033745000000:交易受理成功                                                                                 000001            00000000000<?xml version="1.0" encoding="UTF-8" ?><Result><OrderNumber>5111</OrderNumber><BussFlowNo>8043431812186167923315</BussFlowNo></Result>
                String returnXml = StringUtils.substringAfter(res, "?>");
                Document document = null;
                try {
                    document = DocumentHelper.parseText(returnXml);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
                //银行业务流水号
                String bussFlowNo = document.getRootElement().element("BussFlowNo").getText();
                //订单号
                String orderNumber = document.getRootElement().element("OrderNumber").getText();

                //更新订单 使用业务流水号作为父级订单号
                payOrder.status = BankPayOrder.ING;
                payOrder.superOrderId = bussFlowNo;
                bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

                //加入自动轮询
                SysPaymentLog sysPaymentLog = new SysPaymentLog();
                sysPaymentLog.type = SysPaymentLog.TYPE_SYS;
                sysPaymentLog.orderId = orderNumber;
                logMapper.insertSelective(sysPaymentLog);

                //只有受理成功，才扣除代付池金额。
                //扣除金额.(代付金额+手续费)
                walletService.decrReservoir(mchId, (money + chargeMoney));

                LOGGER.info("平安代付受理成功，系统订单号：" + orderNumber + "\n银行业务流水号：" + bussFlowNo);
                return success(mchOrderId, sysOrderId, money, payOrder.status, secretKey);
            }
            /***处理返回结果-end*/
        } else {
            LOGGER.error("平安代付返回res：null");
            //没有返回. 暂时认为 处理失败。
            //更新订单为失败。
            payOrder.status = BankPayOrder.FAIL;
            bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);

            return new Response("A011", "系统异常");
        }
    }

    public Response success(String mchOrderId, String sysOrderId, Integer money, Integer status, String secretKey) throws Exception {
        PayOrderRes res = new PayOrderRes();
        res.mchOrderId = mchOrderId;
        res.sysOrderId = sysOrderId;
        res.money = money;
        res.status = BankPayOrder.statusToString(status);
        Response response = new Response("A000", "成功", null, new Gson().toJson(res));
        response.sign = SignUtil.generateSignature(SignUtil.objectToMap(response), secretKey);
        return response;
    }


    /**
     * 跨行快付查询KHKF04
     * 系统内部使用。（支付提现）
     *
     * @return
     */
    @Transactional
    public void payQuery(SysPaymentLog log) {

        /**组装请求报文-start**/
        PingAnOrderQuery query = new PingAnOrderQuery();
        query.acctNo = acctNo;
        //query.bussFlowNo = "8043431812224558559480";
        query.orderNumber = log.orderId;

        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(query));
        String reqXml = YQUtil.asemblyPackets(yqdm, "KHKF04", xml);
        /**组装请求报文-end**/


        /***处理返回结果-start*/
        String res = pingAnClient.post(reqXml, url);
        if (res == null) {
            log.failCount = log.failCount + 1;
            log.modifyTime = Timestamp.from(Instant.now());
            logMapper.updateByPrimaryKeySelective(log);
            LOGGER.info("平安跨行快付查询KHKF04：res = null");
            return;
        }

        //获取返回code
        String code = StringUtils.substringBefore(res, ":").substring(87);
        if (!"000000".equals(code)) {
            /**
             * 平安银行代发交易查询失败
             * YQ9996 查询不存在交易返回码
             * 订单创建时间于当前时间小于1分钟，返回处理中；否则返回交易失败
             */
            if ("YQ9996".equals(code)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(log.createTime);
                cal.add(Calendar.HOUR, 8);
                Date tradeTime = cal.getTime();
                if (tradeTime.compareTo(TimeUtil.nextTime(new Date(), null, -1, null)) < 0) {
                    //todo 逻辑先不处理。
                    //交易失败
                    //return "fail";
                }
            }
            //交易进行中
            log.failCount = log.failCount + 1;
            logMapper.updateByPrimaryKeySelective(log);
            return;
            //return "processing";
        }

        //获取返回xml
        String returnXml = StringUtils.substringAfter(res, "?>");

        try {
            Document document = DocumentHelper.parseText(returnXml);
            //银行业务流水号
            String BussFlowNo = document.getRootElement().element("BussFlowNo").getText();
            //银行交易流水号
            String TranFlowNo = document.getRootElement().elementText("TranFlowNo");

            String stt = document.getRootElement().element("Status").getText();
            String RetCode = document.getRootElement().element("RetCode").getText();
            String RetMsg = document.getRootElement().element("RetMsg").getText();
            //成功
            if ("20".equals(stt)) {
                CashLog cashLog = cashLogMapper.selectByPrimaryKey(Long.parseLong(log.orderId.replace(SysPaymentLog.PINGAN, "")));
                cashLog.state = CashLog.FINISH;
                cashLog.type = CashLog.PINGAN;
                cashLogMapper.updateByPrimaryKeySelective(cashLog);

                walletService.cashSuc(cashLog.mchId, cashLog.money);

                //删除终态
                logMapper.delete(log);
                //return "success";
            }
            //失败
            else if ("30".equals(stt)) {
                CashLog cashLog = cashLogMapper.selectByPrimaryKey(Long.parseLong(log.orderId.replace(SysPaymentLog.PINGAN, "")));
                cashLog.state = CashLog.FAILED;
                cashLog.type = CashLog.PINGAN;
                cashLog.msg = RetMsg;
                cashLogMapper.updateByPrimaryKeySelective(cashLog);

                walletService.cashFail(cashLog.mchId, cashLog.money);

                //删除终态
                logMapper.delete(log);
                //return "fail";
            }
            //处理中
            else {
                log.failCount = log.failCount + 1;
                logMapper.updateByPrimaryKeySelective(log);
                //return "processing";
            }
        } catch (Exception e) {
            LOGGER.error("平安银行代发交易查询返回报文解析失败", e);
        }
        /***处理返回结果-end*/
    }

    /**
     * 用户商户代付-自动轮询
     */
    @Transactional
    public void payQueryMch(SysPaymentLog log) {

        /**组装请求报文-start**/
        PingAnOrderQuery query = new PingAnOrderQuery();
        query.acctNo = acctNo;
        //query.bussFlowNo = "8043431812224558559480";
        query.orderNumber = log.orderId;

        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(query));
        String reqXml = YQUtil.asemblyPackets(yqdm, "KHKF04", xml);
        /**组装请求报文-end**/


        /***处理返回结果-start*/
        String res = pingAnClient.post(reqXml, url);
        if (res == null) {
            log.failCount = log.failCount + 1;
            log.modifyTime = Timestamp.from(Instant.now());
            logMapper.updateByPrimaryKeySelective(log);
            LOGGER.info("平安跨行快付查询KHKF04：res = null");
            return;
        }

        //获取返回code
        String code = StringUtils.substringBefore(res, ":").substring(87);
        if (!"000000".equals(code)) {
            /**
             * 平安银行代发交易查询失败
             * YQ9996 查询不存在交易返回码
             * 订单创建时间于当前时间小于1分钟，返回处理中；否则返回交易失败
             */
            /*if ("YQ9996".equals(code)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(log.createTime);
                cal.add(Calendar.HOUR, 8);
                Date tradeTime = cal.getTime();
                if (tradeTime.compareTo(TimeUtil.nextTime(new Date(), null, -1, null)) < 0) {
                    //todo 逻辑先不处理。
                    //交易失败
                    //return "fail";
                }
            }*/
            //交易进行中
            log.failCount = log.failCount + 1;
            logMapper.updateByPrimaryKeySelective(log);
            return;
        }

        //获取返回xml
        String returnXml = StringUtils.substringAfter(res, "?>");

        try {
            Document document = DocumentHelper.parseText(returnXml);
            //银行业务流水号
            String BussFlowNo = document.getRootElement().element("BussFlowNo").getText();
            //银行交易流水号
            String TranFlowNo = document.getRootElement().elementText("TranFlowNo");

            String stt = document.getRootElement().element("Status").getText();
            String RetCode = document.getRootElement().element("RetCode").getText();
            String RetMsg = document.getRootElement().element("RetMsg").getText();
            //成功
            if ("20".equals(stt)) {
                //更新订单状态
                BankPayOrder bankPayOrder = bankPayOrderMapper.selectBySysOrderId(log.orderId);
                bankPayOrder.status = BankPayOrder.FINISH;
                bankPayOrder.tradeTime = Timestamp.from(Instant.now());
                bankPayOrderMapper.updateByPrimaryKeySelective(bankPayOrder);

                //发送异步通知
                if (!Strings.isNullOrEmpty(bankPayOrder.notifyUrl)) {
                    String params = generateRes(bankPayOrder.money, bankPayOrder.mchOrderId, bankPayOrder.sysOrderId, bankPayOrder.status, bankPayOrder.tradeTime);
                    bankPayClient.sendNotify(bankPayOrder.sysOrderId, bankPayOrder.notifyUrl, params, true);
                }

                //删除终态
                logMapper.delete(log);
            }
            //失败
            else if ("30".equals(stt)) {
                //更新订单状态
                BankPayOrder bankPayOrder = bankPayOrderMapper.selectBySysOrderId(log.orderId);
                bankPayOrder.status = BankPayOrder.FAIL;
                bankPayOrderMapper.updateByPrimaryKeySelective(bankPayOrder);

                //回滚金额
                walletService.incrReservoir(bankPayOrder.mchId, (bankPayOrder.money + bankPayOrder.chargeMoney));

                //发送异步通知
                if (!Strings.isNullOrEmpty(bankPayOrder.notifyUrl)) {
                    String params = generateRes(bankPayOrder.money, bankPayOrder.mchOrderId, bankPayOrder.sysOrderId, bankPayOrder.status, bankPayOrder.tradeTime);
                    bankPayClient.sendNotify(bankPayOrder.sysOrderId, bankPayOrder.notifyUrl, params, true);
                }

                //删除终态
                logMapper.delete(log);
            }
            //处理中
            else {
                //
                log.failCount = log.failCount + 1;
                logMapper.updateByPrimaryKeySelective(log);
            }
        } catch (Exception e) {
            LOGGER.error("平安银行代发交易查询返回报文解析失败", e);
        }
        /***处理返回结果-end*/
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
}
