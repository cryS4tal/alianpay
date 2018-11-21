package com.ylli.api.auth.service;

import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import com.ylli.api.auth.Config;
import com.ylli.api.auth.mapper.DeptAccountMapper;
import com.ylli.api.auth.mapper.PermissionMapper;
import com.ylli.api.auth.mapper.RolePermissionMapper;
import com.ylli.api.auth.model.DeptAccount;
import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.auth.model.RolePermission;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.aop.AuthAspect;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.define.EmptyDefine;
import com.ylli.api.base.exception.AwesomeException;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created by RexQian on 2017/2/23.
 */
@Service
public class PermissionService implements AuthAspect.PermissionCheckCallback {
    private static final String HEADER_OPT_DEPT_ID = "X-Opt-Dept-Id";

    @Autowired
    AuthSession authSession;

    @Autowired
    RolePermissionMapper rolePermissionMapper;

    @Autowired
    DeptAccountMapper deptAccountMapper;

    @Autowired
    PermissionMapper permissionMapper;

    @Autowired
    AuthAspect authAspect;

    @PostConstruct
    public void init() {
        authAspect.setPermissionCheckCallback(this);
    }

    public void checkSys(Long sysPermission) {
        check(sysPermission, Config.DEFAULT_SYS_DEPT_ID);
    }

    public void check(Long... permissionAndDeptId) throws AwesomeException {
        if (permissionAndDeptId.length % 2 != 0) {
            throw new InternalError("权限配置错误");
        }

        for (int i = 0; i < permissionAndDeptId.length; i += 2) {
            if (hasPermission(permissionAndDeptId[i], permissionAndDeptId[i + 1])) {
                return;
            }
        }

        permissionDeny();
    }

    public boolean hasSysPermission(long permissionId) {
        return hasPermission(permissionId, Config.DEFAULT_SYS_DEPT_ID);
    }

    public boolean hasPermission(long permissionId, Long deptId) {
        long uid = authSession.getAuthId();
        if (uid == Config.SUPER_MAN_ID) {
            return true;
        }

        if (deptId == null) {
            deptId = Config.DEFAULT_SYS_DEPT_ID;
        }

        DeptAccount deptAccount = new DeptAccount();
        deptAccount.accountId = uid;
        deptAccount.deptId = deptId;
        List<DeptAccount> roleList = deptAccountMapper.select(deptAccount);
        for (DeptAccount role : roleList) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.roleId = role.roleId;
            rolePermission.permissionId = permissionId;

            if (rolePermissionMapper.selectCount(rolePermission) > 0) {
                return true;
            }
        }
        return false;
    }

    public void checkOptSameDept(Long deptId) throws AwesomeException {
        checkOptSameDept(deptId, false);
    }

    public void checkOptSameDept(Long deptId, boolean ignoreSysDept) throws AwesomeException {
        // ignore check for super man
        if (authSession.getAuthId() == Config.SUPER_MAN_ID) {
            return;
        }
        Long optDeptId = getOptDeptId();
        if (optDeptId == null) {
            optDeptId = Config.DEFAULT_SYS_DEPT_ID;
        }

        if (ignoreSysDept
                && optDeptId == Config.DEFAULT_SYS_DEPT_ID) {
            return;
        }

        if (!optDeptId.equals(deptId)) {
            permissionDeny();
        }
    }

    public void checkOptSys() {
        checkOptSameDept(Config.DEFAULT_SYS_DEPT_ID);
    }

    public void permissionDeny() throws AwesomeException {
        throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
    }

    public List<PermissionModel> getList(int type) {
        PermissionModel model = new PermissionModel();
        model.type = type;
        return permissionMapper.select(model);
    }

    public Long getOptDeptId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Long deptId = null;
        String strDeptId = attributes.getRequest().getHeader(HEADER_OPT_DEPT_ID);
        if (!Strings.isNullOrEmpty(strDeptId)) {
            deptId = Longs.tryParse(strDeptId);
        }
        if (EmptyDefine.EMPTY_ID.equals(deptId)) {
            deptId = null;
        }
        return deptId;
    }

    public Long getOptDeptId(boolean useSysDefault) {
        Long deptId = getOptDeptId();
        if (useSysDefault && deptId == null) {
            return Config.DEFAULT_SYS_DEPT_ID;
        }
        return deptId;
    }

    public boolean isSystemDeptId() {
        return getOptDeptId(true) == Config.DEFAULT_SYS_DEPT_ID;
    }

    public boolean isSystemDeptId(boolean useSysDefault) {
        Long deptId = getOptDeptId(useSysDefault);
        return deptId != null && deptId == Config.DEFAULT_SYS_DEPT_ID;
    }

    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    @Override
    public boolean check(Permission[] permissions) throws AwesomeException {
        Long deptId = getOptDeptId();
        for (Permission permission : permissions) {
            boolean grant = true;
            for (long value : permission.value()) {
                if (!hasPermission(value, deptId)) {
                    grant = false;
                    break;
                }
            }
            if (grant) {
                return true;
            }
        }
        return false;
    }
}
