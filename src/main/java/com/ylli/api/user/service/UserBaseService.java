package com.ylli.api.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.UserBaseMapper;
import com.ylli.api.user.model.UserBase;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBaseService {

    @Autowired
    UserBaseMapper userBaseMapper;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    public void register(UserBase userBase) {

        UserBase base = userBaseMapper.selectByMchId(userBase.mchId);
        if (base == null) {
            base = init(userBase.mchId, userBase.linkPhone);
        }
        if (base.state == UserBase.PASS) {
            throw new AwesomeException(Config.ERROR_AUDIT_PASS);
        }
        modelMapper.map(userBase, base);
        userBaseMapper.updateByPrimaryKeySelective(base);
    }


    @Transactional
    public Object audit(Long mchId, Integer state) {
        UserBase userBase = userBaseMapper.selectByMchId(mchId);
        if (userBase == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        userBase.state = state;
        userBase.modifyTime = Timestamp.from(Instant.now());
        userBaseMapper.updateByPrimaryKeySelective(userBase);
        return userBaseMapper.selectByPrimaryKey(userBase);
    }

    @Transactional
    public UserBase init(Long mchId, String phone) {
        UserBase userBase = new UserBase();
        userBase.mchId = mchId;
        userBase.linkPhone = phone;
        userBase.state = UserBase.NEW;
        userBaseMapper.insertSelective(userBase);
        return userBaseMapper.selectByPrimaryKey(userBase.id);
    }

    public Integer getState(Long id) {
        UserBase userBase = new UserBase();
        userBase.mchId = id;
        userBase = userBaseMapper.selectOne(userBase);
        return Optional.ofNullable(userBase).map(base -> base.state).orElse(UserBase.NEW);
    }

    public DataList<UserBase> getBase(Long mchId, Integer state, String mchName, String name, String phone,  String businessLicense, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<UserBase> page = (Page<UserBase>) userBaseMapper.getBase(mchId, state, mchName, name, phone,  businessLicense);
        DataList<UserBase> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }
}
