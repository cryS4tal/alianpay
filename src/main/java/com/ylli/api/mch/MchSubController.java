package com.ylli.api.mch;

import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.mch.model.MchSubAdd;
import com.ylli.api.mch.service.MchSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/phone/sub")
public class MchSubController {
    @Autowired
    MchSubService mchSubService;
    @GetMapping
    public Object getSubAccounts(@AwesomeParam(required = false) Long mchId, @AwesomeParam(defaultValue = "0") int offset,
                                 @AwesomeParam(defaultValue = "100") int limit) {
       /* if (!permissionService.hasSysPermission(com.ylli.api.user.Config.SysPermission.MANAGE_USER_ACCOUNT)) {
            throw new AwesomeException(com.ylli.api.user.Config.ERROR_PERMISSION_DENY);
        }*/
        return mchSubService.getSubAccounts(mchId, offset, limit);
    }

    @PostMapping
    public void addSubMch(@RequestBody MchSubAdd req) {
      /*  if (!permissionService.hasSysPermission(com.ylli.api.user.Config.SysPermission.MANAGE_USER_ACCOUNT)) {
            throw new AwesomeException(com.ylli.api.user.Config.ERROR_PERMISSION_DENY);
        }*/
        mchSubService.addSubMch(req.subIds, req.mchId);
    }

    @DeleteMapping("/{subId}")
    public void delSubMch(@PathVariable long subId) {
       /* if (!permissionService.hasSysPermission(com.ylli.api.user.Config.SysPermission.MANAGE_USER_ACCOUNT)) {
            throw new AwesomeException(com.ylli.api.user.Config.ERROR_PERMISSION_DENY);
        }*/
        mchSubService.delSubMch(subId);
    }
}
