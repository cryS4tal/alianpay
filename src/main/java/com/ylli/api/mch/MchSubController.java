package com.ylli.api.mch;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
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
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BASE))
    public Object getSubAccounts(@AwesomeParam(required = false) Long mchId, @AwesomeParam(defaultValue = "0") int offset,
                                 @AwesomeParam(defaultValue = "100") int limit) {
        return mchSubService.getSubAccounts(mchId, offset, limit);
    }

    @PostMapping
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BASE))
    public void addSubMch(@RequestBody MchSubAdd req) {
        mchSubService.addSubMch(req.subIds, req.mchId);
    }

    @DeleteMapping("/{subId}")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BASE))
    public void delSubMch(@PathVariable long subId) {
        mchSubService.delSubMch(subId);
    }
}
