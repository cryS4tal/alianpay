package com.ylli.api.third.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.service.AppService;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.RedisUtil;
import com.ylli.api.third.pay.Config;
import com.ylli.api.third.pay.mapper.QrCodeMapper;
import com.ylli.api.third.pay.model.QrCode;
import com.ylli.api.wallet.service.WalletService;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
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

    @Autowired
    MchBaseMapper mchBaseMapper;

    @Autowired
    AppService appService;

    @Autowired
    WalletService walletService;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Value("${pay.qr.code.apihost}")
    public String QrApiHost;

    @PostConstruct
    void init() {
        List<String> urls = qrCodeMapper.selectAll().stream().filter(i -> i.enable).map(i -> i.codeUrl).collect(Collectors.toList());
        redisUtil.initUrl(urls);
    }


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
        qrCode.enable = true;
        qrCodeMapper.insertSelective(qrCode);

        //redis add.
        List<String> urls = qrCodeMapper.selectAll().stream().filter(i -> i.enable).map(i -> i.codeUrl).collect(Collectors.toList());
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
        List<String> urls = qrCodeMapper.selectAll().stream().filter(i -> i.enable).map(i -> i.codeUrl).collect(Collectors.toList());
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

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl,
                              String reserve, String payType, String tradeType, Object extra) {

        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        String url = redisUtil.getUrl(bill.sysOrderId);
        if (!Strings.isNullOrEmpty(url)) {
            QrCode qrCode = qrCodeMapper.selectByUrl(url);
            bill.qrOwner = qrCode.authId;

            //设置关键字为订单序列号
            String reserveWord = bill.sysOrderId.substring(16);
            bill.reserveWord = reserveWord;
            billMapper.updateByPrimaryKeySelective(bill);

            //封装自己的url.
            StringBuffer sb = new StringBuffer(QrApiHost)
                    .append("link=").append(url)
                    .append("&money=").append(bill.money)
                    .append("&sys_order_id=").append(bill.sysOrderId)
                    .append("&reserve_word=").append(reserveWord);
            String uid = qrCode.uid;
            if (!Strings.isNullOrEmpty(uid)) {
                sb.append("&native=").append(getNativeUrl(uid, String.format("%.2f", (money / 100.0)), bill.reserveWord));
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    public String getNativeUrl(String uid, String money, String reserveWord) {
        return "alipays://platformapi/startapp?appId=20000123&actionType=scan&biz_data={\"s\": \"money\", \"u\": \"" + uid + "\", \"a\": \"" + money + "\", \"m\": \"" + reserveWord + "\"}";
    }

    @Transactional
    public void uploadUid(Long id, Long authId, String uid) {
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

    public Object getOrders(Long authId, String nickName, String phone, Integer status, String sysOrderId,
                            String mchOrderId, Date startTime, Date endTime, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<Bill> page = (Page<Bill>) billMapper.getOrders(authId, nickName, phone, status, sysOrderId, mchOrderId, startTime, endTime);

        DataList<Bill> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    @Transactional
    public void finish(String sysOrderId, Integer money) throws Exception {
        Bill bill = billService.selectBySysOrderId(sysOrderId);
        if (bill == null) {
            throw new AwesomeException(Config.ERROR_BILL_NOT_FOUND);
        }

        //补单操作..
        if (bill.status == Bill.NEW || bill.status == Bill.AUTO_CLOSE) {
            bill.status = Bill.FINISH;
            bill.tradeTime = Timestamp.from(Instant.now());
            bill.payCharge = (money * appService.getRate(bill.mchId, bill.appId)) / 10000;
            //不返回上游订单号.
            bill.superOrderId = new StringBuffer().append("unknown").append(bill.id).toString();

            bill.msg = (new BigDecimal(money).divide(new BigDecimal(100))).toString();
            billMapper.updateByPrimaryKeySelective(bill);

            //钱包金额变动。
            walletService.incr(bill.mchId, money - bill.payCharge);

            //加入异步通知下游商户系统
            //params jsonStr.
            if (!Strings.isNullOrEmpty(bill.notifyUrl)) {
                String params = payService.generateRes(
                        money.toString(),
                        bill.mchOrderId,
                        bill.sysOrderId,
                        bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                        bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                        bill.reserve);

                payClient.sendNotify(bill.id, bill.notifyUrl, params, true);
            }
        } else {
            throw new AwesomeException(com.ylli.api.pay.Config.ERROR_BILL_STATUS);
        }
    }

    @Transactional
    public void login(long authId) {
        QrCode qrCode = new QrCode();
        qrCode.authId = authId;
        List<QrCode> list = qrCodeMapper.select(qrCode);
        list.stream().forEach(i -> {
            i.enable = true;
            qrCodeMapper.updateByPrimaryKeySelective(i);
        });
        List<String> urls = qrCodeMapper.selectAll().stream().filter(i -> i.enable).map(i -> i.codeUrl).collect(Collectors.toList());
        redisUtil.initUrl(urls);
    }

    @Transactional
    public void logout(long authId) {
        QrCode qrCode = new QrCode();
        qrCode.authId = authId;
        List<QrCode> list = qrCodeMapper.select(qrCode);
        list.stream().forEach(i -> {
            i.enable = false;
            qrCodeMapper.updateByPrimaryKeySelective(i);
        });
        List<String> urls = qrCodeMapper.selectAll().stream().filter(i -> i.enable).map(i -> i.codeUrl).collect(Collectors.toList());
        redisUtil.initUrl(urls);
    }

    @Transactional
    public void rollback(String sysOrderId) {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        bill = billMapper.selectOne(bill);
        if (bill == null) {
            throw new AwesomeException(com.ylli.api.pay.Config.ERROR_BILL_NOT_FOUND);
        }
        if (bill.status != Bill.FINISH) {
            throw new AwesomeException(com.ylli.api.pay.Config.ERROR_BILL_STATUS);
        }
        if (!bill.superOrderId.startsWith("unknown")) {
            throw new AwesomeException(com.ylli.api.pay.Config.ERROR_BILL_ROLLBACK);
        }
        // 9 为 个码支付 TODO 加入缓存.全局控制
        if (bill.channelId != 9) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }

        if (bill.status == Bill.FINISH) {
            //钱包金额变动。
            walletService.rollback(bill.mchId, Double.valueOf(Double.valueOf(bill.msg) * 100).intValue() - bill.payCharge);

            billMapper.rollback(sysOrderId);
        }
    }
}
