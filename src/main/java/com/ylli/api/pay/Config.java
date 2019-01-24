package com.ylli.api.pay;

import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 4;
    /**
     * 错误定义
     */
    public static final ErrorCode ERROR_PERMISSION_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            1, "权限不足");

    public static final ErrorCode ERROR_BILL_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            2, "订单不存在");

    public static final ErrorCode ERROR_BILL_STATUS
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            3, "订单状态非法");

    public static final ErrorCode ERROR_BILL_ROLLBACK
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            4, "此订单不是手工补单，不能回滚");

    public static final ErrorCode ERROR_FAILURE_BILL_EXCEL_EXPORT
            = new ErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MODEL_CODE,
            5, "导出excel异常");

    public static final ErrorCode ERROR_MCH_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            6, "商户不存在");

    public static final ErrorCode ERROR_RATE_NOT_NULL
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            7, "利率不能为空");

    public static final ErrorCode ERROR_RATE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            8, "%s");

    /**
     * 权限
     */
    public static class SysPermission {
        private static final long BASE = MODEL_CODE * 10000 + PermissionModel.TYPE_SYSTEM * 1000;
        /**
         * 管理角色与权限
         */
        public static final long MANAGE_USER_BILL = BASE + 1;
    }
}
