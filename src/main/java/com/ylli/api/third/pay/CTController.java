package com.ylli.api.third.pay;

import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.third.pay.service.CTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/ct")
public class CTController {

    @Autowired
    CTService ctService;

    @GetMapping("/notify")
    public String payNotify(@AwesomeParam(required = false) Boolean result,
                            @AwesomeParam(required = false) String resultCode,
                            @AwesomeParam(required = false) String attach,
                            @AwesomeParam(required = false) String totalFee,
                            @AwesomeParam(required = false) String sign) throws Exception {
        return ctService.paynotify(result, resultCode, attach, totalFee, sign);
    }
}
