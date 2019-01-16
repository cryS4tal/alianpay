package com.ylli.api.third.pay;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.third.pay.model.UploadQrCode;
import com.ylli.api.third.pay.service.QrTransferService;
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
 */
@RestController
@RequestMapping("/pay/qr")
public class QrTransferController {

    @Autowired
    QrTransferService qrTransferService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    /**
     * 登录，登出（登出将不在派单.）
     * 设置个码账号。
     * 手动确认订单。
     * todo 回滚订单
     */


    @Auth
    @PostMapping("/code")
    public void uploadQrCode(@RequestBody UploadQrCode uploadQrCode) {
        ServiceUtil.checkNotEmpty(uploadQrCode);
        if (authSession.getAuthId() != uploadQrCode.authId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        qrTransferService.uploadQrCode(uploadQrCode.authId, uploadQrCode.codeUrl);
    }

    @Auth
    @DeleteMapping("/code/{id}")
    public void deleteQrCode(@PathVariable Long id) {
        qrTransferService.deleteQrCode(id);
    }

    @Auth
    @GetMapping("/code")
    public Object qrCodes(@AwesomeParam Long authId,
                          @AwesomeParam String nickName,
                          @AwesomeParam String phone,
                          @AwesomeParam(defaultValue = "0") int offset,
                          @AwesomeParam(defaultValue = "10") int limit) {
        if (authSession.getAuthId() != authId && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_QR_CODE)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return qrTransferService.qrCodes(authId, nickName, phone, offset, limit);
    }

}
