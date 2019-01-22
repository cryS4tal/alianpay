package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.model.MchBase;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MchBaseService {

    @Autowired
    MchBaseMapper mchBaseMapper;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    public void register(MchBase userBase) {

        MchBase base = mchBaseMapper.selectByMchId(userBase.mchId);
        if (base == null) {
            base = init(userBase.mchId, userBase.linkPhone);
        }
        if (base.state == MchBase.PASS) {
            throw new AwesomeException(Config.ERROR_AUDIT_PASS);
        }
        //强制转换.
        userBase.state = MchBase.NEW;
        //fix use modelMapper cause not update data
        userBase.id  = base.id;
        modelMapper.map(userBase, base);
        mchBaseMapper.updateByPrimaryKeySelective(base);
    }


    @Transactional
    public Object audit(Long mchId, Integer state) {
        MchBase userBase = mchBaseMapper.selectByMchId(mchId);
        if (userBase == null) {
            throw new AwesomeException(Config.ERROR_MCH_NOT_FOUND);
        }
        userBase.state = state;
        userBase.modifyTime = Timestamp.from(Instant.now());
        mchBaseMapper.updateByPrimaryKeySelective(userBase);
        return mchBaseMapper.selectByPrimaryKey(userBase);
    }

    @Transactional
    public MchBase init(Long mchId, String phone) {
        MchBase userBase = new MchBase();
        userBase.mchId = mchId;
        userBase.linkPhone = phone;
        userBase.state = MchBase.NEW;
        mchBaseMapper.insertSelective(userBase);
        return mchBaseMapper.selectByPrimaryKey(userBase.id);
    }

    public Integer getState(Long id) {
        MchBase userBase = new MchBase();
        userBase.mchId = id;
        userBase = mchBaseMapper.selectOne(userBase);
        return Optional.ofNullable(userBase).map(base -> base.state).orElse(MchBase.NEW);
    }

    public MchBase getBase(Long id) {
        MchBase userBase = new MchBase();
        userBase.mchId = id;
        userBase = mchBaseMapper.selectOne(userBase);
        return userBase;
    }

    public DataList<MchBase> getBase(Long mchId, Integer state, String mchName, String name, String phone, String businessLicense, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<MchBase> page = (Page<MchBase>) mchBaseMapper.getBase(mchId, state, mchName, name, phone, businessLicense);
        DataList<MchBase> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    @Transactional
    public MchBase update(MchBase userBase) {
        MchBase base = mchBaseMapper.selectByMchId(userBase.mchId);
        if (base == null) {
            base = init(userBase.mchId, userBase.linkPhone);
        }
        //fix use modelMapper cause not update data
        userBase.id  = base.id;
        modelMapper.map(userBase, base);
        mchBaseMapper.updateByPrimaryKeySelective(base);
        return mchBaseMapper.selectByPrimaryKey(userBase);
    }

    @Transactional
    public Object setAgency(Long mchId) {
        MchBase mchBase = mchBaseMapper.selectByMchId(mchId);
        if (mchBase == null) {
            throw new AwesomeException(Config.ERROR_MCH_NOT_FOUND);
        }
        //TODO 回滚操作.
        mchBase.isAgency = !mchBase.isAgency;
        mchBaseMapper.updateByPrimaryKeySelective(mchBase);
        return mchBase;
    }
}
