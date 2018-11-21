package com.ylli.api.auth;

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
    public static final ErrorCode ERROR_PHONE_NOT_EMPTY
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            1, "手机号不能为空");

    public static final ErrorCode ERROR_INVALID_PHONE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "无效的手机号码");

    public static final ErrorCode ERROR_USER_DISABLE
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            3, "用户已被禁用");

    public static final ErrorCode ERROR_VERIFY
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            4, "账号密码校验错误");

    public static final ErrorCode ERROR_PASSWORD_NOT_EMPTY
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            5, "密码不能为空");

    public static final ErrorCode ERROR_PERMISSION_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            6, "权限不存在");

    public static final ErrorCode ERROR_DATA_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            7, "数据不存在!");

    public static final ErrorCode ERROR_DEPT_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            8, "部门不存在");

    public static final ErrorCode ERROR_USER_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            9, "用户不存在");

    public static final ErrorCode ERROR_ROLE_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            10, "角色不存在");

    public static final ErrorCode ERROR_DELETE_PRE_ROLE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            11, "预设角色不能删除");

    public static final ErrorCode ERROR_EMPTY_ROLE_NAME
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            12, "角色名不能为空");

    public static final ErrorCode ERROR_DUPLICATE_ROLE_NAME
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            13, "角色名已存在");

    public static final ErrorCode ERROR_ROLE_PERMISSION_NOT_MATCH
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            14, "角色与权限类型不匹配");

    public static final ErrorCode ERROR_PERMISSION_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            15, "权限不足");



    /**
     * 默认系统部门
     */
    public static final long DEFAULT_SYS_DEPT_ID = 1;

    /**
     * 个人部门
     */
    public static final long DEFAULT_PERSONAL_DEPT_ID = 2;

    /**
     * 超级管理员Id
     */
    public static final long SUPER_MAN_ID = 1;

    /**
     * 未限定角色的部门Id
     */
    public static final long UNLIMIT_ROLE_DEPT_ID = 0;

    /**
     * 预设个人角色
     */
    public static final long ROLE_PERSONAL = 100;

    /**
     * 权限
     */
    public static class SysPermission {
        private static final long BASE = MODEL_CODE * 10000 + PermissionModel.TYPE_SYSTEM * 1000;
        /**
         * 管理角色与权限
         */
        public static final long MANAGE_ROLE_PERMISSION = BASE + 1;

        /**
         * 管理账户
         */
        public static final long MANAGE_ACCOUNT = BASE + 2;
        /**
         * 管理部门
         */
        public static final long MANAGE_DEPT = BASE + 4;
    }

    public static class DeptPermission {
        private static final long BASE = MODEL_CODE * 10000 + PermissionModel.TYPE_DEPT * 1000;
        /**
         * 管理角色与权限
         */
        public static final long MANAGE_ROLE_PERMISSION = BASE + 1;

        /**
         * 管理账户
         */
        public static final long MANAGE_ACCOUNT = BASE + 2;
    }
}
