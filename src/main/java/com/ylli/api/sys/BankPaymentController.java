package com.ylli.api.sys;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.sys.service.BankPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Auth(@Permission(Config.SysPermission.MANAGE_BANK_PAYMENT))
public class BankPaymentController {

    @Autowired
    BankPaymentService bankPaymentService;


}
