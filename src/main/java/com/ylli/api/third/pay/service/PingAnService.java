package com.ylli.api.third.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylli.api.pingan.PingAnTest;
import com.ylli.api.pingan.model.HttpUtils;
import com.ylli.api.pingan.model.Packets;
import com.ylli.api.pingan.model.XmlRequestUtil;
import com.ylli.api.pingan.model.YQUtil;
import com.ylli.api.third.pay.model.PingAnGR;
import com.ylli.api.third.pay.model.PingAnOrder;
import com.ylli.api.third.pay.model.PingAnQY;
import java.io.UnsupportedEncodingException;
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

        Packets packets = HttpUtils.sendPost(reqXml, url, orderNumber);

        String channleRespCode = null;
        String channleRespDesc = null;
        String returnXml = null;
        String returnHeadXml = null;
        try {
            returnHeadXml = new String(packets.getHead(), "UTF-8");
            channleRespCode = new String(packets.getHead(), 87, 6, "UTF-8");
            channleRespDesc = new String(packets.getHead(), 94, 99, "UTF-8");

            System.out.println(returnHeadXml);
            System.out.println(channleRespCode);
            System.out.println(channleRespDesc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        /*ResponseEntity<String> res = pingAnClient.orderTest(reqXml, url);
        if (res.getBody().contains("交易受理成功")) {
            System.out.println("订单号：" + orderNumber + "\n银企客户号：" + qy.acctNo + "\n卡号：" + gr.inAcctNo);
            System.out.println("start__________________________________________________>");
            System.out.println(res.getBody());
            System.out.println(res.getStatusCode());
            System.out.println("end____________________________________________________>");
        }*/
        /***处理返回结果-end*/

    }

    public static void main(String[] args) {
        String str = "A001010201010010343000045400000000000134KHKF03123450220181217142732YQTEST20181217142732000000:交易受理成功";
        System.out.println(str.contains("交易受理成功"));

    }
}
