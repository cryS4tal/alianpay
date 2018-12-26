package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.model.MchBase;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MchBaseService {

    @Autowired
    MchBaseMapper userBaseMapper;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    public void register(MchBase userBase) {

        MchBase base = userBaseMapper.selectByMchId(userBase.mchId);
        if (base == null) {
            base = init(userBase.mchId, userBase.linkPhone);
        }
        if (base.state == MchBase.PASS) {
            throw new AwesomeException(Config.ERROR_AUDIT_PASS);
        }
        //强制转换.
        userBase.state = MchBase.NEW;
        //fix use modelMapper cause not update data
        userBase.id = base.id;
        modelMapper.map(userBase, base);
        userBaseMapper.updateByPrimaryKeySelective(base);
    }


    @Transactional
    public Object audit(Long mchId, Integer state) {
        MchBase userBase = userBaseMapper.selectByMchId(mchId);
        if (userBase == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        userBase.state = state;
        userBase.modifyTime = Timestamp.from(Instant.now());
        userBaseMapper.updateByPrimaryKeySelective(userBase);
        return userBaseMapper.selectByPrimaryKey(userBase);
    }

    @Transactional
    public MchBase init(Long mchId, String phone) {
        MchBase userBase = new MchBase();
        userBase.mchId = mchId;
        userBase.linkPhone = phone;
        userBase.state = MchBase.NEW;
        userBaseMapper.insertSelective(userBase);
        return userBaseMapper.selectByPrimaryKey(userBase.id);
    }

    public Integer getState(Long id) {
        MchBase userBase = new MchBase();
        userBase.mchId = id;
        userBase = userBaseMapper.selectOne(userBase);
        return Optional.ofNullable(userBase).map(base -> base.state).orElse(MchBase.NEW);
    }

    public MchBase getBase(Long id) {
        MchBase userBase = new MchBase();
        userBase.mchId = id;
        userBase = userBaseMapper.selectOne(userBase);
        return userBase;
    }

    public DataList<MchBase> getBase(Long mchId, Integer state, String mchName, String name, String phone, String businessLicense, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<MchBase> page = (Page<MchBase>) userBaseMapper.getBase(mchId, state, mchName, name, phone, businessLicense);
        DataList<MchBase> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    @Transactional
    public MchBase update(MchBase userBase) {
        MchBase base = userBaseMapper.selectByMchId(userBase.mchId);
        if (base == null) {
            base = init(userBase.mchId, userBase.linkPhone);
        }
        //fix use modelMapper cause not update data
        userBase.id = base.id;
        modelMapper.map(userBase, base);
        userBaseMapper.updateByPrimaryKeySelective(base);
        return userBaseMapper.selectByPrimaryKey(userBase);
    }

    public Object getMchNameLike(String mchName) {
        if (!Strings.isNullOrEmpty(mchName))
            mchName = mchName + "%";
        return userBaseMapper.getMchNameLike(mchName);
    }
}
