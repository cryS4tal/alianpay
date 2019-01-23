package com.ylli.api.pay;

import com.google.common.base.Strings;
import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.util.AwesomeDateTime;
import com.ylli.api.mch.service.MchAgencyService;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bill")
@Auth
public class BillController {

    @Autowired
    BillService billService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    @Autowired
    MchAgencyService mchAgencyService;

    /**
     * 金额
     * 交易时间
     * 状态
     * 渠道：支付宝
     * 系统订单号
     * 商户订单号
     */

    @GetMapping
    public Object getBills(@AwesomeParam(required = false) List<Long> mchIds,
                           //@AwesomeParam(required = false) Long mchId,
                           @AwesomeParam(required = false) Integer status,
                           @AwesomeParam(required = false) String mchOrderId,
                           @AwesomeParam(required = false) String sysOrderId,
                           @AwesomeParam(required = false) String payType,
                           //@AwesomeParam(required = false) String tradeType,
                           @AwesomeParam(required = false) AwesomeDateTime tradeTime,
                           @AwesomeParam(required = false) AwesomeDateTime startTime,
                           @AwesomeParam(required = false) AwesomeDateTime endTime,
                           @AwesomeParam(defaultValue = "0") int offset,
                           @AwesomeParam(defaultValue = "20") int limit) {

        Boolean admin = permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_BILL);
        do {
            if (admin) {
                break;
            }
            if (mchIds.size() == 1 && authSession.getAuthId() == mchIds.get(0)) {
                break;
            }
            if (mchAgencyService.reg(mchIds, authSession.getAuthId())) {
                break;
            }
            permissionService.permissionDeny();
        } while (false);
        return billService.getBills(mchIds, status,
                Strings.isNullOrEmpty(mchOrderId) ? null : mchOrderId,
                Strings.isNullOrEmpty(sysOrderId) ? null : sysOrderId,
                Strings.isNullOrEmpty(payType) ? null : payType,
                //Strings.isNullOrEmpty(tradeType) ? null : tradeType,
                tradeTime == null ? null : tradeTime.getDate(),
                startTime == null ? null : startTime.getDate(),
                endTime == null ? null : endTime.getDate(), admin, offset, limit);
    }

    @GetMapping("/export")
    public void exportBills(@AwesomeParam(required = false) List<Long> mchIds,
                            //@AwesomeParam(required = false) Long mchId,
                            //@AwesomeParam(required = false) Integer status,
                            @AwesomeParam(required = false) String mchOrderId,
                            @AwesomeParam(required = false) String sysOrderId,
                            @AwesomeParam(required = false) String payType,
                            //@AwesomeParam(required = false) String tradeType,
                            @AwesomeParam(required = false) AwesomeDateTime tradeTime,
                            @AwesomeParam(required = false) AwesomeDateTime startTime,
                            @AwesomeParam(required = false) AwesomeDateTime endTime,
                            HttpServletResponse response) {
        billService.exportBills(mchIds, Bill.FINISH, mchOrderId, sysOrderId, payType,
                tradeTime == null ? null : tradeTime.getDate(),
                startTime == null ? null : startTime.getDate(),
                endTime == null ? null : endTime.getDate(),
                response);
    }


    @GetMapping("/today")
    public Object getTodayDetail(@AwesomeParam(required = false) Long mchId) {
        do {
            if (mchId != null && authSession.getAuthId() == mchId) {
                break;
            }
            if (permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_BILL)) {
                break;
            }
            permissionService.permissionDeny();
        } while (false);
        return billService.getTodayDetail(mchId);
    }

    static class Reissue {
        public String sysOrderId;
    }

    /**
     * 补单
     *
     * @return
     */
    @PostMapping("/reissue")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BILL))
    public Object reissue(@RequestBody Reissue reissue) throws Exception {
        return billService.reissue(reissue.sysOrderId);
    }

    /**
     * 补单回滚
     *
     * @return
     */
    @PostMapping("/reissue/rollback")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BILL))
    public void rollback(@RequestBody Reissue reissue) throws Exception {
        billService.rollback(reissue.sysOrderId);
    }
}
