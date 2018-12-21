package com.ylli.api.sys;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.sys.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计
 */
@RestController
@RequestMapping("/stats")
@Auth
public class StatsController {

    @Autowired
    StatsService statsService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    AuthSession authSession;

    @GetMapping
    public Object hourlyData(@AwesomeParam(required = false) Long mchId) {
        do {
            if (mchId != null && authSession.getAuthId() == mchId) {
                break;
            }
            if (permissionService.hasSysPermission(Config.SysPermission.MANAGE_STATS)) {
                break;
            }
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        } while (false);
        return statsService.hourlyData(mchId);
    }

    @GetMapping("/total")
    public Object total(@AwesomeParam(required = false) Long mchId) {
        do {
            if (mchId != null && authSession.getAuthId() == mchId) {
                break;
            }
            if (permissionService.hasSysPermission(Config.SysPermission.MANAGE_STATS)) {
                break;
            }
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        } while (false);
        return statsService.total(mchId);
    }


    @GetMapping("/rate")
    public Object successRate(@AwesomeParam(required = false) Long channelId,
                              @AwesomeParam(required = false) Long mchId,
                              @AwesomeParam(required = false) Long appId) {
        //商户 mch_id 必传
        //appId 选填
        if (!permissionService.hasSysPermission(Config.SysPermission.MANAGE_STATS)) {
            if (mchId == null || mchId != authSession.getAuthId()) {
                throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
            }
            if (channelId != null) {
                throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
            }
        }
        return statsService.successRate(channelId, mchId, appId);
    }
}
