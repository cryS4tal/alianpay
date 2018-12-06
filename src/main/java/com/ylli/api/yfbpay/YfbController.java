package com.ylli.api.yfbpay;

import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.yfbpay.service.YfbClient;
import com.ylli.api.yfbpay.service.YfbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/yfb")
public class YfbController {

    @Autowired
    YfbService yfbService;

    @Autowired
    YfbClient client;

    /**
     * @param orderid    商户订单号
     * @param opstate    订单结果：0支付成功 -1 请求参数无效 -2 签名错误
     * @param ovalue     订单金额
     * @param sign       MD5 签名
     * @param sysorderid 易付宝订单号
     * @param systime    易付宝订单时间
     * @param attach     备注信息
     * @param msg        订单结果说明
     * @return
     */
    @GetMapping("/notify")
    public String payNotify(@AwesomeParam(required = false) String orderid,
                            @AwesomeParam(required = false) String opstate,
                            @AwesomeParam(required = false) String ovalue,
                            @AwesomeParam(required = false) String sign,
                            @AwesomeParam(required = false) String sysorderid,
                            @AwesomeParam(required = false) String systime,
                            @AwesomeParam(required = false) String attach,
                            @AwesomeParam(required = false) String msg) throws Exception {
        return yfbService.payNotify(orderid, opstate, ovalue, sign, sysorderid, systime, attach, msg);
    }

    @GetMapping("/notify/test")
    public String notifyTest(@AwesomeParam String mchOrderId) throws Exception {
        return yfbService.notifyTest(mchOrderId);
    }

}
