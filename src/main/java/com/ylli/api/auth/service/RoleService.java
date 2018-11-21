package com.ylli.api.auth.service;

import com.google.common.base.Strings;
import com.ylli.api.auth.Config;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.mapper.DepartmentMapper;
import com.ylli.api.auth.mapper.DeptAccountMapper;
import com.ylli.api.auth.mapper.PermissionMapper;
import com.ylli.api.auth.mapper.RoleMapper;
import com.ylli.api.auth.mapper.RolePermissionMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.Department;
import com.ylli.api.auth.model.DeptAccount;
import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.auth.model.Role;
import com.ylli.api.auth.model.RoleChange;
import com.ylli.api.auth.model.RolePermission;
import com.ylli.api.auth.model.emun.DeptType;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.SimpleObject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by RexQian on 2017/2/22.
 */
@Service
public class RoleService {
    @Autowired
    RoleMapper roleMapper;

    @Autowired
    DeptAccountMapper deptAccountMapper;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    PermissionMapper permissionMapper;

    @Autowired
    RolePermissionMapper rolePermissionMapper;

    @Autowired
    DepartmentMapper departmentMapper;

    @Autowired
    NoticeService noticeService;

    private void addRoleWithoutNotice(long deptId, long accountId, long roleId) {
        Department department = departmentMapper.selectByPrimaryKey(deptId);
        if (department == null) {
            throw new AwesomeException(Config.ERROR_DEPT_NOT_FOUND);
        }

        Account account = accountMapper.selectByPrimaryKey(accountId);
        if (account == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }

        Role role = roleMapper.selectByPrimaryKey(roleId);
        if (role == null || !validDeptAndRole(department, role)) {
            throw new AwesomeException(Config.ERROR_ROLE_NOT_FOUND);
        }

        DeptAccount deptAccount = new DeptAccount();
        deptAccount.deptId = deptId;
        deptAccount.accountId = accountId;
        deptAccount.roleId = roleId;
        if (deptAccountMapper.selectCount(deptAccount) > 0) {
            // role ready exist
            // ignore this operation
            return;
        }

        deptAccountMapper.insertSelective(deptAccount);
    }

