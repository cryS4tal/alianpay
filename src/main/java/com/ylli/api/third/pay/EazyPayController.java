package com.ylli.api.third.pay;

import com.ylli.api.third.pay.modelVo.easy.EazyNotify;
import com.ylli.api.third.pay.service.eazy.EazyPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/eazy")
public class EazyPayController {

    @Autowired
    EazyPayService payService;

    @PostMapping("/notify")
    public String payNotify(@RequestBody EazyNotify notify) throws Exception {
        return payService.paynotify(notify);
    }
}
