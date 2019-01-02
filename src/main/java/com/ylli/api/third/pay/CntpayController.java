package com.ylli.api.third.pay;

import com.ylli.api.third.pay.model.CardReq;
import com.ylli.api.third.pay.model.ConfirmReq;
import com.ylli.api.third.pay.service.CntClient;
import com.ylli.api.third.pay.service.CntService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay/cnt")
public class CntpayController {
    @Autowired
    CntService cntService;

    @Autowired
    CntClient cntClient;

    @GetMapping("/notify")
    public String payNotify(@RequestParam Long userId,
                            @RequestParam String orderId,
                            @RequestParam String userOrder,
                            @RequestParam String number,
                            @RequestParam String merPriv,
                            @RequestParam String date,
                            @RequestParam String resultCode,
                            @RequestParam String resultMsg,
                            @RequestParam String remark,
                            @RequestParam String appID,
                            @RequestParam String chkValue) throws Exception {
        return cntService.payNotify(userId, orderId, userOrder, number, remark, merPriv, date, resultCode, resultMsg, appID, chkValue);
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
        return cntService.payConfirm(req.cardId, req.orderId, req.sgin, req.mchId);
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
        return cntService.payCancel(req.orderId, req.mchId, req.sgin);
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
    public Object cash() {
        return null;
    }
}