    @Transactional
    public void addRole(long deptId, long accountId, long roleId) throws AwesomeException {
        List<Role> oldRoles = deptAccountMapper.getRoleList(accountId, deptId);

        addRoleWithoutNotice(deptId, accountId, roleId);

        Department department = departmentMapper.selectByPrimaryKey(deptId);
        RoleChange noticeData = new RoleChange();
        noticeData.dept = new SimpleObject();
        noticeData.dept.id = department.id;
        noticeData.dept.name = department.name;
        noticeData.oldRoles = oldRoles;
        noticeData.newRoles = deptAccountMapper.getRoleList(accountId, deptId);
        noticeService.add(noticeData.type(), accountId, accountId, noticeData.title(),
                noticeData.description(),
                noticeData.extras());
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @Transactional
    public void removeRole(long deptId, long accountId, long roleId) throws AwesomeException {
        Department department = departmentMapper.selectByPrimaryKey(deptId);
        if (department == null) {
            throw new AwesomeException(Config.ERROR_DEPT_NOT_FOUND);
        }

        Account account = accountMapper.selectByPrimaryKey(accountId);
        if (account == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }

        Role role = roleMapper.selectByPrimaryKey(roleId);
        if (role == null) {
            throw new AwesomeException(Config.ERROR_ROLE_NOT_FOUND);
        }
        List<Role> oldRoles = deptAccountMapper.getRoleList(accountId, deptId);

        DeptAccount deptAccount = new DeptAccount();
        deptAccount.deptId = deptId;
        deptAccount.accountId = accountId;
        deptAccount.roleId = roleId;
        deptAccountMapper.delete(deptAccount);

        RoleChange noticeData = new RoleChange();
        noticeData.dept = new SimpleObject();
        noticeData.dept.id = department.id;
        noticeData.dept.name = department.name;
        noticeData.oldRoles = oldRoles;
        noticeData.newRoles = deptAccountMapper.getRoleList(accountId, deptId);

        noticeService.add(noticeData.type(), accountId, accountId, noticeData.title(),
                noticeData.description(),
                noticeData.extras());
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @Transactional
    public void setRoles(long accountId, long deptId, List<Long> roleIds) throws AwesomeException {
        Account account = accountMapper.selectByPrimaryKey(accountId);
        if (account == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }

        Department department = departmentMapper.selectByPrimaryKey(deptId);
        if (department == null) {
            throw new AwesomeException(Config.ERROR_DEPT_NOT_FOUND);
        }

        //check roles
        if (roleIds != null) {
            for (Long id : roleIds) {
                Role role = roleMapper.selectByPrimaryKey(id);
                if (role == null || !validDeptAndRole(department, role)) {
                    throw new AwesomeException(Config.ERROR_ROLE_NOT_FOUND);
                }
            }
        }
        List<Role> oldRoles = deptAccountMapper.getRoleList(accountId, deptId);

        DeptAccount deptAccount = new DeptAccount();
        deptAccount.deptId = deptId;
        deptAccount.accountId = accountId;
        deptAccountMapper.delete(deptAccount);

        if (roleIds != null) {
            for (Long roleId : roleIds) {
                addRoleWithoutNotice(deptId, accountId, roleId);
            }
        }

        RoleChange noticeData = new RoleChange();
        noticeData.dept = new SimpleObject();
        noticeData.dept.id = department.id;
        noticeData.dept.name = department.name;
        noticeData.oldRoles = oldRoles;
        noticeData.newRoles = deptAccountMapper.getRoleList(accountId, deptId);

        noticeService.add(noticeData.type(), accountId, accountId, noticeData.title(),
                noticeData.description(),
                noticeData.extras());
    }

    @SuppressWarnings("PMD.CollapsibleIfStatements")
    private boolean validDeptAndRole(Department department, Role role) {
        if (department.type == DeptType.DEPT) {
            if (role.type != Role.TYPE_DEPT_PRE && role.type != Role.TYPE_DEPT) {
                return false;
            }
        } else if (department.type == DeptType.SYSTEM) {
            if (role.type != Role.TYPE_SYSTEM_PRE && role.type != Role.TYPE_SYSTEM) {
                return false;
            }
        }
        return role.deptId == Config.UNLIMIT_ROLE_DEPT_ID
                || role.deptId.equals(department.id);
    }

    public List<Role> getPreDefinedList(Boolean isSystem) {
        List<Role> list = new ArrayList<>();
        if (isSystem == null || isSystem) {
            Role selectRole = new Role();
            selectRole.type = Role.TYPE_SYSTEM_PRE;
            selectRole.deptId = Config.UNLIMIT_ROLE_DEPT_ID;
            List<Role> list1 = roleMapper.select(selectRole);
            list.addAll(list1);
        }

        if (isSystem == null || !isSystem) {
            Role selectRole = new Role();
            selectRole.type = Role.TYPE_DEPT_PRE;
            selectRole.deptId = Config.UNLIMIT_ROLE_DEPT_ID;
            List<Role> list1 = roleMapper.select(selectRole);
            list.addAll(list1);
        }
        for (Role item : list) {
            item.permissions = rolePermissionMapper.getPermissionsByRoleId(item.id);
        }
        return list;
    }

    public List<Role> getListByDept(Long deptId) throws AwesomeException {
        Department department = departmentMapper.selectByPrimaryKey(deptId);
        if (department == null) {
            throw new AwesomeException(Config.ERROR_DEPT_NOT_FOUND);
        }
        Role role = new Role();
        role.deptId = deptId;
        List<Role> list = roleMapper.select(role);

        if (department.type == DeptType.SYSTEM) {
            Role selectRole = new Role();
            selectRole.type = Role.TYPE_SYSTEM_PRE;
            selectRole.deptId = Config.UNLIMIT_ROLE_DEPT_ID;
            List<Role> list1 = roleMapper.select(selectRole);
            list.addAll(0, list1);
        } else if (department.type == DeptType.DEPT) {
            Role selectRole = new Role();
            selectRole.type = Role.TYPE_DEPT_PRE;
            selectRole.deptId = Config.UNLIMIT_ROLE_DEPT_ID;
            List<Role> list1 = roleMapper.select(selectRole);
            list.addAll(0, list1);
        }

        for (Role item : list) {
            item.permissions = rolePermissionMapper.getPermissionsByRoleId(item.id);
        }
        return list;
    }

    public List<Role> getListByAccountAndDept(long accountId, Long deptId) {
        List<Role> list = deptAccountMapper.getRoleList(accountId, deptId);
        for (Role item : list) {
            item.permissions = rolePermissionMapper.getPermissionsByRoleId(item.id);
        }
        return list;
    }

    public Role get(Long id) {
        return roleMapper.selectByPrimaryKey(id);
    }

    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    @Transactional
    public Role addRole(String name, String description,
                        long deptId, List<Long> permissionIds) throws AwesomeException {
        Role role = addRole(name, description, deptId);
        setPermissions(role.id, permissionIds);
        role.permissions = rolePermissionMapper.getPermissionsByRoleId(role.id);
        return role;
    }

    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    @Transactional
    public Role addRole(String name, String description,
                        long deptId) throws AwesomeException {
        checkRoleName(null, name);
        checkDept(deptId);

        Role role = new Role();
        role.name = name;
        role.description = description;
        role.deptId = deptId;
        role.type = deptId == Config.DEFAULT_SYS_DEPT_ID ? Role.TYPE_SYSTEM : Role.TYPE_DEPT;
        roleMapper.insertSelective(role);
        return role;
    }

    @Transactional
    public void updateRole(long id, String name, String description,
                           List<Long> permissionIds) throws AwesomeException {
        updateRole(id, name, description);
        setPermissions(id, permissionIds);
    }

    public void updateRole(long id, String name, String description) throws AwesomeException {
        checkRoleName(id, name);
        Role role = new Role();
        role.id = id;
        role.name = name;
        role.description = Strings.nullToEmpty(description);
        role.modifyTime = Timestamp.from(Instant.now());
        int count = roleMapper.updateByPrimaryKeySelective(role);
        if (count == 0) {
            throw new AwesomeException(Config.ERROR_ROLE_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteRole(long id) throws AwesomeException {
        Role role = roleMapper.selectByPrimaryKey(id);
        if (role == null) {
            throw new AwesomeException(Config.ERROR_ROLE_NOT_FOUND);
        }
        if (role.type == Role.TYPE_DEPT_PRE || role.type == Role.TYPE_SYSTEM_PRE) {
            throw new AwesomeException(Config.ERROR_DELETE_PRE_ROLE);
        }
        roleMapper.deleteByPrimaryKey(id);

        RolePermission rolePermission = new RolePermission();
        rolePermission.roleId = id;
        rolePermissionMapper.delete(rolePermission);

        DeptAccount deptAccount = new DeptAccount();
        deptAccount.roleId = id;
        deptAccountMapper.delete(deptAccount);
    }

    private void checkRoleName(Long id, String name) throws AwesomeException {
        if (Strings.isNullOrEmpty(name)) {
            throw new AwesomeException(Config.ERROR_EMPTY_ROLE_NAME);
        }

        if (id != null) {
            Role role = roleMapper.selectByPrimaryKey(id);
            if (name.equals(role.name)) {
                return;
            }
        }

        Role role = new Role();
        role.name = name;
        if (roleMapper.selectCount(role) > 0) {
            throw new AwesomeException(Config.ERROR_DUPLICATE_ROLE_NAME);
        }
    }

    private void checkDept(long deptId) throws AwesomeException {
        if (null == departmentMapper.selectByPrimaryKey(deptId)) {
            throw new AwesomeException(Config.ERROR_DEPT_NOT_FOUND);
        }
    }

    public void addPermission(long roleId, long permissionId) throws AwesomeException {
        Role role = checkRole(roleId);
        PermissionModel permissionModel = checkPermission(permissionId);
        if ((role.type == Role.TYPE_SYSTEM || role.type == Role.TYPE_SYSTEM_PRE)
                && permissionModel.type != PermissionModel.TYPE_SYSTEM
                ||
                (role.type == Role.TYPE_DEPT || role.type == Role.TYPE_DEPT_PRE)
                        && permissionModel.type != PermissionModel.TYPE_DEPT) {
            throw new AwesomeException(Config.ERROR_ROLE_PERMISSION_NOT_MATCH);
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.roleId = roleId;
        rolePermission.permissionId = permissionId;
        if (rolePermissionMapper.selectCount(rolePermission) > 0) {
            // ignore if permission already exist
            return;
        }

        rolePermissionMapper.insertSelective(rolePermission);
    }

    private void setPermissions(long roleId, List<Long> permissionList) throws AwesomeException {
        // remove all  permission
        RolePermission rolePermission = new RolePermission();
        rolePermission.roleId = roleId;
        rolePermissionMapper.delete(rolePermission);

        // add permission
        if (permissionList != null) {
            for (long pmId : permissionList) {
                addPermission(roleId, pmId);
            }
        }
    }

    public void removePermission(long roleId, long permissionId) throws AwesomeException {
        checkRole(roleId);
        checkPermission(permissionId);

        RolePermission rolePermission = new RolePermission();
        rolePermission.roleId = roleId;
        rolePermission.permissionId = permissionId;
        rolePermissionMapper.delete(rolePermission);
    }

    private Role checkRole(long roleId) throws AwesomeException {
        Role role = roleMapper.selectByPrimaryKey(roleId);
        if (role == null) {
            throw new AwesomeException(Config.ERROR_ROLE_NOT_FOUND);
        }
        return role;
    }

    private PermissionModel checkPermission(long permissionId) throws AwesomeException {
        PermissionModel permission = permissionMapper.selectByPrimaryKey(permissionId);
        if (permission == null) {
            throw new AwesomeException(Config.ERROR_PERMISSION_NOT_FOUND);
        }
        return permission;
    }
}
