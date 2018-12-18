package com.ylli.api.third.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylli.api.pingan.PingAnTest;
import com.ylli.api.pingan.model.TimeUtil;
import com.ylli.api.pingan.model.XmlRequestUtil;
import com.ylli.api.pingan.model.YQUtil;
import com.ylli.api.third.pay.model.PingAnGR;
import com.ylli.api.third.pay.model.PingAnOrder;
import com.ylli.api.third.pay.model.PingAnOrderQuery;
import com.ylli.api.third.pay.model.PingAnQY;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
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

    //企业签约帐号
    public String acctNo = "";
    //单位代码
    public String corpId = "Q000201184";
    //银企代码
    public String yqdm = "01001034300004537000";

    //收款卡号
    public String inAcctNo = "6226330151030000";
    //收款户名
    public String inAcctName = "张小花";
    //收款方银行名称
    public String inAcctBankName = "华夏";
    //收款方手机号
    public String mobile = "";


    /**
     * xml报文头
     */
    public final static String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    public void createPingAnOrder(String orderNumber, PingAnQY qy, PingAnGR gr) {

        /**组装请求报文-start*/
        PingAnOrder order = new PingAnOrder();
        //required
        order.orderNumber = orderNumber;
        order.acctNo = qy.acctNo;
        order.busiType = "00000";
        //金额转换-分转换为元
        //String amount = (new BigDecimal(bankPayReqDTO.getAmount()).divide(new BigDecimal(100))).toString();
        order.tranAmount = "100";
        order.inAcctNo = gr.inAcctNo;
        order.inAcctName = gr.inAcctName;
        //not required
        order.corpId = qy.corpId;
        order.ccyCode = "RMB";
        order.inAcctBankName = gr.inAcctBankName;
        order.inAcctBankNode = "";
        order.mobile = gr.mobile;
        order.remark = "";
        order.inAcctProvinceName = "";
        order.inAcctCityName = "";

        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(order));

        String reqXml = YQUtil.asemblyPackets(qy.yqdm, "KHKF03", xml);
        /**组装请求报文-end*/

        String res = pingAnClient.post(reqXml, url);
        if (res == null) {
            //todo


        }
        String code = StringUtils.substringBefore(res, ":").substring(87);
        if (code != "000000") {
            //平安银行代发交易查询失败
            String msg = StringUtils.substringBefore(res.substring(94), "0").trim();
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
            String orderNo = document.getRootElement().element("OrderNumber").getText();
            System.out.println(bussFlowNo);
            System.out.println(orderNo);
        }
        /***处理返回结果-end*/

    }


    /**
     * 跨行快付查询KHKF04
     *
     * @return
     */
    public String payQuery() {
        /**组装请求报文-start**/
        PingAnOrderQuery query = new PingAnOrderQuery();
        query.acctNo = "15000090253679";
        query.bussFlowNo = "8043431812186167968178";
        query.orderNumber = "5133";

        String xml = XML_HEAD + XmlRequestUtil.createXmlRequest((JSONObject) JSON.toJSON(query));
        String reqXml = YQUtil.asemblyPackets("01001034300004539000", "KHKF04", xml);
        /**组装请求报文-end**/


        /***处理返回结果-start*/
        String res = pingAnClient.post(reqXml, url);
        if (res == null) {
            //todo
            return "empty";
        }
        System.out.println(res);

        //获取返回code
        String code = StringUtils.substringBefore(res, ":").substring(87);
        if (!"000000".equals(code)) {
            /**
             * 平安银行代发交易查询失败
             * YQ9996 查询不存在交易返回码
             * 订单创建时间于当前时间小于1分钟，返回处理中；否则返回交易失败
             */
            if ("YQ9996".equals(code)) {
                //todo tradeTime 是该笔订单的创建时间...
                Date tradeTime = new Date();
                if (tradeTime.compareTo(TimeUtil.nextTime(new Date(), null, -1, null)) < 0) {
                    //交易失败
                    return "fail";
                }
            }
            //交易进行中
            return "processing";
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
                return "success";
            }
            //失败
            else if ("30".equals(stt)) {
                return "fail";
            }
            //处理中
            else {
                return "processing";
            }
        } catch (Exception e) {
            LOGGER.error("globalSeq[{}]平安银行代发交易查询返回报文解析失败", e);
        }
        return "";
        /***处理返回结果-end*/
    }
}
