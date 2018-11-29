package com.ylli.api.pay.service;

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

    public static final String Yfb_Ali = "992";
    public static final String Yfb_Wx = "1004";


    /**
     * 中央调度server. 根据情况选择不同通道
     * 支付系统商户号为用户id. 代付系统系统生成string 商户号
     *
     * @param baseOrder
     * @return
     */
    public Object createOrder(BaseOrder baseOrder) throws Exception {

        //todo 参数非空前置校验

        //sign 前置校验
        String secretKey = userKeyService.getKeyById(baseOrder.mchId);
        if (secretKey == null) {
            return new Response("empty private key", "请先上传商户私钥", null);
        }
        if (isSignValid(baseOrder, secretKey)) {
            return new Response("signature check failed", "签名校验失败", baseOrder);
        }

        //加入系统通道管理
        if (true) {
            //易付宝支付
            //参数前置校验
            if (!payTypes.contains(baseOrder.payType)) {
                return new Response("Invalid arguments", "不支持的支付类型", baseOrder);
            }
            if (yfbService.exist(baseOrder.mchOrderId)) {
                return new Response("repeat order number", "订单号重复", baseOrder);
            }
            return yfbService.createOrder(baseOrder.mchId, typeConvert(null, baseOrder.payType), baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl, baseOrder.redirectUrl, baseOrder.reserve, baseOrder.extra);
        } else if (true) {
            //快易支付..
            //其他支付...

        } else {
            //

            return new Response("fail", "下单失败，暂无可用通道", null);
        }
        return null;
    }

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

    /**
     * 支付类型转换
     *
     * @return
     */
    public String typeConvert(Long channelId, String type) {

        if (true) { //channelId 为通道id.
            if (type.equals(Ali)) {
                return Yfb_Ali;
            } else if (type.equals(Wx)) {
                return Yfb_Wx;
            } else {
                return null;
            }
        }
        return "";
    }

    @Transactional
    public Object orderQuery(OrderQueryReq orderQuery) throws Exception {
        String key = userKeyService.getKeyById(orderQuery.mchId);
        Map<String, String> map = SignUtil.objectToMap(orderQuery);
        String sign = SignUtil.generateSignature(map, key);
        if (sign.equals(orderQuery.sign.toUpperCase())) {
            YfbBill bill = yfbService.selectByMchOrderId(orderQuery.mchOrderId);
            if (bill == null) {
                return new OrderQueryRes("FAIL", "订单不存在");
            }
            if (bill.status == YfbBill.FINISH || bill.status == YfbBill.FAIL) {

            } else {
                //主动请求易付宝服务，获取当前订单信息
                bill = yfbService.orderQuery(orderQuery.mchOrderId);

            }
            //直接返回订单信息
            OrderQueryRes res = new OrderQueryRes();
            res.code = "SUCCESS";
            res.message = "成功";
            res.money = bill.amount;
            res.mchOrderId = bill.subNo;
            res.sysOrderId = bill.orderNo;
            res.status = bill.status == YfbBill.FINISH ? "S" : bill.status == YfbBill.FAIL ? "F" : "I";
            res.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime);

            Map<String, String> map1 = SignUtil.objectToMap(res);
            res.sign = SignUtil.generateSignature(map1, key);
            return res;
        }
        return new OrderQueryRes("FAIL", "签名校验失败");
    }
}
