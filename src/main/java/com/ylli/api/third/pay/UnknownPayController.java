package com.ylli.api.third.pay;

import com.ylli.api.third.pay.service.UnknownPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/unknown")
public class UnknownPayController {

    @Autowired
    UnknownPayService unknownPayService;



}
