package com.ylli.api.example.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.example.Config;
import com.ylli.api.example.mapper.ExampleMapper;
import com.ylli.api.example.model.Example;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.model.base.DataList;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by ylli on 2017/5/10.
 */
@Service
public class ExampleService {

    @Autowired
    ExampleMapper exampleMapper;

    public DataList<Example> getList(int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<Example> list = (Page<Example>) exampleMapper.selectAll();
        return new DataList<>(list);
    }

    public Example get(Long id) {
        return exampleMapper.selectByPrimaryKey(id);
    }

    @Transactional
    public Example add(String username, Boolean enabled, List<Long> images,
                       List<String> imageUrls) {
        if (Strings.isNullOrEmpty(username)) {
            throw new AwesomeException(Config.ERROR_EMPTY_USERNAME);
        }
        Example example = new Example();
        example.name = username;
        example.enabled = Boolean.TRUE.equals(enabled);
        example.images = images;
        example.imageUrls = imageUrls;

        exampleMapper.insertSelective(example);
        return exampleMapper.selectByPrimaryKey(example.id);
    }

    @Transactional
    public void delete(Long id) {
        if (exampleMapper.selectByPrimaryKey(id) == null) {
            throw new AwesomeException(Config.ERROR_EXAMPLE_NOT_FOUND);
        }
        exampleMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public void update(Long id, String username, Boolean enabled) {
        if (exampleMapper.selectByPrimaryKey(id) == null) {
            throw new AwesomeException(Config.ERROR_EXAMPLE_NOT_FOUND);
        }
        Example example = new Example();
        example.name = username;
        example.enabled = enabled;
        example.modifyTime = Timestamp.from(Instant.now());
        exampleMapper.updateByPrimaryKeySelective(example);
    }
}
