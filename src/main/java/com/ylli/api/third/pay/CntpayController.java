package com.ylli.api.third.pay;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.model.CardReq;
import com.ylli.api.third.pay.model.CntCashReq;
import com.ylli.api.third.pay.model.CntNotifyReq;
import com.ylli.api.third.pay.model.ConfirmReq;
import com.ylli.api.third.pay.service.CntClient;
import com.ylli.api.third.pay.service.CntService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.KeySelector;
import java.util.Map;

@RestController
@RequestMapping("/pay/cnt")
public class CntpayController {
    @Autowired
    CntService cntService;

    @Autowired
    CntClient cntClient;
    @Autowired
    MchKeyService mchKeyService;

    @PostMapping("/notify")
    public String payNotify(@RequestBody CntNotifyReq req) throws Exception {
        System.out.println(new Gson().toJson(req));
        return cntService.payNotify(req.userid, req.orderid, req.userorder, req.number, req.remark, req.merpriv, req.data, req.resultcode, req.resultmsg, req.appid, req.ispur, req.chkvalue);
    }

    /**
     * 支付确认
     *
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/confirm")
    public Object payConfirm(@RequestBody ConfirmReq req) throws Exception {
        String key = mchKeyService.getKeyById(req.mchId);
        Map<String, String> map = SignUtil.objectToMap(req);
        if (!SignUtil.generateSignature(map, key).equals(req.sign.toUpperCase())) {
            return new Response("A001", "签名校验失败", req);
        }
        return cntService.payConfirm(req.mchOrderId, req.mchId);
    }

    /**
     * 取消订单
     *
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/cancel")
    public Object payCancel(@RequestBody ConfirmReq req) throws Exception {
//        return cntService.payCancel(req);
        return null;
    }

    /**
     * 添加卡片
     *
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/card")
    public Object addCard(@RequestBody CardReq req) throws Exception {
        return cntService.addCard(req.mchId, req.userName, req.payName, req.openBank, req.subbranch);
    }

    /**
     * 提现
     *
     * @return
     */
    @PostMapping("/cash")
    public Object cash(@RequestBody CntCashReq req) throws Exception {
        String key = mchKeyService.getKeyById(req.mchId);
        Map<String, String> map = SignUtil.objectToMap(req);
        if (!SignUtil.generateSignature(map, key).equals(req.sign.toUpperCase())) {
            return new Response("A001", "签名校验失败", req);
        }
        return cntService.cash(req);
    }
}
