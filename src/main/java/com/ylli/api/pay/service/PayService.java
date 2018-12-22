package com.ylli.api.pay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.model.MchKey;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.OrderQueryRes;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.sys.model.SysChannel;
import com.ylli.api.sys.service.ChannelService;
import com.ylli.api.third.pay.model.NotifyRes;
import com.ylli.api.third.pay.service.UnknownPayClient;
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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayService {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PayService.class);

    @Autowired
    YfbService yfbService;

    @Autowired
    MchKeyService userKeyService;

    @Autowired
    ChannelService channelService;

    @Autowired
    WzService wzService;

    @Autowired
    UnknownPayService unknownPayService;

    @Autowired
    BillService billService;

    @Autowired
    SerializeUtil serializeUtil;

    @Autowired
    UnknownPayClient unknownPayClient;

    public static final String ALI = "alipay";
    public static final String WX = "wx";

    public static final String NATIVE = "native";
    public static final String WAP = "wap";
    public static final String APP = "app";

    @Value("${yfb.ali.min}")
    public Integer Ali_Min;

    @Value("${yfb.ali.max}")
    public Integer Ali_Max;

    /**
     * 中央调度server. 根据情况选择不同通道
     *
     * @param baseOrder
     * @return
     */
    public Object createOrder(BaseOrder baseOrder) throws Exception {

        if (baseOrder.mchId == null || Strings.isNullOrEmpty(baseOrder.mchOrderId)
                || baseOrder.money == null || Strings.isNullOrEmpty(baseOrder.payType)
                || Strings.isNullOrEmpty(baseOrder.sign)) {
            return new Response("A003", "非法的请求参数", baseOrder);
        }

        //sign 前置校验
        String secretKey = userKeyService.getKeyById(baseOrder.mchId);
        if (secretKey == null) {
            return new Response("A002", "请先上传商户私钥", null);
        }
        if (!payTypes.contains(baseOrder.payType)) {
            return new Response("A004", "不支持的支付类型", baseOrder);
        }
        if (baseOrder.tradeType != null && !tradeTypes.contains(baseOrder.tradeType)) {
            return new Response("A008", "不支持的支付方式", baseOrder);
        }
        if (isSignValid(baseOrder, secretKey)) {
            return new Response("A001", "签名校验失败", baseOrder);
        }
        SysChannel channel = channelService.getCurrentChannel(baseOrder.mchId);
        // v1.1 新增 通道关闭的话。不允许下单
        if (channel.state == false) {
            return new Response("A009", "当前通道关闭，请联系管理员切换通道");
        }
        if (billService.mchOrderExist(baseOrder.mchOrderId)) {
            return new Response("A005", "订单号重复", baseOrder);
        }

        if (channel.code.equals("YFB")) {
            //易付宝支付

            //订单金额校验.
            if (baseOrder.payType.equals(ALI)) {
                //10-9999
                if (baseOrder.money < Ali_Min || baseOrder.money > Ali_Max) {
                    return new Response("A007", "交易金额限制：支付宝 100 -9999 元", baseOrder);
                }
            } else if (baseOrder.payType.equals(WX)) {
                //20 30 50 100 200 300 500
                if (!Wx_Allow.contains(baseOrder.money)) {
                    return new Response("A007", "交易金额限制：微信 20，30，50，100，200，300，500 元", baseOrder);
                }
            }
            String str = yfbService.createOrder(baseOrder.mchId, channel.id, baseOrder.payType, baseOrder.tradeType, baseOrder.money,
                    baseOrder.mchOrderId, baseOrder.notifyUrl, baseOrder.redirectUrl, baseOrder.reserve, baseOrder.extra);
            //return str;
            str = str.replace("/pay/alipay/scanpay.aspx", "http://api.qianyipay.com/pay/alipay/scanpay.aspx");
            str = str.replace("/pay/weixin/scanpay.aspx", "http://api.qianyipay.com/pay/weixin/scanpay.aspx");
            str = str.replace("/pay/alipay/wap.aspx", "http://api.qianyipay.com/pay/alipay/wap.aspx");
            str = str.replace("/pay/weixin/wap.aspx", "http://api.qianyipay.com/pay/weixin/wap.aspx");
            return new Response("A000", "成功", successSign("A000", "成功", "form", str, secretKey), "form", str);
        } else if (channel.code.equals("WZ")) {
            //暂时只支持支付宝H5
            if (!ALI.equals(baseOrder.payType) || !WAP.equals(baseOrder.tradeType)) {
                return new Response("A098", "临时限制：系统暂时只支持支付宝H5（payType=alipay，tradeType=wap）", baseOrder);
            }

            //金额限制，低于10元 && 费率低于 1% 存在金额精度丢失。（按分计）
            if (baseOrder.money < Ali_Min) {
                return new Response("A007", "交易金额限制：当前最低交易金额100元", baseOrder);
            }
            if (Strings.isNullOrEmpty(baseOrder.redirectUrl)) {
                //兼容网众支付  商户跳转地址必填
                baseOrder.redirectUrl = "http://www.baidu.com";
            }

            String str = wzService.createOrder(baseOrder.mchId, channel.id, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                    baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);

            return new Response("A000", "成功", successSign("A000", "成功", "url", str, secretKey), "url", str);

        } else if (channel.code.equals("unknown")) {
            // ?? unknown 支付
            //金额校验
            if (baseOrder.money < Ali_Min || baseOrder.money > Ali_Max) {
                return new Response("A007", "交易金额限制：支付宝 100 -9999 元", baseOrder);
            }
            //支付方式校验
            if (!baseOrder.payType.equals(ALI) || baseOrder.tradeType.equals(APP)) {
                return new Response("A098", "临时限制：系统暂时只支持支付宝H5", baseOrder);
            }
            if (Strings.isNullOrEmpty(baseOrder.redirectUrl)) {
                baseOrder.redirectUrl = "http://www.baidu.com";
            }
            String str = unknownPayService.createOrder(baseOrder.mchId, channel.id, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl,
                    baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);

            return new Response("A000", "成功", successSign("A000", "成功", "form", str, secretKey), "form", str);
        } else {
            //

            return new Response("A099", "下单失败，暂无可用通道", null);
        }
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

    public String successSign(String code, String message, String type, Object data, String key) throws Exception {
        Response response = new Response(code, message, data);
        response.type = type;
        Map<String, String> map = SignUtil.objectToMap(response);
        return SignUtil.generateSignature(map, key);
    }


    @Transactional
    public Object orderQuery(OrderQueryReq orderQuery) throws Exception {
        String key = userKeyService.getKeyById(orderQuery.mchId);
        Map<String, String> map = SignUtil.objectToMap(orderQuery);
        String sign = SignUtil.generateSignature(map, key);
        if (sign.equals(orderQuery.sign.toUpperCase())) {

            SysChannel channel = channelService.getCurrentChannel(orderQuery.mchId);

            Bill bill = billService.selectByMchOrderId(orderQuery.mchOrderId);
            if (bill == null) {
                return new OrderQueryRes("A006", "订单不存在");
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
        MchKey key = userKeyService.getKeyByUserId(bill.mchId);

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

    /*public Object testRedis() {
        List<String> list = new ArrayList<>();
        Long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        list.add("start当前时间：" + sdf.format(time));
        for (int i = 0; i < 1000; i++) {
            list.add(serializeUtil.generateSysOrderId());
        }
        list.add("end当前时间：" + sdf.format(System.currentTimeMillis()));
        list.add("size:" + (list.size() - 3));
        return list;
    }*/


    /*public Object testu(Integer count) throws Exception {
        List<String> list = new ArrayList<>();
        Long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        list.add("start当前时间：" + sdf.format(time));
        for (int i = 0; i < count; i++) {
            String sysOrderId = new StringBuffer("test").append(serializeUtil.generateSysOrderId()).toString() ;
            list.add("当前订单号：" + sysOrderId);
            String str = unknownPayClient.createOrder("1", 1, "test", sysOrderId, get20UUID(), 2);
            list.add(str);
        }
        list.add("end当前时间：" + sdf.format(System.currentTimeMillis()));
        list.add("size:" + (list.size() - 3));
        return list;
    }

    public String get20UUID() {
        UUID id = UUID.randomUUID();
        String[] idd = id.toString().split("-");
        return idd[0] + idd[1] + idd[2] + idd[3];
    }*/
}
