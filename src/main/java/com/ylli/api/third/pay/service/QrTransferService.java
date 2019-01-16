package com.ylli.api.third.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.third.pay.Config;
import com.ylli.api.third.pay.mapper.QrCodeMapper;
import com.ylli.api.third.pay.model.QrCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QrTransferService {

    @Autowired
    QrCodeMapper qrCodeMapper;

    @Transactional
    public void uploadQrCode(Long authId, String codeUrl) {
        //toUpperCase
        codeUrl = codeUrl.toUpperCase();

        QrCode qrCode = new QrCode();
        qrCode.codeUrl = codeUrl;
        qrCode = qrCodeMapper.selectOne(qrCode);
        if (qrCode != null) {
            throw new AwesomeException(Config.ERROR_URL_EXIST);
        }
        qrCode = new QrCode();
        qrCode.authId = authId;
        qrCode.codeUrl = codeUrl;
        qrCodeMapper.insertSelective(qrCode);

        //TODO redis add.
    }

    @Transactional
    public void deleteQrCode(Long id) {
        QrCode qrCode = qrCodeMapper.selectByPrimaryKey(id);
        if (qrCode == null) {
            throw new AwesomeException(Config.ERROR_QR_CODE_NOT_FOUND);
        }
        qrCodeMapper.delete(qrCode);
        //TODO redis remove.


    }


    public Object qrCodes(Long authId, String nickName, String phone, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<QrCode> page = (Page<QrCode>) qrCodeMapper.selectByCondition(authId, nickName, phone);

        DataList<QrCode> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }
}
