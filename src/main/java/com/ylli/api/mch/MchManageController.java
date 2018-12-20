package com.ylli.api.mch;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.mch.service.MchManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mch")
@Auth(@Permission(Config.SysPermission.MANAGE_USER_ACCOUNT))
public class MchManageController {

    @Autowired
    MchManageService userManageService;


    /**
     * 设定为admin管理后台唯一获取用户列表接口
     *
     * @param phone   注册手机 || 法人手机
     * @param mchId   商户号
     * @param mchName 商户名
     * @param state   审核状态
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping("/list")
    public Object getMchList(@AwesomeParam(required = false) String phone,
                             @AwesomeParam(required = false) String mchId,
                             @AwesomeParam(required = false) String mchName,
                             @AwesomeParam(required = false) Integer auditState,
                             @AwesomeParam(required = false) String mchState,
                             @AwesomeParam(defaultValue = "0") int offset,
                             @AwesomeParam(defaultValue = "10") int limit) {

        return userManageService.mchList(phone, mchId, mchName, auditState,mchState, offset, limit);
    }

    @GetMapping
    public Object getMchInfo(@AwesomeParam Long mchId) {
        //return userManageService.getAccountDetail(mchId);
        return null;
    }
}
