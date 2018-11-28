package com.ylli.api.yfbpay;

import com.ylli.api.yfbpay.service.YfbService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/yfb")
public class YfbController {

    @Autowired
    YfbService yfbService;


    @GetMapping("/notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        yfbService.payNotify(request, response);
    }

}
