package com.ylli.api.auth.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by RexQian on 2017/2/22.
 */
@Table(name = "t_role_permission")
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long roleId;

    public Long permissionId;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
