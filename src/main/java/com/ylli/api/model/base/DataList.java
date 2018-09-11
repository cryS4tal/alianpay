package com.ylli.api.model.base;

import com.github.pagehelper.Page;
import java.util.List;

/**
 * Created by RexQian on 2017/4/18.
 */
public class DataList<T> {
    public Integer offset;
    public Integer count;
    public Long totalCount;
    public List<T> dataList;

    public DataList() {

    }

    public DataList(Page<T> page) {
        offset = page.getStartRow();
        count = page.size();
        totalCount = page.getTotal();
        dataList = page;
    }
}
