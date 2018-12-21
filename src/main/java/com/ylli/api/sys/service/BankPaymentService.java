package com.ylli.api.sys.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.sys.Config;
import com.ylli.api.sys.mapper.BankPaymentMapper;
import com.ylli.api.sys.model.BankPayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankPaymentService {

    @Autowired
    BankPaymentMapper bankPaymentMapper;

    @Transactional
    public void paymentSwitch(Long id, Boolean isOpen) {
        BankPayment payment = bankPaymentMapper.selectByPrimaryKey(id);
        if (payment == null) {
            throw new AwesomeException(Config.ERROR_PAYMENT_NOT_FOUND);
        }
        payment.state = isOpen;
        bankPaymentMapper.updateByPrimaryKeySelective(payment);
    }

    public DataList<BankPayment> bankPays(int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<BankPayment> page = (Page<BankPayment>) bankPaymentMapper.selectAll();

        DataList<BankPayment> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }
}
