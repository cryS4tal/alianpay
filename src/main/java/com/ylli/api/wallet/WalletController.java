package com.ylli.api.wallet;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Auth
@RestController
@RequestMapping
public class WalletController {

    @Autowired
    WalletService walletService;

    // todo
    // 余额添加接口
    // 余额查询接口  admin & 个人
}
