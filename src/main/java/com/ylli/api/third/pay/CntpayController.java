package com.ylli.api.third.pay;

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

    /* @GetMapping("/notify")
     public String payNotify(@RequestParam Long userId,
                             @RequestParam String orderId,
                             @RequestParam String userOrder,
                             @RequestParam String number,
                             @RequestParam String date,
                             @RequestParam String resultCode,
                             @RequestParam String resultMsg,
                             @RequestParam String sn,
                             @RequestParam String appID,
                             @RequestParam String chkValue) throws Exception {
         return cntService.payNotify(userId, orderId, userOrder, number, sn, date, resultCode, resultMsg, appID,chkValue);
     }*/
    @PostMapping
    public Object payConfirm(ConfirmReq req) {
        return cntService.payConfirm(req.orderId);
    }
}
