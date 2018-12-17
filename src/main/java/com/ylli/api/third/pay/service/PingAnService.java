package com.ylli.api.third.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylli.api.pingan.PingAnTest;
import com.ylli.api.pingan.model.Packets;
import com.ylli.api.pingan.model.XmlRequestUtil;
import com.ylli.api.pingan.model.YQUtil;
import com.ylli.api.third.pay.model.PingAnOrder;
import com.ylli.api.third.pay.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PingAnService {

    private final static Logger LOGGER = LoggerFactory.getLogger(PingAnTest.class);

    //@Value("${b2bc.url}")
    public String url = "http://47.99.180.135:7072";

    @Autowired
    PingAnClient pingAnClient;

    /**
     * xml报文头
     */
    public final static String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    public void createPingAnOrder() {

        /**组装请求报文-start*/
        PingAnOrder order = new PingAnOrder();
        //required
        order.orderNumber = "20181217A0001";
        order.acctNo = "15000096544539";
        order.busiType = "00000";
        order.tranAmount = "100.00";
        order.inAcctNo = "6217920274920375";
        order.inAcctName = "李玉龙";
        //not required
        order.corpId = "";
        order.ccyCode = "RMB";
        order.inAcctBankName = "";
        order.inAcctBankNode = "";
        order.mobile = "";
        order.remark = "";
        order.inAcctProvinceName = "";
        order.inAcctCityName = "";

        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(order));

        System.out.println("平安单笔付款 KHKF03 请求xml：" + xml);

        //LOGGER.info("平安单笔付款 KHKF03 请求xml：" + xml);

        String reqXml = YQUtil.asemblyPackets("1002", "KHKF03", xml);
        /**组装请求报文-end*/


        //System.out.println(reqXml);
        //System.out.println(reqXml.replace(xml,"").length());

        /***处理返回结果-start*/
        String res = pingAnClient.orderTest(reqXml, url);
        System.out.println(res);

        /*Packets packets = HttpUtils.sendPost(reqXml, url);
        if (null == packets) {
            LOGGER.warn("globalSeq[{}]平安银行代发交易请求返回报文为空", globalSeq);
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
            LOGGER.warn("globalSeq[{}]平安银行代发报文解析异常", globalSeq);
            respDTO.setRespDTO(BankGatewayErrorCodeEnum.SEND_CHANNEL_EXCEPTION, globalSeq);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        }
        LOGGER.info("globalSeq[{}]平安银行代发交易请求响应报文头[{}]", globalSeq, returnHeadXml);
        LOGGER.info("globalSeq[{}]平安银行代发交易请求响应报文[{}]", globalSeq, returnXml);
        respDTO.setChannelRespCode(channleRespCode);
        respDTO.setChannelRespDesc(channleRespDesc);
        if (packets.getLen() == 0 || !"000000".equals(channleRespCode)) {
            LOGGER.warn("globalSeq[{}]平安银行代发交易失败,返回码[{}]返回描述[{}]", globalSeq, channleRespCode, channleRespDesc);
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
            LOGGER.error("globalSeq[{}]平安银行代发交易返回报文解析失败", globalSeq, e);
            respDTO.setRespDTO(BankGatewayErrorCodeEnum.RESULT_MESSAGE_EXCEPTION, globalSeq);
            respDTO.setStatus(TradeStatusEnum.PROCESSING.getStatus());
            return respDTO;
        }*/
        /***处理返回结果-end*/

    }


    public static void main(String[] args) {
        PingAnService service = new PingAnService();
        service.createPingAnOrder();


    }
}
