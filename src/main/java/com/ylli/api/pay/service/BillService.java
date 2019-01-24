package com.ylli.api.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.Config;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.BaseBill;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.SumAndCount;
import com.ylli.api.pay.util.RedisUtil;
import com.ylli.api.sys.model.SysChannel;
import com.ylli.api.sys.service.ChannelService;
import com.ylli.api.third.pay.model.WzQueryRes;
import com.ylli.api.third.pay.service.WzClient;
import com.ylli.api.third.pay.service.YfbClient;
import com.ylli.api.wallet.service.WalletService;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillService {

    @Autowired
    BillMapper billMapper;

    @Autowired
    WzClient wzClient;

    @Autowired
    YfbClient yfbClient;

    @Autowired
    WalletService walletService;

    @Autowired
    RateService appService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    MchBaseMapper mchBaseMapper;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Autowired
    ChannelService channelService;

    /**
     * @param mchIds
     * @param status
     * @param mchOrderId
     * @param sysOrderId
     * @param payType
     * @param tradeTime
     * @param startTime
     * @param endTime
     * @return
     */

    public Object getBills(List<Long> mchIds, Integer status, String mchOrderId, String sysOrderId, String payType,
                           Date tradeTime, Date startTime, Date endTime, Boolean admin, int offset, int limit) {

        PageHelper.offsetPage(offset, limit);
        Page<Bill> page = (Page<Bill>) billMapper.getBills(mchIds, status, mchOrderId, sysOrderId, payType, tradeTime, startTime, endTime);

        DataList<BaseBill> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        List<BaseBill> list = new ArrayList();
        for (Bill bill : page) {
            BaseBill object = convert(bill, admin);
            list.add(object);
        }
        dataList.dataList = list;
        return dataList;

    }

    private BaseBill convert(Bill bill, Boolean admin) {
        BaseBill baseBill = new BaseBill();
        baseBill.mchId = bill.mchId;
        baseBill.mchName = Optional.ofNullable(mchBaseMapper.selectByMchId(bill.mchId)).map(i -> i.mchName).orElse(null);
        baseBill.mchOrderId = bill.mchOrderId;
        baseBill.sysOrderId = bill.sysOrderId;
        baseBill.superOrderId = bill.superOrderId;
        baseBill.money = bill.money;
        baseBill.mchCharge = bill.payCharge;
        baseBill.payType = typeToString(bill.payType);
        baseBill.state = bill.status;
        if (bill.tradeTime != null) {
            baseBill.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime);
        }
        baseBill.createTime = bill.createTime;
        if (admin) {
            baseBill.channel = channelService.getChannelName(bill.channelId);
        }
        baseBill.msg = bill.msg;
        return baseBill;
    }

    public Object getTodayDetail(Long mchId) {
        SumAndCount sumAndCount = billMapper.getTodayDetail(mchId);
        if (sumAndCount.total == null) {
            sumAndCount.total = 0L;
        }
        return sumAndCount;
    }

    public String typeToString(String payType) {
        return new StringBuffer()
                .append(payType)
                .toString()
                .replace(PayService.ALI, "支付宝")
                .replace(PayService.WX, "微信");
    }


    public boolean mchOrderExist(String mchOrderId) {
        Bill bill = new Bill();
        bill.mchOrderId = mchOrderId;
        return billMapper.selectOne(bill) != null;
    }

    public Bill selectByMchOrderId(String mchOrderId) {
        Bill bill = new Bill();
        bill.mchOrderId = mchOrderId;
        return billMapper.selectOne(bill);
    }

    /**
     *
     */
    public Bill orderQuery(String sysOrderId, String code) throws Exception {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        bill = billMapper.selectOne(bill);

        if (code.equals("WZ")) {

            WzQueryRes res = wzClient.orderQuery(sysOrderId);
            if (res.code.equals("success")) {
                if (bill.status != Bill.FINISH) {
                    bill.status = Bill.FINISH;
                    bill.tradeTime = Timestamp.from(Instant.now());
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);
                }
            } else if (res.code.equals("fail")) {
                if (bill.status == Bill.NEW) {
                    bill.status = Bill.FAIL;
                    billMapper.updateByPrimaryKeySelective(bill);
                }
            }
            return bill;

        } else if (code.equals("YFB") || code.equals("HRJF")) {

            String str = yfbClient.orderQuery(bill.sysOrderId);
            //orderid=20181203040702B000100200000025&opstate=0&ovalue=100.00&sign=52aeb9d6083a43130f3050468a37e30c&msg=查询成功
            str = StringUtils.substringAfter(str, "=");
            String orderid = StringUtils.substringBefore(str, "&");

            str = StringUtils.substringAfter(str, "=");
            String opstate = StringUtils.substringBefore(str, "&");

            str = StringUtils.substringAfter(str, "=");
            String ovalue = StringUtils.substringBefore(str, "&");

            str = StringUtils.substringAfter(str, "=");
            String sign = StringUtils.substringBefore(str, "&");

            boolean flag = yfbClient.signVerify(orderid, opstate, ovalue, sign);
            if (flag) {
                if (opstate.equals("0")) {
                    if (bill.status != Bill.FINISH) {
                        bill.status = Bill.FINISH;
                        bill.msg = ovalue;
                        bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                        bill.tradeTime = Timestamp.from(Instant.now());
                        billMapper.updateByPrimaryKeySelective(bill);

                        //钱包金额变动。
                        walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);
                    }
                }
                //其他情况 暂时不做处理.

            }
            return bill;

        } else if (code.equals("CNT")) {
            //TODO 接入cnt订单查询。

            return bill;
        } else {
            return bill;
        }
    }

    public Bill selectBySysOrderId(String sysOrderId) {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        return billMapper.selectOne(bill);
    }

    @Transactional
    public void autoClose() {
        billMapper.autoClose();
    }

    @Transactional
    public Bill createBill(Long mchId, String mchOrderId, Long channelId, String payType, String tradeType, Integer money, String reserve, String notifyUrl, String redirectUrl) {
        Bill bill = new Bill();
        bill.mchId = mchId;
        bill.sysOrderId = redisUtil.generateSysOrderId();
        bill.mchOrderId = mchOrderId;
        bill.channelId = channelId;
        bill.appId = appService.getAppId(payType);
        bill.money = money;
        bill.status = Bill.NEW;
        bill.reserve = reserve;
        bill.notifyUrl = notifyUrl;
        bill.redirectUrl = redirectUrl;
        bill.payType = payType;
        bill.tradeType = tradeType;
        billMapper.insertSelective(bill);
        return bill;
    }

    @Transactional
    public Object reissue(String sysOrderId) throws Exception {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        bill = billMapper.selectOne(bill);
        if (bill == null) {
            throw new AwesomeException(Config.ERROR_BILL_NOT_FOUND);
        }
        //补单操作..
        if (bill.status == Bill.NEW || bill.status == Bill.AUTO_CLOSE) {
            bill.status = Bill.FINISH;
            bill.tradeTime = Timestamp.from(Instant.now());
            bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;

            //不返回上游订单号.
            bill.superOrderId = new StringBuffer().append("unknown").append(bill.id).toString();
            bill.msg = (new BigDecimal(bill.money).divide(new BigDecimal(100))).toString();
            billMapper.updateByPrimaryKeySelective(bill);

            //钱包金额变动。
            walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);

            //加入异步通知下游商户系统
            //params jsonStr.
            if (!Strings.isNullOrEmpty(bill.notifyUrl)) {
                String params = payService.generateRes(
                        bill.money.toString(),
                        bill.mchOrderId,
                        bill.sysOrderId,
                        bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                        bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                        bill.reserve);

                payClient.sendNotify(bill.id, bill.notifyUrl, params, true);
            }
        } else {
            throw new AwesomeException(Config.ERROR_BILL_STATUS);
        }
        return convert(bill, true);
    }

    @Transactional
    public void rollback(String sysOrderId) {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        bill = billMapper.selectOne(bill);
        if (bill == null) {
            throw new AwesomeException(Config.ERROR_BILL_NOT_FOUND);
        }
        if (bill.status != Bill.FINISH) {
            throw new AwesomeException(Config.ERROR_BILL_STATUS);
        }

        if (!bill.superOrderId.startsWith("unknown")) {
            throw new AwesomeException(Config.ERROR_BILL_ROLLBACK);
        }
        if (bill.status == Bill.FINISH) {
            //钱包金额变动。
            walletService.rollback(bill.mchId, bill.money - bill.payCharge);

            billMapper.rollback(sysOrderId);
        }
    }

    //TODO 分段下载.过多会内存溢出
    public void exportBills(List<Long> mchIds, Integer status, String mchOrderId, String sysOrderId, String payType,
                            Date tradeTime, Date startTime, Date endTime, HttpServletResponse response) {
        List<Bill> list = billMapper.getBills(mchIds, status, mchOrderId, sysOrderId, payType, tradeTime, startTime, endTime);

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFRow row1 = sheet.createRow(0);
        HSSFCell cell1 = row1.createCell(0);
        HSSFCell cell2 = row1.createCell(1);
        HSSFCell cell3 = row1.createCell(2);
        HSSFCell cell4 = row1.createCell(3);
        HSSFCell cell5 = row1.createCell(4);
        HSSFCell cell6 = row1.createCell(5);
        HSSFCell cell7 = row1.createCell(6);
        HSSFCell cell8 = row1.createCell(7);
        HSSFCell cell9 = row1.createCell(8);
        HSSFCell cell10 = row1.createCell(9);
        HSSFCell cell11 = row1.createCell(10);
        cell1.setCellValue("id");
        cell2.setCellValue("商户号");
        cell3.setCellValue("系统订单号");
        cell4.setCellValue("商户订单号");
        cell5.setCellValue("上游订单号");
        cell6.setCellValue("是否手动补单");
        cell7.setCellValue("通道");
        cell8.setCellValue("订单状态");
        cell9.setCellValue("手续费/分");
        cell10.setCellValue("金额/元");
        cell11.setCellValue("创建时间(北京时间)");
        for (int i = 1; i <= list.size(); i++) {
            HSSFRow rowi = sheet.createRow(i);
            HSSFCell newCell1 = rowi.createCell(0);
            HSSFCell newCell2 = rowi.createCell(1);
            HSSFCell newCell3 = rowi.createCell(2);
            HSSFCell newCell4 = rowi.createCell(3);
            HSSFCell newCell5 = rowi.createCell(4);
            HSSFCell newCell6 = rowi.createCell(5);
            HSSFCell newCell7 = rowi.createCell(6);
            HSSFCell newCell8 = rowi.createCell(7);
            HSSFCell newCell9 = rowi.createCell(8);
            HSSFCell newCell10 = rowi.createCell(9);
            HSSFCell newCell11 = rowi.createCell(10);
            newCell1.setCellValue(list.get(i - 1).id);
            newCell2.setCellValue(list.get(i - 1).mchId);
            newCell3.setCellValue(list.get(i - 1).sysOrderId);
            newCell4.setCellValue(list.get(i - 1).mchOrderId);
            newCell5.setCellValue(list.get(i - 1).superOrderId);

            if (!Strings.isNullOrEmpty(list.get(i - 1).superOrderId)) {
                newCell6.setCellValue(list.get(i - 1).superOrderId.startsWith("unknown") ? "是" : "否");
            }
            newCell7.setCellValue(SysChannel.getName(list.get(i - 1).channelId));
            newCell8.setCellValue(Bill.getStatus(list.get(i - 1).status));
            newCell9.setCellValue(Optional.ofNullable(list.get(i - 1).payCharge).map(j -> j.toString()).orElse(""));
            newCell10.setCellValue(list.get(i - 1).msg);
            newCell11.setCellValue(convertZ8(list.get(i - 1).createTime));
        }
        OutputStream output = null;
        try {
            output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=orders.xls");
            response.setContentType("application/msexcel");
            wb.write(output);
            output.flush();
        } catch (IOException ex) {
            throw new AwesomeException(Config.ERROR_FAILURE_BILL_EXCEL_EXPORT);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    //logger.info("stream closed failure");
                }
            }
        }
    }

    /**
     * Z0 to Z8
     */
    public String convertZ8(Timestamp ts) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ts);
        calendar.add(Calendar.HOUR, 8);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    public Bill selectBySuperOrderId(String superOrderId) {
        Bill bill = new Bill();
        bill.superOrderId = superOrderId;
        return billMapper.selectOne(bill);
    }

    @Transactional
    public void orderFail(String mchOrderId) {
        Bill bill = selectByMchOrderId(mchOrderId);
        bill.status = Bill.FAIL;
        billMapper.updateByPrimaryKeySelective(bill);
    }

    public Bill getBntBill(String sysOrderId) {
        Bill bill = selectBySysOrderId(sysOrderId);
        if (bill == null) {
            return null;
        }
        Bill bill1 = new Bill();
        bill1.status = bill.status;
        bill1.createTime = bill.createTime;
        return bill1;
    }
}
