package com.ylli.api.third.pay;

import com.google.common.base.Strings;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.model.ConfirmReq;
import com.ylli.api.third.pay.service.CntClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay/cnt/test")
public class CntpayTestController {
    @Autowired
    MchKeyService mchKeyService;
    @Autowired
    CntClient cntClient;
    @Autowired
    SerializeUtil serializeUtil;
    public final static String host = "http://116.62.209.131:8088/pay/";

    @PostMapping("/confirm")
    public Object payConfirm(@RequestBody ConfirmReq req) throws Exception {
        String key = mchKeyService.getKeyById(req.mchId);
        Map<String, String> map = SignUtil.objectToMap(req);
        String sign = SignUtil.generateSignature(map, key);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("mch_id", req.mchId.toString());
        params.add("sign", sign);
        params.add("mch_order_id", req.mchOrderId);
        params.add("tradeType", "native");
        return cntClient.post(params, host + "cnt/confirm");
    }

    @PostMapping("/order")
    public Object createOrder(@RequestBody BaseOrder baseOrder) throws Exception {
        baseOrder.tradeType = "native";
        baseOrder.version = BaseOrder.CNT;
        baseOrder.mchOrderId = serializeUtil.generateSysOrderId();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("mch_id", baseOrder.mchId.toString());
        params.add("money", baseOrder.money.toString());
        params.add("version", "1.1");
        params.add("mch_order_id", baseOrder.mchOrderId);
        params.add("pay_type", baseOrder.payType);
        params.add("trade_type",baseOrder.tradeType);;
        Map<String, String> map = SignUtil.objectToMap(baseOrder);
        String key = mchKeyService.getKeyById(baseOrder.mchId);
        params.add("sign", SignUtil.generateSignature(map, key));
        Map<String, String> m = new HashMap<>();
        m.put("data", cntClient.post(params, host + "order"));
        m.put("mchOrderId", baseOrder.mchOrderId);
        return m;
    }

}
