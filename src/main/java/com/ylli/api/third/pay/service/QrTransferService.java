package com.ylli.api.third.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.util.RedisUtil;
import com.ylli.api.third.pay.Config;
import com.ylli.api.third.pay.mapper.QrCodeMapper;
import com.ylli.api.third.pay.model.QrCode;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QrTransferService {

    @Autowired
    QrCodeMapper qrCodeMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    BillService billService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    AuthSession authSession;

    @Value("${pay.qr.code.apihost}")
    public String QrApiHost;

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

        //redis add.
        List<String> urls = qrCodeMapper.selectAll().stream().map(i -> i.codeUrl).collect(Collectors.toList());
        redisUtil.initUrl(urls);
    }

    @Transactional
    public void deleteQrCode(Long id) {
        QrCode qrCode = qrCodeMapper.selectByPrimaryKey(id);
        if (qrCode == null) {
            throw new AwesomeException(Config.ERROR_QR_CODE_NOT_FOUND);
        }
        qrCodeMapper.delete(qrCode);

        //redis add.
        List<String> urls = qrCodeMapper.selectAll().stream().map(i -> i.codeUrl).collect(Collectors.toList());
        redisUtil.initUrl(urls);
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

    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl,
                              String reserve, String payType, String tradeType, Object extra) {

        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        String url = redisUtil.getUrl(bill.sysOrderId);
        QrCode qrCode = qrCodeMapper.selectByUrl(url);

        //设置关键字为订单序列号
        String reserveWord = bill.sysOrderId.substring(16);
        bill.reserveWord = reserveWord;
        bill.qrOwner = qrCode.authId;
        billMapper.updateByPrimaryKeySelective(bill);

        if (Strings.isNullOrEmpty(url)) {
            return null;
        } else {
            //封装自己的url.
            StringBuffer sb = new StringBuffer(QrApiHost)
                    .append("&link=").append(url)
                    .append("&reserve_key=").append(reserveWord);
            String uid = qrCode.uid;
            if (!Strings.isNullOrEmpty(uid)) {
                sb.append("&native=").append(getNativeUrl(uid, String.format("%.2f", (money / 100.0)), bill.reserveWord));
            }
            return sb.toString();
        }
    }

    public String getNativeUrl(String uid, String money, String reserveWord) {
        return "alipays://platformapi/startapp?appId=20000123&actionType=scan&biz_data={\"s\": \"money\", \"u\": \"" + uid + "\", \"a\": \"" + money + "\", \"m\": \"" + reserveWord + "\"}";
    }

    @Transactional
    public void uploadUid(Long id,Long authId, String uid) {
        QrCode qrCode = qrCodeMapper.selectByPrimaryKey(id);
        if (qrCode == null) {
            throw new AwesomeException(Config.ERROR_QR_CODE_NOT_FOUND);
        }
        if (authSession.getAuthId() != authId || qrCode.authId.longValue() != authId.longValue()) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        QrCode exist = new QrCode();
        exist.uid = uid;
        exist = qrCodeMapper.selectOne(exist);
        if (exist != null) {
            throw new AwesomeException(Config.ERROR_UID_EXIST);
        }
        qrCode.uid = uid;
        qrCodeMapper.updateByPrimaryKeySelective(qrCode);
    }
}
