package com.ylli.api.user;

import com.google.common.base.Strings;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.AwesomeDateTime;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.user.model.UserChargeInfo;
import com.ylli.api.user.model.UserOwnInfo;
import com.ylli.api.user.service.UserSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/settlement")
@Auth
public class UserSettlementController {

    @Autowired
    UserSettlementService userSettlementService;

    @Autowired
    AuthSession authSession;


    @PostMapping("/own")
    public Object saveUserInfo(@RequestBody UserOwnInfo ownInfo) {
        ServiceUtil.checkNotEmptyIgnore(ownInfo, true, "bankType", "openBank");
        if (authSession.getAuthId() != ownInfo.userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return userSettlementService.saveUserInfo(ownInfo);
    }

    @PostMapping("/charge")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_CHARGE))
    public Object saveChargeInfo(@RequestBody UserChargeInfo userChargeInfo) {
        ServiceUtil.checkNotEmpty(userChargeInfo);
        //目前只支持 百分比
        if (userChargeInfo.chargeType != UserChargeInfo.TYPE_FLOAT) {
            throw new AwesomeException(Config.ERROR_CHARGE_TYPE);
        }
        return userSettlementService.saveChargeInfo(userChargeInfo);
    }

    @GetMapping("/list")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_CHARGE))
    public Object getUserList(@AwesomeParam(required = false) Long userId,
                              @AwesomeParam(required = false) String name,
                              @AwesomeParam(required = false) String identityCard,
                              @AwesomeParam(required = false) String bankcardNumber,
                              @AwesomeParam(required = false) String reservedPhone,
                              @AwesomeParam(required = false) String openBank,
                              @AwesomeParam(required = false) String subBank,
                              @AwesomeParam(required = false) AwesomeDateTime beginTime,
                              @AwesomeParam(required = false) AwesomeDateTime endTime,
                              @AwesomeParam(defaultValue = "0") int offset,
                              @AwesomeParam(defaultValue = "10") int limit) {


        return userSettlementService.getUserList(userId, Strings.emptyToNull(name), Strings.emptyToNull(identityCard),
                Strings.emptyToNull(bankcardNumber), Strings.emptyToNull(reservedPhone), Strings.emptyToNull(openBank),
                Strings.emptyToNull(subBank), beginTime == null ? null : beginTime.getTimestamp(), endTime == null ? null : endTime.getTimestamp(), offset, limit);
    }

    @GetMapping
    public Object getUserInfo(@AwesomeParam Long userId) {
        if (authSession.getAuthId() != userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return userSettlementService.getUserInfo(userId);
    }

    @DeleteMapping("/{id}")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_CHARGE))
    public void removeUserInfo(@PathVariable Long id) {
        userSettlementService.removeUserInfo(id);
    }

}
