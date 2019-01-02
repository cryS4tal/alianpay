package com.ylli.api.third.pay;

import com.ylli.api.third.pay.service.XianFenService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/xf")
public class XianFenPayController {

    @Autowired
    XianFenService xianFenService;

    /**
     * 目前接收商户回调...
     */
    @PostMapping("/notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        xianFenService.payNotify(request, response);
    }
}
