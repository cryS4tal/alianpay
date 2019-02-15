package com.ylli.api.third.pay;

import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.third.pay.service.cntbnt.CntService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/cnt")
public class CntpayController {

    private static Logger LOGGER = LoggerFactory.getLogger(CntpayController.class);

    @Autowired
    CntService cntService;

    @Autowired
    MchKeyService mchKeyService;

    @PostMapping("/notify")
    public String payNotify(@RequestParam(required = false) String userId,
                            @RequestParam(required = false) String orderId,
                            @RequestParam(required = false) String userOrder,
                            @RequestParam(required = false) String number,
                            @RequestParam(required = false) String date,
                            @RequestParam(required = false) String resultCode,
                            @RequestParam(required = false) String resultMsg,
                            @RequestParam(required = false) String appID,
                            @RequestParam(required = false) String chkValue,
                            @RequestParam(required = false) String remark,
                            @RequestParam(required = false) String merPriv,
                            @RequestParam(required = false) String isPur) throws Exception {
        LOGGER.info("received cnt notify: userId = [" + userId + "] orderId = [ " + orderId + "] userOrder = ["
                + userOrder + "] isPur = [" + isPur + "] resultCode = [" + resultCode + "] resultMsg = [" + resultMsg);

        return cntService.payNotify(userId, orderId, userOrder, number, remark, merPriv, date, resultCode, resultMsg, appID, isPur, chkValue);
    }
}
