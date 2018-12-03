package com.ylli.api.pay.service;

import com.google.common.base.Strings;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.OrderQueryRes;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.user.service.UserKeyService;
import com.ylli.api.yfbpay.model.YfbBill;
import com.ylli.api.yfbpay.service.YfbService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayService {

    //todo 管理员后台加入通道管理，选择不同通道

    @Autowired
    YfbService yfbService;

    @Autowired
    UserKeyService userKeyService;

    public static final String Ali = "alipay";
    public static final String Wx = "wx";


    @Value("${yfb.ali.min}")
    public Integer Ali_Min;

    @Value("${yfb.ali.max}")
    public Integer Ali_Max;

    /**
     * 中央调度server. 根据情况选择不同通道
     * 支付系统商户号为用户id. 代付系统系统生成string 商户号
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
        if (isSignValid(baseOrder, secretKey)) {
            return new Response("A001", "签名校验失败", baseOrder);
        }

        //加入系统通道管理
        if (true) {
            //易付宝支付
            //参数前置校验
            if (!payTypes.contains(baseOrder.payType)) {
                return new Response("A004", "不支持的支付类型", baseOrder);
            }
            if (yfbService.exist(baseOrder.mchOrderId)) {
                return new Response("A005", "订单号重复", baseOrder);
            }
            //订单金额校验.
            if (baseOrder.payType.equals(Ali)) {
                //10-9999
                if (baseOrder.money < Ali_Min || baseOrder.money > Ali_Max) {
                    return new Response("A007", "交易金额限制：支付宝 10 -9999 元", baseOrder);
                }
            } else if (baseOrder.payType.equals(Wx)) {
                //20 30 50 100 200 300 500
                if (!Wx_Allow.contains(baseOrder.money)) {
                    return new Response("A007", "交易金额限制：微信 20，30，50，100，200，300，500 元", baseOrder);
                }
            }
            String str = yfbService.createOrder(baseOrder.mchId, baseOrder.payType, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl, baseOrder.redirectUrl, baseOrder.reserve, baseOrder.extra);
            //return str;
            str = str.replace("/pay/alipay/scanpay.aspx", "http://api.qianyipay.com/pay/alipay/scanpay.aspx");
            str = str.replace("/pay/weixin/scanpay.aspx", "http://api.qianyipay.com/pay/weixin/scanpay.aspx");
            return new Response("A000", "成功", successSign("A000", "成功", str, secretKey), str);
        } else if (true) {
            //快易支付..
            //其他支付...

        } else {
            //

            return new Response("A099", "下单失败，暂无可用通道", null);
        }
        return null;
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
            add(Ali);
            add(Wx);
        }
    };

    public boolean isSignValid(BaseOrder order, String secretKey) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(order);
        return !SignUtil.generateSignature(map, secretKey).equals(order.sign.toUpperCase());
    }

    public String successSign(String code, String message, Object data, String key) throws Exception {
        Response response = new Response(code, message, data);
        Map<String, String> map = SignUtil.objectToMap(response);
        return SignUtil.generateSignature(map, key);
    }


    @Transactional
    public Object orderQuery(OrderQueryReq orderQuery) throws Exception {
        String key = userKeyService.getKeyById(orderQuery.mchId);
        Map<String, String> map = SignUtil.objectToMap(orderQuery);
        String sign = SignUtil.generateSignature(map, key);
        if (sign.equals(orderQuery.sign.toUpperCase())) {
            YfbBill bill = yfbService.selectByMchOrderId(orderQuery.mchOrderId);
            if (bill == null) {
                return new OrderQueryRes("A006", "订单不存在");
            }
            if (bill.status == YfbBill.FINISH || bill.status == YfbBill.FAIL) {

            } else {
                //主动请求易付宝服务，获取当前订单信息
                bill = yfbService.orderQuery(bill.orderNo);

            }
            //直接返回订单信息
            OrderQueryRes res = new OrderQueryRes();
            res.code = "A000";
            res.message = "成功";
            res.money = bill.amount;
            res.mchOrderId = bill.subNo;
            res.sysOrderId = bill.orderNo;
            res.status = bill.status == YfbBill.FINISH ? "S" : bill.status == YfbBill.FAIL ? "F" : "I";
            if (bill.tradeTime != null) {
                res.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime);
            }

            Map<String, String> map1 = SignUtil.objectToMap(res);
            res.sign = SignUtil.generateSignature(map1, key);
            return res;
        }
        return new OrderQueryRes("A001", "签名校验失败");
    }
}
