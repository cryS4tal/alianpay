package com.ylli.api.third.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylli.api.pingan.PingAnTest;
import com.ylli.api.pingan.model.BankGatewayErrorCodeEnum;
import com.ylli.api.pingan.model.HttpUtils;
import com.ylli.api.pingan.model.Packets;
import com.ylli.api.pingan.model.TradeStatusEnum;
import com.ylli.api.pingan.model.XmlRequestUtil;
import com.ylli.api.pingan.model.YQUtil;
import com.ylli.api.third.pay.model.PingAnGR;
import com.ylli.api.third.pay.model.PingAnOrder;
import com.ylli.api.third.pay.model.PingAnQY;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

        /*Packets packets = HttpUtils.sendPost(reqXml, url, orderNumber);

        String returnXml = null;
        try {
            if (packets.getBody() != null) {
                returnXml = new String(packets.getBody(), "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            Document document = DocumentHelper.parseText(returnXml);
            String FrontLogNo = document.getRootElement().element("BussFlowNo").getText();
        } catch (DocumentException e) {
            LOGGER.error("globalSeq[{}]平安银行代发交易返回报文解析失败", e);
        }*/






        ResponseEntity<String> res = pingAnClient.orderTest(reqXml, url);
        if (res.getBody().contains("交易受理成功")) {
            //System.out.println("订单号：" + orderNumber + "\n银企客户号：" + qy.acctNo + "\n卡号：" + gr.inAcctNo);
            System.out.println("start__________________________________________________>");
            //A001010201010010343000045390000000000134KHKF03123450220181218033745YQTEST20181218033745000000:交易受理成功                                                                                 000001            00000000000<?xml version="1.0" encoding="UTF-8" ?><Result><OrderNumber>5111</OrderNumber><BussFlowNo>8043431812186167923315</BussFlowNo></Result>
            String body =res.getBody();


            System.out.println("end____________________________________________________>");
        }
        /***处理返回结果-end*/

    }

    public static void main(String[] args) {
        String str = "A001010201010010343000045390000000000134KHKF03123450220181218033745YQTEST20181218033745000000:交易受理成功                                                                                 000001            00000000000<?xml version=\"1.0\" encoding=\"UTF-8\" ?><Result><OrderNumber>5111</OrderNumber><BussFlowNo>8043431812186167923315</BussFlowNo></Result>\n";
        String xml = StringUtils.substringAfter(str,"?>");
        Document document = null;
        try {
            document = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        String FrontLogNo = document.getRootElement().element("BussFlowNo").getText();
        System.out.println(FrontLogNo);
    }
}
