package com.ylli.api.wallet;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.Permission;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Auth(@Permission(Config.SysPermission.MANAGE_USER_CASH))
public class CashController {
}
