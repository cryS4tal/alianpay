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

        String res = pingAnClient.KHKF03(reqXml, url);
        if (res == null) {
            //todo


        }
        System.out.println(res);

        if (res.contains("交易受理成功")) {
            //A001010201010010343000045390000000000134KHKF03123450220181218033745YQTEST20181218033745000000:交易受理成功                                                                                 000001            00000000000<?xml version="1.0" encoding="UTF-8" ?><Result><OrderNumber>5111</OrderNumber><BussFlowNo>8043431812186167923315</BussFlowNo></Result>
            String returnXml = StringUtils.substringAfter(res,"?>");
            Document document = null;
            try {
                document = DocumentHelper.parseText(returnXml);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            String frontLogNo = document.getRootElement().element("BussFlowNo").getText();
            String orderNo = document.getRootElement().element("OrderNumber").getText();
            System.out.println(frontLogNo);
            System.out.println(orderNo);
        } else {
            String msg = StringUtils.substringBefore(res.substring(94),"0").trim();
            System.out.println(msg);
        }
        /***处理返回结果-end*/

    }

    public static void main(String[] args) {
        /*String str = "A001010201010010343000045390000000000134KHKF03123450220181218033745YQTEST20181218033745000000:交易受理成功                                                                                 000001            00000000000<?xml version=\"1.0\" encoding=\"UTF-8\" ?><Result><OrderNumber>5111</OrderNumber><BussFlowNo>8043431812186167923315</BussFlowNo></Result>\n";
        String xml = StringUtils.substringAfter(str,"?>");
        Document document = null;
        try {
            document = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        String FrontLogNo = document.getRootElement().element("BussFlowNo").getText();
        System.out.println(FrontLogNo);*/

        /*String str1 = "A001010201010010343000045370000000000000KHKF03123450220181218064757YQTEST20181218064757BIB006";
        String str2 = "A001010201010010343000045380000000000000KHKF03123450220181218065033YQTEST201812180650335223  ";
        String str3 = "A001010201010010343000045390000000000134KHKF03123450220181218065051YQTEST20181218065051000000";
        String str4 = "A001010201010010343000045400000000000134KHKF03123450220181218065108YQTEST20181218065108000000";
        String str5 = "A001010201010010343000045410000000000000KHKF03123450220181218065125YQTEST201812180651255223  ";
        System.out.println(str1.length());
        System.out.println(str2.length());
        System.out.println(str3.length());
        System.out.println(str4.length());
        System.out.println(str5.length());*/
    }
}
