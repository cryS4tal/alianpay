package com.ylli.api.third.pay;

import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.model.CntCashReq;
import com.ylli.api.third.pay.service.CntService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/cnt")
public class CntpayController {
    @Autowired
    CntService cntService;

    @Autowired
    MchKeyService mchKeyService;

    @PostMapping("/notify")
    public String payNotify(@RequestParam String userId,
                            @RequestParam String orderId,
                            @RequestParam String userOrder,
                            @RequestParam String number,
                            @RequestParam String date,
                            @RequestParam String resultCode,
                            @RequestParam String resultMsg,
                            @RequestParam String appID,
                            @RequestParam String chkValue,
                            @RequestParam String remark,
                            @RequestParam String merPriv,
                            @RequestParam String isPur) throws Exception {
        return cntService.payNotify(userId, orderId, userOrder, number, remark, merPriv, date, resultCode, resultMsg, appID, isPur, chkValue);
    }

    /**
     * 提现
     *
     * @return
     */
    @PostMapping("/cash")
    public Object cash(@RequestBody CntCashReq req) throws Exception {
        String key = mchKeyService.getKeyById(req.mchId);
        //签名判断
        Map<String, String> map = SignUtil.objectToMap(req);
        if (!SignUtil.generateSignature(map, key).equals(req.sign.toUpperCase())) {
            return new Response("A001", "签名校验失败", req);
        }
        return cntService.cash(req);
    }
}
