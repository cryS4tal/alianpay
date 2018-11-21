package com.ylli.api.auth.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ylli.api.model.base.SimpleObject;
import java.util.List;

/**
 * Created by RexQian on 2017/4/8.
 */
public class RoleChange implements NoticeData {
    public SimpleObject dept;
    public List<Role> oldRoles;
    public List<Role> newRoles;

    @Override
    public String type() {
        return "role_change";
    }

    @Override
    public String extras() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        return gson.toJson(this);
    }

    @Override
    public String title() {
        if (newRoles.isEmpty()) {
            return String.format("您被移除出了“%s”组织机构", dept.name);
        } else if (oldRoles.isEmpty()) {
            return String.format("您加入了“%s”组织机构", dept.name);
        }
        return "您的角色发生了变化";
    }

    @Override
    public String description() {
        if (newRoles.isEmpty()) {
            return String.format("您被移除出了“%s”组织机构", dept.name);
        }
        StringBuilder sb = new StringBuilder();
        for (Role newRole : newRoles) {
            sb.append("“");
            sb.append(newRole.name);
            sb.append("”");
        }
        return String.format("您在“%s”组织机构被设为%s角色", dept.name, sb.toString());
    }
}
