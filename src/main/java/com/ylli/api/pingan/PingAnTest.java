package com.ylli.api.pingan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylli.api.pingan.model.BankGatewayErrorCodeEnum;
import com.ylli.api.pingan.model.BankPayReqDTO;
import com.ylli.api.pingan.model.BankPayRespDTO;
import com.ylli.api.pingan.model.BankQueryReqDTO;
import com.ylli.api.pingan.model.BankQueryRespDTO;
import com.ylli.api.pingan.model.ErrorCodeEnum;
import com.ylli.api.pingan.model.HttpUtils;
import com.ylli.api.pingan.model.Packets;
import com.ylli.api.pingan.model.PayConfig;
import com.ylli.api.pingan.model.PayeeInfo;
import com.ylli.api.pingan.model.PayerInfo;
import com.ylli.api.pingan.model.ReqKHKF03;
import com.ylli.api.pingan.model.ReqKHKF04;
import com.ylli.api.pingan.model.TimeUtil;
import com.ylli.api.pingan.model.TradeStatusEnum;
import com.ylli.api.pingan.model.XmlRequestUtil;
import com.ylli.api.pingan.model.YQUtil;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PingAnTest {

    private final static Logger logger = LoggerFactory.getLogger(PingAnTest.class);

    @Autowired
    private PayConfig payConfig;


    /**
     * xml报文头
     */
    public final static String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    /**
     * 跨行快付申请KHKF03
     *
     * @param bankPayReqDTO
     * @return
     */
    //@Override
    public BankPayRespDTO pay(BankPayReqDTO bankPayReqDTO) {
        String globalSeq = bankPayReqDTO.getGlobalSeq();
        BankPayRespDTO respDTO = new BankPayRespDTO();
        respDTO.setRespDTO(ErrorCodeEnum.SUCCESS, globalSeq);
        PayeeInfo payeeInfo = bankPayReqDTO.getPayeeInfo();
        PayerInfo payerInfo = bankPayReqDTO.getPayerInfo();
        //金额转换-分转换为元
        String amount = (new BigDecimal(bankPayReqDTO.getAmount()).divide(new BigDecimal(100))).toString();

        /**组装请求报文-start**/
        ReqKHKF03 req = new ReqKHKF03();
        req.setOrderNumber(bankPayReqDTO.getOrderNo());
        req.setCcyCode("RMB");
        req.setAcctNo(payerInfo.getPayerAccountNo());
        req.setBusiType("00000");
        req.setTranAmount(amount);
        req.setInAcctNo(payeeInfo.getPayeeAccountNo());
        req.setInAcctName(payeeInfo.getPayeeAccountName());
        req.setInAcctBankName(payeeInfo.getPayeeBankName());
        // req.setInAcctBankNode(payeeInfo.getPayeeBankBranchId());
        req.setRemark(bankPayReqDTO.getDesc());
        // req.setInAcctProvinceName("");//TODO
        //req.setInAcctCityName("");//TODO
        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(req));
        logger.info("globalSeq[{}]平安银行代发交易请求发送报文[{}]", globalSeq, xml);

        String reqXml = YQUtil.asemblyPackets(bankPayReqDTO.getChannelMerchantId(), "KHKF03", xml);
        /**组装请求报文-end**/

        /***处理返回结果-start*/
        Packets packets = HttpUtils.sendPost(reqXml, payConfig.getRemoteUrl(), globalSeq);
        if (null == packets) {
            logger.warn("globalSeq[{}]平安银行代发交易请求返回报文为空", globalSeq);
            respDTO.setRespDTO(BankGatewayErrorCodeEnum.SEND_CHANNEL_EXCEPTION, globalSeq);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        }
        String channleRespCode = null;
        String channleRespDesc = null;
        String returnXml = null;
        String returnHeadXml = null;
        try {
            returnHeadXml = new String(packets.getHead(), "UTF-8");
            channleRespCode = new String(packets.getHead(), 87, 6, "UTF-8");
            channleRespDesc = new String(packets.getHead(), 94, 99, "UTF-8");
            if (packets.getBody() != null) {
                returnXml = new String(packets.getBody(), "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            logger.warn("globalSeq[{}]平安银行代发报文解析异常", globalSeq);
            respDTO.setRespDTO(BankGatewayErrorCodeEnum.SEND_CHANNEL_EXCEPTION, globalSeq);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        }
        logger.info("globalSeq[{}]平安银行代发交易请求响应报文头[{}]", globalSeq, returnHeadXml);
        logger.info("globalSeq[{}]平安银行代发交易请求响应报文[{}]", globalSeq, returnXml);
        respDTO.setChannelRespCode(channleRespCode);
        respDTO.setChannelRespDesc(channleRespDesc);
        if (packets.getLen() == 0 || !"000000".equals(channleRespCode)) {
            logger.warn("globalSeq[{}]平安银行代发交易失败,返回码[{}]返回描述[{}]", globalSeq, channleRespCode, channleRespDesc);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());//失败
            return respDTO;
        }
        try {
            Document document = DocumentHelper.parseText(returnXml);
            String FrontLogNo = document.getRootElement().element("BussFlowNo").getText();
            respDTO.setOutSerialNo(FrontLogNo);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        } catch (DocumentException e) {
            logger.error("globalSeq[{}]平安银行代发交易返回报文解析失败", globalSeq, e);
            respDTO.setRespDTO(BankGatewayErrorCodeEnum.RESULT_MESSAGE_EXCEPTION, globalSeq);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        }
        /***处理返回结果-end*/
    }

    /**
     * 跨行快付查询KHKF04
     *
     * @param bankQueryReqDTO
     * @return
     */
    //@Override
    public BankQueryRespDTO payQuery(BankQueryReqDTO bankQueryReqDTO) {
        String globalSeq = bankQueryReqDTO.getGlobalSeq();
        BankQueryRespDTO respDTO = new BankQueryRespDTO();
        respDTO.setRespDTO(ErrorCodeEnum.SUCCESS, globalSeq);
        PayerInfo payerInfo = bankQueryReqDTO.getPayerInfo();

        /**组装请求报文-start**/
        ReqKHKF04 req = new ReqKHKF04();
        req.setAcctNo(payerInfo.getPayerAccountNo());
        req.setBussFlowNo(bankQueryReqDTO.getOutOrderNo());
        req.setOrderNumber(bankQueryReqDTO.getOriOrderNo());
        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(req));
        String reqXml = YQUtil.asemblyPackets(bankQueryReqDTO.getChannelMerchantId(), "KHKF04", xml);
        logger.info("globalSeq[{}]平安银行代发交易查询请求发送报文[{}]", globalSeq, xml);
        /**组装请求报文-end**/

        /***处理返回结果-start*/
        Packets packets = HttpUtils.sendPost(reqXml, payConfig.getRemoteUrl(), globalSeq);
        if (null == packets) {
            logger.warn("globalSeq[{}]平安银行代发交易查询请求返回报文为空", globalSeq);
            respDTO.setRespDTO(BankGatewayErrorCodeEnum.SEND_CHANNEL_EXCEPTION, globalSeq);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        }
        String channleRespCode = null;
        String channleRespDesc = null;
        String returnXml = null;
        String returnHeadXml = null;
        try {
            returnHeadXml = new String(packets.getHead(), "UTF-8");
            channleRespCode = new String(packets.getHead(), 87, 6, "UTF-8");
            channleRespDesc = new String(packets.getHead(), 94, 99, "UTF-8");
            respDTO.setChannelRespCode(channleRespCode);
            respDTO.setChannelRespDesc(channleRespDesc);
            Date tradeTime = bankQueryReqDTO.getTradeTime();
            if (packets.getLen() == 0 || !"000000".equals(channleRespCode)) {
                logger.warn("globalSeq[{}]平安银行代发交易查询失败,返回码[{}]返回描述[{}]", globalSeq, channleRespCode, channleRespDesc);
                if ("YQ9996".equals(channleRespCode)) {
                    if (tradeTime.compareTo(TimeUtil.nextTime(new Date(), null, -1, null)) < 0) {
                        respDTO.setStatus(TradeStatusEnum.FAIL.getStatus());//失败
                        return respDTO;
                    }
                }
                respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());//失败
                return respDTO;
            }
            returnXml = new String(packets.getBody(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warn("globalSeq[{}]平安银行代发交易查询报文解析异常", globalSeq);
            respDTO.setRespDTO(BankGatewayErrorCodeEnum.SEND_CHANNEL_EXCEPTION, globalSeq);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        }
        logger.info("globalSeq[{}]平安银行代发交易查询请求响应报文头[{}]", globalSeq, returnHeadXml);
        logger.info("globalSeq[{}]平安银行代发交易查询请求响应报文[{}]", globalSeq, returnXml);
        try {
            Document document = DocumentHelper.parseText(returnXml);
            String FrontLogNo = document.getRootElement().element("BussFlowNo").getText();
            String stt = document.getRootElement().element("Status").getText();
            String RetCode = document.getRootElement().element("RetCode").getText();
            String RetMsg = document.getRootElement().element("RetMsg").getText();
            respDTO.setChannelRespCode(RetCode);
            respDTO.setChannelRespDesc(RetMsg);
            respDTO.setOutSerialNo(FrontLogNo);
            //成功
            if ("20".equals(stt)) {
                respDTO.setStatus(TradeStatusEnum.SUCCESS.getStatus());
            }
            //失败
            else if ("30".equals(stt)) {
                respDTO.setStatus(TradeStatusEnum.FAIL.getStatus());
            }
            //处理中
            else {
                respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            }
            return respDTO;
        } catch (Exception e) {
            logger.error("globalSeq[{}]平安银行代发交易查询返回报文解析失败", globalSeq, e);
            respDTO.setRespDTO(BankGatewayErrorCodeEnum.RESULT_MESSAGE_EXCEPTION, globalSeq);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        }
        /***处理返回结果-end*/
    }

}
