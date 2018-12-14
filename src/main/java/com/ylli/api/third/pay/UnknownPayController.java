package com.ylli.api.third.pay;

import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.third.pay.service.UnknownPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/unknown")
public class UnknownPayController {

    @Autowired
    UnknownPayService unknownPayService;

    @GetMapping("/notify")
    public String payNotify(@AwesomeParam String orderid,
                            @AwesomeParam String price,
                            @AwesomeParam String codeid,
                            @AwesomeParam String key) throws Exception {
        return unknownPayService.payNotify(orderid, price, codeid, key);
    }

}
