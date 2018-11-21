package com.ylli.api.auth.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.mapper.DepartmentMapper;
import com.ylli.api.auth.mapper.DeptAccountMapper;
import com.ylli.api.auth.mapper.RolePermissionMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.Department;
import com.ylli.api.auth.model.RolePermission;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yqpeng on 2017/2/20.
 */
@Service
public class DepartmentService {
    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DeptAccountMapper deptAccountMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    public Page<Department> list(String nameLike, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        List<Department> list = departmentMapper.getDeptList(nameLike);
        return (Page<Department>) list;
    }

    public Department queryById(long id) {
        return departmentMapper.selectByPrimaryKey(id);
    }

    public void patch(long deptId, String name, String desc) {
        Department department = new Department();
        department.id = deptId;
        department.name = name;
        department.description = desc;
        department.modifyTime = Timestamp.from(Instant.now());
        departmentMapper.updateByPrimaryKeySelective(department);
    }

    public Page<Account> getAccountList(Long deptId, String nameLike,
                                        Long roleId, Long permissionId,
                                        int offset, int limit) {
        List<Long> roleList = new ArrayList<>();
        if (permissionId != null) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.permissionId = permissionId;
            List<RolePermission> list = rolePermissionMapper.select(rolePermission);
            for (RolePermission item : list) {
                roleList.add(item.roleId);
            }
        } else if (roleId != null) {
            roleList.add(roleId);
        }
        if (roleList.isEmpty()) {
            roleList = null;
        }
        PageHelper.offsetPage(offset, limit);
        Page<Account> accounts = (Page<Account>) deptAccountMapper.getAccountList(
                deptId, nameLike, roleList);
        return accounts;
    }

    public List<Department> getDeptsByAccount(long accountId) {
        return deptAccountMapper.getDeptList(accountId);
    }
}
