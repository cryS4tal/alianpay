package com.ylli.api.third.pay;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.third.pay.service.xianfen.XianFenService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/xf")
public class XianFenPayController {

    @Autowired
    XianFenService xianFenService;

    /**
     * 单笔代发回调
     */
    @PostMapping("/notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        xianFenService.payNotify(request, response);
    }

    @Auth(@Permission(Config.SysPermission.MANAGE_XF))
    @GetMapping("/balance")
    public Object balance() throws Exception {
        return xianFenService.balance();
    }

    /**
     * 线下充值回调
     */
    @PostMapping("/recharge/notify")
    public void rechargeNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        xianFenService.rechargeNotify(request, response);
    }
}
