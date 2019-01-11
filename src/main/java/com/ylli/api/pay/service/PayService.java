package com.ylli.api.pay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.model.MchKey;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.enums.Version;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.ConfirmResponse;
import com.ylli.api.pay.model.OrderConfirm;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.OrderQueryRes;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.model.ResponseEnum;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.sys.model.SysChannel;
import com.ylli.api.sys.service.ChannelService;
import com.ylli.api.third.pay.model.CTOrderResponse;
import com.ylli.api.third.pay.model.EazyResponse;
import com.ylli.api.third.pay.model.NotifyRes;
import com.ylli.api.third.pay.service.CTService;
import com.ylli.api.third.pay.service.CntService;
import com.ylli.api.third.pay.service.EazyPayService;
import com.ylli.api.third.pay.service.GPayService;
import com.ylli.api.third.pay.service.HRJFService;
import com.ylli.api.third.pay.service.UnknownPayService;
import com.ylli.api.third.pay.service.WzService;
import com.ylli.api.third.pay.service.YfbService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PayService {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PayService.class);

    @Autowired
    YfbService yfbService;

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    ChannelService channelService;

    @Autowired
    WzService wzService;

    @Autowired
    UnknownPayService unknownPayService;

    @Autowired
    BillService billService;

    @Autowired
    HRJFService hrjfService;

    @Autowired
    CntService cntService;

    @Autowired
    CTService ctService;

    @Autowired
    GPayService gPayService;

    @Autowired
    PayClient payClient;

    @Autowired
    EazyPayService eazyPayService;

    @Autowired
    BillMapper billMapper;

    public static final String ALI = "alipay";
    public static final String WX = "wx";

    public static final String NATIVE = "native";
    public static final String WAP = "wap";
    public static final String APP = "app";

    @Value("${ali.min}")
    public Integer Ali_MIN;

    @Value("${ali.max}")
    public Integer Ali_MAX;

    @Value("${cnt.min}")
    public Integer CNT_MIN;

    @Value("${cnt.max}")
    public Integer CNT_MAX;

    @Value("${server.release}")
    public Boolean release;

    public static String successCode = "0000";

    /**
     * 中央调度server. 根据情况选择不同通道
     *
     * @param baseOrder
     * @return
     */
    public Object createOrder(BaseOrder baseOrder) throws Exception {

        Response response = baseCheck(baseOrder);
        if (response != null) {
            return response;
        }

        //sign 前置校验
        String secretKey = mchKeyService.getKeyById(baseOrder.mchId);
        if (release) {
            response = signCheck(baseOrder, secretKey);
        }
        if (response != null) {
            return response;
        }

        SysChannel channel = channelService.getCurrentChannel(baseOrder.mchId);
        response = sysCheck(baseOrder, channel);
        if (response != null) {
            return response;
        }
        //默认 wap.
        if (Strings.isNullOrEmpty(baseOrder.tradeType)) {
            baseOrder.tradeType = WAP;
        }

        if (channel.code.equals("YFB")) {
            //易付宝支付
            return yfbOrder(baseOrder, channel.id, secretKey);

        } else if (channel.code.equals("WZ")) {
            //网众支付
            return wzOrder(baseOrder, channel.id, secretKey);

        } else if (channel.code.equals("unknown")) {
            //老个码支付
            return unknownOrder(baseOrder, channel.id, secretKey);

        } else if (channel.code.equals("HRJF")) {
            //华融巨富（新个码） 基本等同于 易付宝
            return hrjfOrder(baseOrder, channel.id, secretKey);

        } else if (channel.code.equals("CT")) {
            //畅通支付.
            if (!baseOrder.payType.equals(ALI)) {
                return new Response("A098", "临时限制：系统暂时只支持支付宝(pay_type = alipay)", baseOrder);
            }
            CTOrderResponse ctOrderResponse = ctService.createOrder(baseOrder.mchId, channel.id, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                    baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);
            if (!ctOrderResponse.result) {
                //下单失败
                Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
                bill.status = Bill.FAIL;
                billMapper.updateByPrimaryKeySelective(bill);
                return ResponseEnum.A099(ctOrderResponse.msg, null);
            } else {
                //下单成功
                return new Response("A000", "成功", successSign("A000", "成功", "url", ctOrderResponse.data, secretKey), "url", ctOrderResponse.data);
            }
        } else if (channel.code.equals("CNT")) {
            //cnt 支付.
            //cnt 加入版本 version = 1.1 。因为不会发起主动回调，需要商户主动确认。。
            if (!(Version.CNT.getVersion()).equals(baseOrder.version)) {
                return new Response("A011", "版本校验错误，当前通道对应支付版本version=1.1", baseOrder);
            }
            if (baseOrder.tradeType.equals(APP)) {
                return ResponseEnum.A003("支付方式暂不支持 app.", baseOrder);
            }
            String str = cntService.createOrder(baseOrder.mchId, channel.id, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                    baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);
            if (Strings.isNullOrEmpty(str)) {
                Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
                bill.status = Bill.FAIL;
                billMapper.updateByPrimaryKeySelective(bill);
                return ResponseEnum.A099("cnt response empty", null);
            }
            return new Response("A000", "成功", successSign("A000", "成功", "url", str, secretKey), "url", str);

        } else if (channel.code.equals("GP")) {
            //Gpay

            //微信只支持扫码  支付宝走 H5
            //暂时强制给商户转换。不去报错，免去切换通道带来的参数不兼容.....后续有问题再修改
            if (baseOrder.payType.equals(WX) && !baseOrder.tradeType.equals(NATIVE)) {
                baseOrder.tradeType = NATIVE;
            }
            if (baseOrder.payType.equals(ALI) && !baseOrder.tradeType.equals(WAP)) {
                baseOrder.tradeType = WAP;
            }
            String str = gPayService.createOrder(baseOrder.mchId, channel.id, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                    baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);

            if (str.startsWith("message")) {
                Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
                bill.status = Bill.FAIL;
                billMapper.updateByPrimaryKeySelective(bill);
                return ResponseEnum.A099(str, null);
            }
            return new Response("A000", "成功", successSign("A000", "成功", "url", str, secretKey), "url", str);
        } else if (channel.code.equals("EAZY")) {
            //eazy 支付
            if (!ALI.equals(baseOrder.payType)) {
                return ResponseEnum.A007("pay_type = alipay",baseOrder);
            }
            if (!WAP.equals(baseOrder.tradeType)) {
                return ResponseEnum.A008("trade_type = wap",baseOrder);
            }
            EazyResponse eazyResponse = eazyPayService.createOrder(baseOrder.mchId, channel.id, baseOrder.money,
                    baseOrder.mchOrderId, baseOrder.notifyUrl, baseOrder.redirectUrl, baseOrder.reserve,
                    baseOrder.payType, baseOrder.tradeType, baseOrder.extra);

            if (200 == eazyResponse.code) {
                return new Response("A000", "成功", successSign("A000", "成功", "url", eazyResponse.data.qrcode, secretKey), "url", eazyResponse.data.qrcode);
            } else {
                return ResponseEnum.A099(eazyResponse.msg, null);
            }
        } else {
            //
            return ResponseEnum.A099("暂无可用通道", null);
        }
    }

    /**
     * 参数合法性校验
     * <p>
     * 兼容初版设计. 默认 WAP
     */
    public Response baseCheck(BaseOrder baseOrder) {
        if (baseOrder.mchId == null || Strings.isNullOrEmpty(baseOrder.mchOrderId)
                || baseOrder.money == null || Strings.isNullOrEmpty(baseOrder.payType)
                || Strings.isNullOrEmpty(baseOrder.sign)) {
            return ResponseEnum.A003(null, baseOrder);
        }
        if (!payTypes.contains(baseOrder.payType)) {
            return ResponseEnum.A003("不支持的支付类型", baseOrder);
        }
        if (baseOrder.tradeType != null && !tradeTypes.contains(baseOrder.tradeType)) {
            return ResponseEnum.A003("不支持的支付方式", baseOrder);
        }
        return null;
    }

    /**
     * 签名校验
     */
    public Response signCheck(BaseOrder baseOrder, String secretKey) throws Exception {
        if (secretKey == null) {
            return ResponseEnum.A002(null, null);
        }
        if (isSignValid(baseOrder, secretKey)) {
            return ResponseEnum.A001(null, baseOrder);
        }
        return null;
    }

    /**
     * 系统校验
     */
    public Response sysCheck(BaseOrder baseOrder, SysChannel channel) {
        if (billService.mchOrderExist(baseOrder.mchOrderId)) {
            return ResponseEnum.A004(null, baseOrder);
        }
        // 通道关闭，不允许下单
        if (channel.state == false) {
            return new Response("A009", "当前通道关闭，请联系管理员切换通道");
        }
        if (baseOrder.payType.equals(ALI)) {

            if (channel.code.equals("CNT")) {
                //CNT   单独设置金额范围. 10 -9999
                if (baseOrder.money < CNT_MIN || baseOrder.money > CNT_MAX) {
                    return ResponseEnum.A005(String.format("%s - %s 元", CNT_MIN / 100, CNT_MAX / 100), baseOrder);
                }
            } else {
                //alipay金额限制 - sys
                if (baseOrder.money < Ali_MIN || baseOrder.money > Ali_MAX) {
                    return ResponseEnum.A005(String.format("%s - %s 元", Ali_MIN / 100, Ali_MAX / 100), baseOrder);
                }
            }
        } else if (baseOrder.payType.equals(WX)) {
            //平台微信限额
        }

        return null;
    }


    public Response hrjfOrder(BaseOrder baseOrder, Long channelId, String secretKey) throws Exception {
        //华荣聚付  -  和易付宝基本一致

        //不支持微信
        if (!baseOrder.payType.equals(ALI) || !baseOrder.tradeType.equals(WAP)) {
            return new Response("A098", "临时限制：系统暂时只支持支付宝wap（payType=alipay，tradeType=wap）", baseOrder);
        }

        String str = hrjfService.createOrder(baseOrder.mchId, channelId, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);

        if (Strings.isNullOrEmpty(str)) {
            //下单失败
            Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return ResponseEnum.A099("hrjf response empty", null);
        }
        if (str.startsWith("error")) {
            Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);

            return ResponseEnum.A099(str, null);

        } else if (str.startsWith("目前网关拥堵,请稍后再试")) {
            Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return ResponseEnum.A099("目前网关拥堵,请稍后再试(error:hrjf)", null);

        } else {
            str = str.replace("/ydpay/PayH5New.aspx", "http://gateway.iexindex.com/ydpay/PayH5New.aspx");
            //str = str.replace("/ydpay/Pay.aspx", "http://gateway.iexindex.com/ydpay/Pay.aspx");

            str = formToUrl(str);
            return new Response("A000", "成功", successSign("A000", "成功", "url", str, secretKey), "url", str);

        }
    }

    public Response unknownOrder(BaseOrder baseOrder, Long channelId, String secretKey) throws Exception {

        //支付方式校验
        if (!baseOrder.payType.equals(ALI) || baseOrder.tradeType.equals(APP)) {
            return new Response("A098", "临时限制：系统暂时只支持支付宝H5", baseOrder);
        }
        if (Strings.isNullOrEmpty(baseOrder.redirectUrl)) {
            baseOrder.redirectUrl = "http://www.baidu.com";
        }
        String str = unknownPayService.createOrder(baseOrder.mchId, channelId, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);

        if (Strings.isNullOrEmpty(str)) {
            //下单失败
            Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return ResponseEnum.A099("123 response empty", null);
        }

        return new Response("A000", "成功", successSign("A000", "成功", "form", str, secretKey), "form", str);
    }

    public Response wzOrder(BaseOrder baseOrder, Long channelId, String secretKey) throws Exception {
        //暂时只支持支付宝H5
        if (!ALI.equals(baseOrder.payType) || !WAP.equals(baseOrder.tradeType)) {
            return new Response("A098", "临时限制：系统暂时只支持支付宝H5（payType=alipay，tradeType=wap）", baseOrder);
        }
        if (Strings.isNullOrEmpty(baseOrder.redirectUrl)) {
            //兼容网众支付  商户跳转地址必填
            baseOrder.redirectUrl = "http://www.baidu.com";
        }

        String str = wzService.createOrder(baseOrder.mchId, channelId, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);

        if (Strings.isNullOrEmpty(str)) {
            //下单失败
            Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return ResponseEnum.A099("wz response empty", null);
        }
        return new Response("A000", "成功", successSign("A000", "成功", "url", str, secretKey), "url", str);
    }

    public Response yfbOrder(BaseOrder baseOrder, Long channelId, String secretKey) throws Exception {
        //通道私有金额校验.
        if (baseOrder.payType.equals(WX)) {
            //20 30 50 100 200 300 500
            if (!Wx_Allow.contains(baseOrder.money)) {
                return ResponseEnum.A005("微信 20，30，50，100，200，300，500 元", baseOrder);
            }
        }
        String str = yfbService.createOrder(baseOrder.mchId, channelId, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);

        if (Strings.isNullOrEmpty(str)) {
            //下单失败
            Bill bill = billService.selectByMchOrderId(baseOrder.mchOrderId);
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return ResponseEnum.A099("yfb response empty", null);
        }
        //return str;
        str = str.replace("/pay/alipay/scanpay.aspx", "http://api.qianyipay.com/pay/alipay/scanpay.aspx");
        str = str.replace("/pay/weixin/scanpay.aspx", "http://api.qianyipay.com/pay/weixin/scanpay.aspx");
        str = str.replace("/pay/alipay/wap.aspx", "http://api.qianyipay.com/pay/alipay/wap.aspx");
        str = str.replace("/pay/weixin/wap.aspx", "http://api.qianyipay.com/pay/weixin/wap.aspx");
        return new Response("A000", "成功", successSign("A000", "成功", "form", str, secretKey), "form", str);
    }

    public String formToUrl(String form) {
        form = form.replace("<form name=\"payform\" id=\"payform\" method=\"post\" action=\"http://gateway.iexindex.com/ydpay/PayH5New.aspx\">", "");
        form = form.replace("</form>", "");
        form = form.replace("<script type=\"text/javascript\" language=\"javascript\">function go(){ var _form = document.forms['payform']; _form.submit();};setTimeout(function(){go()},100);</script>", "");

        String price =
                StringUtils.substringBefore(StringUtils.substringAfter(form, "name=\"price\" value=\""), "\"");

        String istype =
                StringUtils.substringBefore(StringUtils.substringAfter(form, "name=\"istype\" value=\""), "\"");

        String return_url =
                StringUtils.substringBefore(StringUtils.substringAfter(form, "name=\"return_url\" value=\""), "\"");

        String payurl =
                StringUtils.substringBefore(StringUtils.substringAfter(form, "name=\"payurl\" value=\""), "\"");

        String id =
                StringUtils.substringBefore(StringUtils.substringAfter(form, "name=\"id\" value=\""), "\"");

        String url = UriComponentsBuilder.fromHttpUrl(
                "http://gateway.iexindex.com/ydpay/PayH5New.aspx")
                .queryParam("price", price)
                .queryParam("istype", istype)
                .queryParam("return_url", return_url)
                .queryParam("payurl", payurl)
                .queryParam("id", id)
                .build().toUriString();
        return url;
    }


    public static List<Integer> Wx_Allow = new ArrayList<Integer>() {
        {
            add(2000);
            add(3000);
            add(5000);
            add(10000);
            add(20000);
            add(30000);
            add(50000);
        }
    };

    public static List<String> payTypes = new ArrayList<String>() {
        {
            add(ALI);
            add(WX);
        }
    };

    public static List<String> tradeTypes = new ArrayList<String>() {
        {
            add(NATIVE);
            add(APP);
            add(WAP);
        }
    };

    public boolean isSignValid(BaseOrder order, String secretKey) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(order);
        return !SignUtil.generateSignature(map, secretKey).equals(order.sign.toUpperCase());
    }

    public boolean isSignValid(OrderConfirm confirm, String secretKey) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(confirm);
        return !SignUtil.generateSignature(map, secretKey).equals(confirm.sign.toUpperCase());
    }

    public String successSign(String code, String message, String type, Object data, String key) throws Exception {
        Response response = new Response(code, message, data);
        response.type = type;
        Map<String, String> map = SignUtil.objectToMap(response);
        return SignUtil.generateSignature(map, key);
    }


    @Transactional
    public Object orderQuery(OrderQueryReq orderQuery) throws Exception {
        String key = mchKeyService.getKeyById(orderQuery.mchId);
        Map<String, String> map = SignUtil.objectToMap(orderQuery);
        String sign = SignUtil.generateSignature(map, key);
        if (sign.equals(orderQuery.sign.toUpperCase())) {

            SysChannel channel = channelService.getCurrentChannel(orderQuery.mchId);

            Bill bill = billService.selectByMchOrderId(orderQuery.mchOrderId);
            if (bill == null) {
                return ResponseEnum.A006(null, null);
            }
            if (bill.status == Bill.FINISH || bill.status == Bill.FAIL) {

            } else {
                bill = billService.orderQuery(bill.sysOrderId, channel.code);
            }
            //直接返回订单信息
            OrderQueryRes res = new OrderQueryRes();
            res.code = "A000";
            res.message = "成功";
            res.money = bill.money;
            res.mchOrderId = bill.mchOrderId;
            res.sysOrderId = bill.sysOrderId;
            res.status = bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I";
            if (bill.tradeTime != null) {
                res.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime);
            }

            Map<String, String> map1 = SignUtil.objectToMap(res);
            res.sign = SignUtil.generateSignature(map1, key);
            return res;
        }
        return new OrderQueryRes("A001", "签名校验失败");
    }

    /**
     * 金额
     * 子系统订单干号
     * 我们系统订单号
     * 状态
     * 交易时间
     * sign
     * 保留域
     */
    public String generateRes(String money, String mchOrderId, String sysOrderId, String status, String tradeTime, String reserve) throws Exception {
        NotifyRes res = new NotifyRes();
        res.money = money;
        res.mchOrderId = mchOrderId;
        res.sysOrderId = sysOrderId;
        res.status = status;
        res.tradeTime = tradeTime;
        res.reserve = reserve;
        Bill bill = billService.selectBySysOrderId(sysOrderId);
        MchKey key = mchKeyService.getKeyByUserId(bill.mchId);

        Map<String, String> map = SignUtil.objectToMap(res);
        res.sign = SignUtil.generateSignature(map, key.secretKey);
        return new Gson().toJson(res);
    }

    public void paynotify(HttpServletRequest request, HttpServletResponse response) {
        InputStream inputStream;
        OutputStream outputStream = null;
        try {
            //读取参数
            StringBuffer sb = new StringBuffer();
            inputStream = request.getInputStream();
            String s;
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
            inputStream.close();
            outputStream = response.getOutputStream();
            LOGGER.info("进入模拟商户回调：\n");
            LOGGER.info(sb.toString());
            //System.out.println(sb.toString());
            if (sb.length() == 0) {
                outputStream.write("empty".getBytes("UTF-8"));
                return;
            }
            //verify sign
            //boolean flag = signVerify(sb.toString(), key);
            boolean flag = true;
            if (flag) {
                //业务处理
                LOGGER.info("商户业务处理...");

                outputStream.write("success".getBytes("UTF-8"));
            } else {
                outputStream.write("sign error".getBytes("UTF-8"));
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Object payConfirm(OrderConfirm confirm, Boolean isMch) throws Exception {

        if (isMch) {
            String secretKey = mchKeyService.getKeyById(confirm.mchId);

            if (isSignValid(confirm, secretKey)) {
                return ResponseEnum.A001(null, confirm);
            }
        }

        //根据商户定单号查询商户定单，获取上游定单号
        Bill bill = billService.selectByMchOrderId(confirm.mchOrderId);
        if (bill == null) {
            return ResponseEnum.A006(null, null);
        }
        //根据上游定单号通知上游确认支付
        try {
            String str = cntService.confirm(bill.superOrderId, bill.reserve);
            ConfirmResponse response = new Gson().fromJson(str, ConfirmResponse.class);
            //TODO 无论支付都会返回付款成功
            if (successCode.equals(response.resultCode)) {
                return new Response("A000", "成功", bill.reserveWord);
            } else {
                //TODO 确认失败. 是否需要更改订单状态？
                return new Response("A010", "失败:" + response.resultMsg);
            }
        } catch (Exception e) {
            return new Response("A010", "失败,当前服务状态异常。");
        }
    }

    public Object manualNotify(String mchOrderId) throws Exception {
        Bill bill = billService.selectByMchOrderId(mchOrderId);
        if (bill == null) {
            return "bill not found";
        }
        if (!Strings.isNullOrEmpty(bill.notifyUrl)) {
            String params = generateRes(
                    bill.money.toString(),
                    bill.mchOrderId,
                    bill.sysOrderId,
                    bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                    bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                    bill.reserve);

            payClient.sendNotify(bill.id, bill.notifyUrl, params, true);
        }

        return "ok";
    }
}
