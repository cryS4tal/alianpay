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
import com.ylli.api.pay.util.RedisUtil;
import com.ylli.api.sys.model.SysChannel;
import com.ylli.api.sys.service.ChannelService;
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
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillService {

    @Autowired
    BillMapper billMapper;

   /* @Autowired
    WzClient wzClient;*/

    @Autowired
    YfbClient yfbClient;

    @Autowired
    WalletService walletService;

    @Autowired
    RateService rateService;

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
        baseBill.tradeTime = bill.tradeType;
        baseBill.createTime = bill.createTime;
        if (admin) {
            baseBill.channel = channelService.getChannelName(bill.channelId);
        }
        baseBill.msg = bill.msg;
        baseBill.isSuccess = bill.isSuccess;
        return baseBill;
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

        /*if (code.equals("WZ")) {

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

        } else*/
        if (code.equals("YFB") || code.equals("HRJF")) {

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
                        bill.payCharge = (bill.money * rateService.getRate(bill.mchId, bill.appId)) / 10000;
                        bill.tradeTime = Timestamp.from(Instant.now());
                        billMapper.updateByPrimaryKeySelective(bill);

                        //钱包金额变动。
                        walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);
                    }
                }
                //其他情况 暂时不做处理.

            }
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
        bill.appId = rateService.getAppId(payType);
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
            bill.payCharge = (bill.money * rateService.getRate(bill.mchId, bill.appId)) / 10000;

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
            walletService.rollback(bill.mchId, bill.money, bill.payCharge, bill.payType);

            billMapper.rollback(sysOrderId);
        }
    }

    public void exportBills(List<Long> mchIds, Integer status, String mchOrderId, String sysOrderId, String payType,
                            Date tradeTime, Date startTime, Date endTime, HttpServletResponse response) {
        List<Bill> list = billMapper.getBills(mchIds, status, mchOrderId, sysOrderId, payType, tradeTime, startTime, endTime);


        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();

        Sheet sheet = sxssfWorkbook.createSheet("Sheet1");
        // 冻结最左边的两列、冻结最上面的一行
        // 即：滚动横向滚动条时，左边的第一、二列固定不动;滚动纵向滚动条时，上面的第一行固定不动。
        sheet.createFreezePane(2, 1);
        setSheet(sheet);
        // 设置并获取到需要的样式
        XSSFCellStyle xssfCellStyleHeader = getAndSetXSSFCellStyleHeader(sxssfWorkbook);
        XSSFCellStyle xssfCellStyleOne = getAndSetXSSFCellStyleOne(sxssfWorkbook);
        XSSFCellStyle xssfCellStyleTwo = getAndSetXSSFCellStyleTwo(sxssfWorkbook);
        // 创建第一行,作为header表头
        Row header = sheet.createRow(0);
        // 循环创建header单元格(根据实际情况灵活创建即可)
        for (int cellnum = 0; cellnum < 11; cellnum++) {
            Cell cell = header.createCell(cellnum);
            cell.setCellStyle(xssfCellStyleHeader);
            // 判断单元格
            if (cellnum == 0) {
                cell.setCellValue("id");
            } else if (cellnum == 1) {
                cell.setCellValue("商户号");
            } else if (cellnum == 2) {
                cell.setCellValue("系统订单号");
            } else if (cellnum == 3) {
                cell.setCellValue("商户订单号");
            } else if (cellnum == 4) {
                cell.setCellValue("上游订单号");
            } else if (cellnum == 5) {
                cell.setCellValue("是否手动补单");
            } else if (cellnum == 6) {
                cell.setCellValue("通道");
            } else if (cellnum == 7) {
                cell.setCellValue("订单状态");
            } else if (cellnum == 8) {
                cell.setCellValue("手续费/分");
            } else if (cellnum == 9) {
                cell.setCellValue("金额/元");
            } else if (cellnum == 10) {
                cell.setCellValue("创建时间(北京时间)");
            }
        }
        // 遍历创建行,导出数据
        for (int rownum = 1; rownum <= list.size(); rownum++) {
            Row row = sheet.createRow(rownum);
            // 循环创建单元格
            for (int cellnum = 0; cellnum < 11; cellnum++) {
                Cell cell = row.createCell(cellnum);
                // 根据行数,设置该行内的单元格样式
                if (rownum % 2 == 1) { // 奇数
                    cell.setCellStyle(xssfCellStyleOne);
                } else { // 偶数
                    cell.setCellStyle(xssfCellStyleTwo);
                }
                // 根据单元格所属,录入相应内容
                if (cellnum == 0) {
                    cell.setCellValue((list.get(rownum - 1).id));
                } else if (cellnum == 1) {
                    cell.setCellValue(list.get(rownum - 1).mchId);
                } else if (cellnum == 2) {
                    //cell.setCellType(CellType.NUMERIC);
                    cell.setCellValue(list.get(rownum - 1).sysOrderId);
                } else if (cellnum == 3) {
                    cell.setCellValue(list.get(rownum - 1).mchOrderId);
                } else if (cellnum == 4) {
                    cell.setCellValue(list.get(rownum - 1).superOrderId);
                } else if (cellnum == 5) {
                    cell.setCellValue(Optional.ofNullable(list.get(rownum - 1).superOrderId).map(i ->
                            i.startsWith("unknown") ? "是" : "否").orElse(""));
                } else if (cellnum == 6) {
                    cell.setCellValue(SysChannel.getName(list.get(rownum - 1).channelId));
                } else if (cellnum == 7) {
                    cell.setCellValue(Bill.getStatus(list.get(rownum - 1).status));
                } else if (cellnum == 8) {
                    cell.setCellValue(list.get(rownum - 1).payCharge);
                } else if (cellnum == 9) {
                    cell.setCellValue(list.get(rownum - 1).msg);
                } else if (cellnum == 10) {
                    cell.setCellValue(convertZ8(list.get(rownum - 1).createTime));
                }
            }
        }

        OutputStream output = null;
        try {
            output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=orders.xlsx");
            response.setContentType("application/msexcel");
            sxssfWorkbook.write(output);
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

    private void setSheet(Sheet sheet) {
        // 设置各列宽度(单位为:字符宽度的1/256)
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 3000);
        sheet.setColumnWidth(2, 8000);
        sheet.setColumnWidth(3, 8000);
        sheet.setColumnWidth(4, 8000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 3000);
        sheet.setColumnWidth(7, 3000);
        sheet.setColumnWidth(8, 3000);
        sheet.setColumnWidth(9, 3000);
        sheet.setColumnWidth(10, 8000);
    }

    /**
     * 获取并设置header样式
     */
    private XSSFCellStyle getAndSetXSSFCellStyleHeader(SXSSFWorkbook sxssfWorkbook) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) sxssfWorkbook.createCellStyle();
        Font font = sxssfWorkbook.createFont();
        // 字体大小
        font.setFontHeightInPoints((short) 14);
        // 字体粗细
        font.setBoldweight((short) 20);
        // 将字体应用到样式上面
        xssfCellStyle.setFont(font);
        // 是否自动换行
        xssfCellStyle.setWrapText(false);
        // 水平居中
        xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        xssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return xssfCellStyle;
    }

    /**
     * 获取并设置样式一
     */
    private XSSFCellStyle getAndSetXSSFCellStyleOne(SXSSFWorkbook sxssfWorkbook) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) sxssfWorkbook.createCellStyle();
        XSSFDataFormat format = (XSSFDataFormat) sxssfWorkbook.createDataFormat();
        // 是否自动换行
        xssfCellStyle.setWrapText(false);
        // 水平居中
        xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        xssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 前景颜色
        xssfCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        xssfCellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        // 边框
        xssfCellStyle.setBorderBottom(BorderStyle.THIN);
        xssfCellStyle.setBorderRight(BorderStyle.THIN);
        xssfCellStyle.setBorderTop(BorderStyle.THIN);
        xssfCellStyle.setBorderLeft(BorderStyle.THIN);
        xssfCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        // 防止数字过长,excel导出后,显示为科学计数法,如:防止8615192053888被显示为8.61519E+12
        xssfCellStyle.setDataFormat(format.getFormat("0"));
        return xssfCellStyle;
    }

    /**
     * 获取并设置样式二
     */
    private XSSFCellStyle getAndSetXSSFCellStyleTwo(SXSSFWorkbook sxssfWorkbook) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) sxssfWorkbook.createCellStyle();
        XSSFDataFormat format = (XSSFDataFormat) sxssfWorkbook.createDataFormat();
        // 是否自动换行
        xssfCellStyle.setWrapText(false);
        // 水平居中
        xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 边框
        xssfCellStyle.setBorderBottom(BorderStyle.THIN);
        xssfCellStyle.setBorderRight(BorderStyle.THIN);
        xssfCellStyle.setBorderTop(BorderStyle.THIN);
        xssfCellStyle.setBorderLeft(BorderStyle.THIN);
        xssfCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        xssfCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        // 垂直居中
        xssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 防止数字过长,excel导出后,显示为科学计数法,如:防止8615192053888被显示为8.61519E+12
        xssfCellStyle.setDataFormat(format.getFormat("0"));
        return xssfCellStyle;
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
