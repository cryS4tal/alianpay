package com.ylli.api.third.pay;

import com.google.gson.Gson;
import com.ylli.api.third.pay.model.GPNotify;
import com.ylli.api.third.pay.service.GPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/gpay")
public class GPayController {

    @Autowired
    GPayService gPayService;

    @PostMapping("/notify")
    public void payNotify(@RequestBody String str) throws Exception {
        GPNotify notify = new Gson().fromJson(str, GPNotify.class);
        gPayService.paynotify(notify);
    }
}
