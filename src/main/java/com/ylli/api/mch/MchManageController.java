package com.ylli.api.mch;

import com.google.common.base.Strings;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.CheckPhone;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.mch.model.AdminResetPwd;
import com.ylli.api.mch.model.MchAgentDto;
import com.ylli.api.mch.model.MchEnable;
import com.ylli.api.mch.service.MchManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mch")
@Auth(@Permission(Config.SysPermission.MANAGE_USER_ACCOUNT))
public class MchManageController {

    @Autowired
    MchManageService manageService;


    /**
     * 设定为admin管理后台唯一获取用户列表接口
     *
     * @param phone      注册手机 || 法人手机
     * @param mchId      商户号
     * @param mchName    商户名
     * @param auditState 审核状态
     * @param mchState   账户状态
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

        return manageService.mchList(phone, mchId, mchName, auditState, mchState, offset, limit);
    }

    /**
     * 激活冻结账户
     */
    @PostMapping
    public void mchEnable(@RequestBody MchEnable enable) {
        ServiceUtil.checkNotEmpty(enable);
        manageService.mchEnable(enable.mchId, enable.open);
    }

    /**
     * 管理员重置密码
     */
    @PostMapping("/pwd")
    public void resetPwd(@RequestBody AdminResetPwd resetPwd) {
        ServiceUtil.checkNotEmpty(resetPwd);
        manageService.resetPwd(resetPwd.mchId, resetPwd.password);
    }

    /**
     * 添加代理商
     */
    @PostMapping("/agent")
    public void addAgent(@RequestBody MchAgentDto req) {
        if (Strings.isNullOrEmpty(req.phone)) {
            throw new AwesomeException(com.ylli.api.auth.Config.ERROR_PHONE_NOT_EMPTY);
        }
        if (Strings.isNullOrEmpty(req.password)) {
            throw new AwesomeException(com.ylli.api.auth.Config.ERROR_PASSWORD_NOT_EMPTY);
        }
        if (Strings.isNullOrEmpty(req.mchName)) {
            throw new AwesomeException(com.ylli.api.auth.Config.ERROR_EMPTY_MCH_NAME);
        }
        manageService.createAgent(req.phone, req.password, req.apps, req.mchName);
    }

    @PutMapping("/agent")
    public void updateAgent(@RequestBody MchAgentDto req) {
        if (Strings.isNullOrEmpty(req.phone)) {
            throw new AwesomeException(com.ylli.api.auth.Config.ERROR_PHONE_NOT_EMPTY);
        }
        if (Strings.isNullOrEmpty(req.mchName)) {
            throw new AwesomeException(com.ylli.api.auth.Config.ERROR_EMPTY_MCH_NAME);
        }
        if (!CheckPhone.isSimplePhone(req.phone)) {
            throw new AwesomeException(com.ylli.api.auth.Config.ERROR_INVALID_PHONE);
        }
        manageService.updateAgent(req.phone, req.password, req.apps, req.mchName,req.mchId);
    }

    @GetMapping("/agent")
    public Object getAgents(@AwesomeParam(defaultValue = "0") int offset,
                            @AwesomeParam(defaultValue = "10") int limit) {
        return manageService.getAgents(offset, limit);
    }




}
