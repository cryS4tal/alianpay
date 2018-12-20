package com.ylli.api.auth.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.auth.Config;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.model.Account;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by RexQian on 2017/2/21.
 */
@Service
public class AccountService {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    RoleService roleService;

    public Account getById(long id) {
        return accountMapper.selectByPrimaryKey(id);
    }

    /*public Page<Account> getList(String nameLike, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        return (Page<Account>) accountMapper.getAccounts(nameLike);
    }*/

    /*public SimpleAccount getSimpleAccount(long id) {
        Account account = accountMapper.selectByPrimaryKey(id);
        if (account == null) {
            return null;
        }
        SimpleAccount simpleAccount = new SimpleAccount();
        simpleAccount.id = account.id;
        simpleAccount.name = account.nickname;
        simpleAccount.avatar = account.avatar;
        return simpleAccount;
    }*/

    private String getNickname(String nickname) {
        if (Strings.isNullOrEmpty(nickname)) {
            nickname = "USER" + UUID.randomUUID().toString();
        }
        return nickname;
    }

    public Account create(String nickname, String avatar) {
        Account account = new Account();

        account.nickname = getNickname(nickname);
        account.username = UUID.randomUUID().toString();
        account.avatar = avatar;
        accountMapper.insertSelective(account);

        // add to personal dept
        roleService.addRole(Config.DEFAULT_PERSONAL_DEPT_ID,
                account.id, Config.ROLE_PERSONAL);

        return accountMapper.selectByPrimaryKey(account.id);
    }
}
