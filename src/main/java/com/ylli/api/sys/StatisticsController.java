package com.ylli.api.sys;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.sys.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计
 */
@RestController
@RequestMapping("/stats")
@Auth
public class StatisticsController {

    @Autowired
    StatisticsService statsService;

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
            if (permissionService.hasSysPermission(com.ylli.api.sys.Config.SysPermission.MANAGE_STATS)) {
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
            if (permissionService.hasSysPermission(com.ylli.api.sys.Config.SysPermission.MANAGE_STATS)) {
                break;
            }
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        } while (false);
        return statsService.total(mchId);
    }

    @GetMapping("/bonus/{date}")
    public Object bonus(@AwesomeParam(required = false) Long mchId,
                        @PathVariable String date) {
        do {
            if (mchId != null && authSession.getAuthId() == mchId) {
                break;
            }
            if (permissionService.hasSysPermission(com.ylli.api.sys.Config.SysPermission.MANAGE_STATS)) {
                break;
            }
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        } while (false);
        return statsService.bonus(mchId, date);
    }

    //暂时只对管理员开放。
    @GetMapping("/category/{date}")
    public Object category(@AwesomeParam(required = false) Long channelId,
                           @AwesomeParam(required = false) Long mchId,
                           @AwesomeParam(required = false) Integer status,
                           @AwesomeParam(defaultValue = "7") Integer groupby,
                           @PathVariable String date) {
        do {
            if (permissionService.hasSysPermission(com.ylli.api.sys.Config.SysPermission.MANAGE_STATS)) {
                break;
            }
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        } while (false);
        return statsService.category(channelId, mchId, status, date, groupby);
    }
}
