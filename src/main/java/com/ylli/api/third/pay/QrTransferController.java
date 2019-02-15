package com.ylli.api.third.pay;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.AwesomeDateTime;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.third.pay.modelVo.cntbnt.QrOrderFinish;
import com.ylli.api.third.pay.model.QrPendInfo;
import com.ylli.api.third.pay.modelVo.cntbnt.UploadQrCode;
import com.ylli.api.third.pay.modelVo.cntbnt.UploadUid;
import com.ylli.api.third.pay.service.cntbnt.QrTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个码转账.
 * 原生支付系统，不依赖任何第三方，需手动确认
 * <p>
 * 功能可以参考billController
 */
@RestController
@RequestMapping("/pay/qr")
@Auth(@Permission(Config.SysPermission.QR_CODE))
public class QrTransferController {

    @Autowired
    QrTransferService qrTransferService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    /**
     * 上传个人收款码
     *
     * @param uploadQrCode
     */
    @Auth
    @PostMapping("/code")
    public void uploadQrCode(@RequestBody UploadQrCode uploadQrCode) {
        ServiceUtil.checkNotEmpty(uploadQrCode);
        if (authSession.getAuthId() != uploadQrCode.authId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        qrTransferService.uploadQrCode(uploadQrCode.authId, uploadQrCode.codeName, uploadQrCode.codeUrl);
    }

    /**
     * 上传个人uid
     *
     * @param uploadUid
     */
    @Auth
    @PostMapping("/uid")
    public void uploadUid(@RequestBody UploadUid uploadUid) {
        ServiceUtil.checkNotEmpty(uploadUid);
        qrTransferService.uploadUid(uploadUid.id, uploadUid.authId, uploadUid.uid);
    }

    @Auth
    @DeleteMapping("/code/{id}")
    public void deleteQrCode(@PathVariable Long id) {
        qrTransferService.deleteQrCode(id);
    }

    @Auth
    @GetMapping("/code")
    public Object qrCodes(@AwesomeParam(required = false) Long authId,
                          @AwesomeParam(required = false) String nickName,
                          @AwesomeParam(required = false) String phone,
                          @AwesomeParam(defaultValue = "0") int offset,
                          @AwesomeParam(defaultValue = "10") int limit) {
        if (authSession.getAuthId() != (authId == null ? 0 : authId) && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_QR_CODE)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return qrTransferService.qrCodes(authId, nickName, phone, offset, limit);
    }

    @Auth
    @PostMapping("/order/finish")
    public void finish(@RequestBody QrOrderFinish finish) throws Exception {
        qrTransferService.finish(finish.sysOrderId, finish.money);
    }

    /**
     * 补单回滚
     *
     * @return
     */
    static class Reissue {
        public String sysOrderId;
    }

    @Auth
    @PostMapping("/reissue/rollback")
    public void rollback(@RequestBody Reissue reissue) throws Exception {
        qrTransferService.rollback(reissue.sysOrderId);
    }

    @Auth
    @GetMapping("/order")
    public Object getOrders(@AwesomeParam(required = false) Long authId,
                            @AwesomeParam(required = false) String nickName,
                            @AwesomeParam(required = false) String phone,
                            @AwesomeParam(required = false) Integer status,
                            @AwesomeParam(required = false) String sysOrderId,
                            @AwesomeParam(required = false) String mchOrderId,
                            @AwesomeParam(required = false) AwesomeDateTime startTime,
                            @AwesomeParam(required = false) AwesomeDateTime endTime,
                            @AwesomeParam(defaultValue = "0") int offset,
                            @AwesomeParam(defaultValue = "10") int limit) {
        if (authId == null && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_QR_CODE)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return qrTransferService.getOrders(authId, nickName, phone, status, sysOrderId, mchOrderId,
                startTime == null ? null : startTime.getDate(), endTime == null ? null : endTime.getDate(), offset, limit);
    }

    /**
     * 登入.默认登入
     */
    @Auth
    @PostMapping("/login")
    public void login() {
        qrTransferService.login(authSession.getAuthId());
    }

    /**
     * 登出.
     */
    @Auth
    @DeleteMapping("/logout")
    public void logout() {
        qrTransferService.logout(authSession.getAuthId());
    }


    @Auth
    @GetMapping("/login")
    public Integer loginCount() {
        return qrTransferService.loginCount();
    }

    @Auth
    @PostMapping("/pending")
    public void addPending(@RequestBody QrPendInfo pendInfo) {
        ServiceUtil.checkNotEmptyIgnore(pendInfo, true, "enable");
        qrTransferService.addPending(pendInfo);
    }

    @Auth
    @GetMapping("/pending")
    public Object getPending(@AwesomeParam(required = false) String name,
                             @AwesomeParam(required = false) Integer money,
                             @AwesomeParam(required = false) AwesomeDateTime startTime,
                             @AwesomeParam(required = false) AwesomeDateTime endTime,
                             @AwesomeParam(defaultValue = "0") int offset,
                             @AwesomeParam(defaultValue = "10") int limit) {
        return qrTransferService.getPending(name, money, startTime == null ? null : startTime.getDate(),
                endTime == null ? null : endTime.getDate(), offset, limit);
    }

    @Auth
    @GetMapping("/pending/{id}")
    public void pendHand(@PathVariable Long id) {
        qrTransferService.pendHand(id);
    }
}
