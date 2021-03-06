package com.ylli.api.xfpay;

import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 7;
    /**
     * 错误定义
     */
    public static final ErrorCode ERROR_PERMISSION_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            1, "权限不足");

    public static final ErrorCode ERROR_ORDERNO_NOT_EMPTY
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "订单号不能为空");

    public static final ErrorCode ERROR_ORDER_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            3, "订单号不存在");

    /**
     * 权限
     */
    public static class SysPermission {
        private static final long BASE = MODEL_CODE * 10000 + PermissionModel.TYPE_SYSTEM * 1000;
        /**
         * 管理角色与权限
         */
        //public static final long MANAGE_USER_CHARGE = BASE + 1;
    }
}
