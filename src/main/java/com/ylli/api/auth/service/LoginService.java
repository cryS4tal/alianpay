package com.ylli.api.auth.service;

import com.ylli.api.auth.Config;
import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.PhoneAuth;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.MergeUtil;
import com.ylli.api.user.service.UserAppService;
import com.ylli.api.user.service.UserSettlementService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by ylli on 2018/11/20.
 */
@Service
public class LoginService {

    @Autowired
    AuthSession authSession;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    RoleService roleService;

    @Autowired
    AccountService accountService;

    @Autowired
    PhoneAuthService phoneAuthService;

    @Autowired
    UserSettlementService userInfoService;

    @Autowired
    UserAppService userAppService;

    @Autowired
    MergeUtil mergeUtil;

    @Value("${login.phone_auth_required}")
    boolean isPhoneAuthRequired;

    public Map login(long accountId) {
        Account account = accountService.getById(accountId);
        if (!account.state.equals(Account.STATE_ENABLE)) {
            throw new AwesomeException(Config.ERROR_USER_DISABLE);
        }
        authSession.setAuth(account.id);
        return getOwnInfo();
    }

    public Map getOwnInfo() {
        long id = authSession.getAuthId();
        Account account = accountService.getById(id);
        Map map = mergeUtil.builder()
                .merge(account)
                .merge("depts", departmentService.getDeptsByAccount(account.id))
                .merge("roles", roleService.getListByAccountAndDept(account.id, null))
                .merge("phone", () -> {
                    PhoneAuth phoneAuth = phoneAuthService.getByAccountId(id);
                    return phoneAuth != null ? phoneAuth.phone : null;
                })
                //.merge("user_info", userInfoService.getUserInfo(account.id))
                //.merge("apps", userAppService.getApp(account.id))
                .create();
        return map;
    }

    public Map getPayOwnInfo(String openid) {
        Map map = mergeUtil.builder()
                .merge("openid", openid == null ? "" : openid)
                .create();
        return map;
    }
}
