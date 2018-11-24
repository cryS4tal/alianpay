package com.ylli.api.wallet;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.wallet.service.TradePasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Auth
public class TradePasswordController {

    @Autowired
    TradePasswordService tradePasswordService;

    // todo
    // 设置交易密码
    // 更改交易密码
    // 忘记交易密码
}
