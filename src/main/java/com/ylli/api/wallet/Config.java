package com.ylli.api.wallet;

import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 8;
    /**
     * 错误定义
     */
    public static final ErrorCode ERROR_PERMISSION_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            1, "权限不足");

    public static final ErrorCode ERROR_CHARGE_MONEY
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "提现金额区间为 1000 - 20W");

    public static final ErrorCode ERROR_VERIFY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            3, "密码错误");

    public static final ErrorCode ERROR_SETTLEMENT_CHARGE_EMPTY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            4, "请联系管理员设置您的费率");

    public static final ErrorCode ERROR_SETTLEMENT_EMPTY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            5, "请先设置结算信息.");

    public static final ErrorCode ERROR_CASH_OUT_BOUND
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            6, "您当前最大的提现金额为：%s 元");

    public static final ErrorCode ERROR_REQUEST_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            7, "记录不存在");

    public static final ErrorCode ERROR_CASH_HANDLED
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            8, "记录不存在");
    /**
     * 权限
     */
    public static class SysPermission {
        private static final long BASE = MODEL_CODE * 10000 + PermissionModel.TYPE_SYSTEM * 1000;
        /**
         * 管理角色与权限
         */
        public static final long MANAGE_USER_WALLET = BASE + 1;

        public static final long MANAGE_USER_CASH = BASE + 2;
    }
}
