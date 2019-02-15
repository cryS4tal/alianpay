package com.ylli.api.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.mapper.MchAgencyMapper;
import com.ylli.api.mch.model.MchAgency;
import static com.ylli.api.mch.service.MchAgencyService.bankPay;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.Config;
import com.ylli.api.pay.mapper.BankPayOrderMapper;
import com.ylli.api.pay.mapper.MchBankPayRateMapper;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.MchBankPayRate;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.OrderQueryRes;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.model.ResponseEnum;
import com.ylli.api.pay.model.SignPayOrder;
import com.ylli.api.pay.util.ExcelUtil;
import com.ylli.api.pay.util.RedisUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.sys.mapper.BankPaymentMapper;
import com.ylli.api.sys.model.BankPayment;
import com.ylli.api.third.pay.service.pingan.PingAnService;
import com.ylli.api.third.pay.service.xianfen.XianFenService;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.service.WalletService;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankPayService {

    @Value("${bank.pay.min}")
    public Integer bankPayMin;

    @Value("${bank.pay.max}")
    public Integer bankPayMax;

    //手续费暂时设置定值
    @Value("${bank.pay.charge}")
    public Integer bankPayCharge;

    @Autowired
    BankPayOrderMapper bankPayOrderMapper;

    @Autowired
    BankPaymentMapper bankPaymentMapper;

    @Autowired
    MchBankPayRateMapper mchBankPayRateMapper;

    @Autowired
    MchAgencyMapper mchAgencyMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    PingAnService pingAnService;

    @Autowired
    XianFenService xianFenService;

    @Autowired
    WalletService walletService;

    @Transactional
    public Object createOrder(BankPayOrder bankPayOrder) throws Exception {
        //参数校验
        if (bankPayOrder.mchId == null) {
            return ResponseEnum.A003("商户号为空", bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.mchOrderId)) {
            return ResponseEnum.A003("商户订单号为空", bankPayOrder);
        }
        if (mchOrderExist(bankPayOrder.mchOrderId)) {
            return ResponseEnum.A004(null, bankPayOrder);
        }
        //金额校验
        if (bankPayOrder.money == null || bankPayMin > bankPayOrder.money || bankPayMax < bankPayOrder.money) {
            return ResponseEnum.A005(String.format("%s - %s 元", bankPayMin / 100, bankPayMax / 100), bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.accNo)) {
            return ResponseEnum.A003("银行卡号为空", bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.accName)) {
            return ResponseEnum.A003("姓名为空", bankPayOrder);
        }
        //代付类型转换 & 校验
        if (bankPayOrder.payType == null) {
            bankPayOrder.payType = BankPayOrder.PAY_TYPE_PERSON;
        }
        if (!BankPayOrder.payAllows.contains(bankPayOrder.payType)) {
            return ResponseEnum.A003("代付类型不正确", bankPayOrder);
        }
        //默认值
        if (bankPayOrder.payType == BankPayOrder.PAY_TYPE_PERSON &&
                (bankPayOrder.accType != null && !BankPayOrder.accAllows.contains(bankPayOrder.accType))) {
            return ResponseEnum.A003("账户类型不正确", bankPayOrder);
        }
        //联行号校验
        if (bankPayOrder.payType == BankPayOrder.PAY_TYPE_COMPANY && Strings.isNullOrEmpty(bankPayOrder.issuer)) {
            return ResponseEnum.A003("联行号不能为空", bankPayOrder);
        }
        //sign 校验.
        if (Strings.isNullOrEmpty(bankPayOrder.sign)) {
            return ResponseEnum.A001(null, bankPayOrder);
        }
        String secretKey = mchKeyService.getKeyById(bankPayOrder.mchId);
        if (secretKey == null) {
            return ResponseEnum.A002(null, null);
        }
        if (isSignValid(formatParams(bankPayOrder), secretKey)) {
            return ResponseEnum.A001(null, bankPayOrder);
        }
        Wallet wallet = walletService.getOwnWallet(bankPayOrder.mchId);

        //加入代付费率.
        MchBankPayRate rate = mchBankPayRateMapper.selectByMchId(bankPayOrder.mchId);
        if (rate == null) {
            return new Response("A013", "error：请联系管理员设置费率，商户号：" + bankPayOrder.mchId);
        }
        // 计算手续费
        Integer payCharge = bankPayOrder.money * rate.rate / 10000 + bankPayCharge;
        /*if (bankPayOrder.money < bankPayLimit) {
            payCharge = payCharge + bankPayAdd;
        }*/
        if (wallet.reservoir < (bankPayOrder.money + payCharge)) {
            return new Response("A012", "代付余额不足");
        }

        //临时使用bankPayment 开启关闭，代付通道。优先先锋
        BankPayment payment = new BankPayment();
        payment.state = Boolean.TRUE;
        List<BankPayment> list = bankPaymentMapper.select(payment);

        String code = null;
        if (list.size() == 0) {
            return new Response("A013", "暂无可用代付通道，请联系管理员");
        } else {
            if (list.size() == 1) {
                code = list.get(0).code;
            } else {
                //临时逻辑。目前系统只支持 平安 / 先锋
                code = "xianFen";
            }
        }

        //代付通道选择（系统统一切换还是可以按商户单独分配）
        if ("pingAn".equals(code)) {
            //TODO 代付订单系统 , 1 - 平安，2先锋 ，
            //平安
            bankPayOrder = insertOrder(bankPayOrder, 1L, rate.rate == 0 ? BankPayOrder.FIX : BankPayOrder.FLOAT, payCharge);

            return pingAnService.createPingAnOrder(bankPayOrder.sysOrderId, bankPayOrder.accNo, bankPayOrder.accName,
                    bankPayOrder.bankName, bankPayOrder.mobile, bankPayOrder.money, secretKey, bankPayOrder.mchOrderId,
                    bankPayOrder.chargeMoney, bankPayOrder.mchId);

        } else if ("xianFen".equals(code)) {
            //先锋
            bankPayOrder = insertOrder(bankPayOrder, 2L, rate.rate == 0 ? BankPayOrder.FIX : BankPayOrder.FLOAT, payCharge);

            return xianFenService.createXianFenOrder(bankPayOrder.sysOrderId, bankPayOrder.money, bankPayOrder.accNo,
                    bankPayOrder.accName, bankPayOrder.mobile, bankPayOrder.payType, bankPayOrder.accType,
                    bankPayOrder.mchId, secretKey, bankPayOrder.mchOrderId, bankPayOrder.chargeMoney);
        } else {
            //temp code.
            return new Response("A013", "暂无可用代付通道，请联系管理员");
        }
    }

    public SignPayOrder formatParams(BankPayOrder bankPayOrder) {
        return modelMapper.map(bankPayOrder, SignPayOrder.class);
    }

    private boolean mchOrderExist(String mchOrderId) {
        BankPayOrder payOrder = new BankPayOrder();
        payOrder.mchOrderId = mchOrderId;
        return bankPayOrderMapper.selectOne(payOrder) != null;
    }

    public boolean isSignValid(SignPayOrder order, String secretKey) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(order);
        return !SignUtil.generateSignature(map, secretKey).equals(order.sign.toUpperCase());
    }

    /**
     * 控制商户代付入参.记录系统代付信息
     */
    public BankPayOrder insertOrder(BankPayOrder bankPayOrder, Long bankPaymentId, Integer chargeType, Integer chargeMoney) {
        bankPayOrder.id = null;
        bankPayOrder.sysOrderId = redisUtil.generateSysOrderId20();
        bankPayOrder.superOrderId = null;
        bankPayOrder.bankPaymentId = bankPaymentId;
        bankPayOrder.chargeType = chargeType;
        bankPayOrder.chargeMoney = chargeMoney;
        bankPayOrder.isSuccess = null;
        bankPayOrder.status = BankPayOrder.NEW;

        bankPayOrderMapper.insertSelective(bankPayOrder);
        return bankPayOrder;
    }

    public Object orderQuery(OrderQueryReq orderQuery) throws Exception {
        String key = mchKeyService.getKeyById(orderQuery.mchId);
        Map<String, String> map = SignUtil.objectToMap(orderQuery);
        String sign = SignUtil.generateSignature(map, key);
        if (sign.equals(orderQuery.sign.toUpperCase())) {

            BankPayOrder order = bankPayOrderMapper.selectByMchOrderId(orderQuery.mchOrderId);
            if (order == null) {
                return ResponseEnum.A006(null, null);
            }
            //直接返回订单信息
            OrderQueryRes res = new OrderQueryRes();
            res.code = "A000";
            res.message = "成功";
            res.money = order.money;
            res.mchOrderId = order.mchOrderId;
            res.sysOrderId = order.sysOrderId;
            res.status = BankPayOrder.statusToString(order.status);
            if (order.tradeTime != null) {
                res.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.tradeTime);
            }

            Map<String, String> map1 = SignUtil.objectToMap(res);
            res.sign = SignUtil.generateSignature(map1, key);
            return res;
        }
        return ResponseEnum.A001(null, null);
    }

    public DataList<BankPayOrder> getOrders(List<Long> mchIds, Integer status, String mchOrderId, String sysOrderId, String accName, Integer payType, Date tradeTime, Date startTime, Date endTime, int offset, int limit) {

        PageHelper.offsetPage(offset, limit);
        Page<BankPayOrder> page = (Page<BankPayOrder>) bankPayOrderMapper.getOrders(mchIds, status, mchOrderId, sysOrderId, accName, payType, tradeTime, startTime, endTime);
        DataList<BankPayOrder> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    /**
     * 设置代付费率.
     *
     * @param mchId
     * @param rate
     */
    @Transactional
    public void setBankPayRate(Long mchId, Integer rate) {
        if (mchId == null) {
            throw new AwesomeException(Config.ERROR_MCH_NOT_FOUND);
        }
        if (rate == null) {
            throw new AwesomeException(Config.ERROR_RATE_NOT_NULL);
        }
        MchBankPayRate bankPayRate = mchBankPayRateMapper.selectByMchId(mchId);
        if (bankPayRate == null) {
            //insert
            bankPayRate = new MchBankPayRate();
            bankPayRate.mchId = mchId;
            bankPayRate.rate = rate;
            mchBankPayRateMapper.insertSelective(bankPayRate);
        } else {
            // update.
            bankPayRate.rate = rate;
            mchBankPayRateMapper.updateByPrimaryKeySelective(bankPayRate);
        }

        /**
         * 是否是代理商. yes 更新所有 mch_agency 费率差
         *
         * 是否是子账户. yes 更新所有 mch_agency 费率差
         */
        MchAgency agency = new MchAgency();
        agency.mchId = mchId;
        agency.type = bankPay;
        List<MchAgency> isAgency = mchAgencyMapper.select(agency);
        if (isAgency.size() > 0) {
            isAgency.stream().forEach(item -> {

                //获得子账户代付费率
                MchBankPayRate subRate = mchBankPayRateMapper.selectByMchId(item.subId);
                //费率差 = 子账户 -代理商
                item.bankRate = subRate.rate - rate;
                if (item.bankRate < 0) {
                    throw new AwesomeException(Config.ERROR_RATE.format(new StringBuffer("当前代理商")
                            .append(mchId).append("代付费率：")
                            .append(String.format("%.2f", (rate / 100.0))).append("%")
                            .append("大于子账户").append(item.subId)
                            .append("代付费率")
                            .append(String.format("%.2f", (subRate.rate / 100.0))).append("%")
                            .toString()
                    ));
                }
                mchAgencyMapper.updateByPrimaryKeySelective(item);
            });
        }

        MchAgency isSub = new MchAgency();
        isSub.subId = mchId;
        isSub.type = bankPay;
        isSub = mchAgencyMapper.selectOne(isSub);
        if (isSub != null) {
            //是子账户，获得代理商代付费率
            MchBankPayRate supRate = mchBankPayRateMapper.selectByMchId(isSub.mchId);

            //更新当前记录的费率差
            isSub.bankRate = rate - supRate.rate;
            if (isSub.bankRate < 0) {
                throw new AwesomeException(Config.ERROR_RATE.format(new StringBuffer("当前子账户")
                        .append(mchId).append("代付费率：")
                        .append(String.format("%.2f", (rate / 100.0))).append("%")
                        .append("小于代理商").append(isSub.mchId)
                        .append("代付费率")
                        .append(String.format("%.2f", (supRate.rate / 100.0))).append("%")
                        .toString()
                ));
            }
            mchAgencyMapper.updateByPrimaryKeySelective(isSub);
        }
    }

    public MchBankPayRate getBankPayRate(Long mchId) {
        return mchBankPayRateMapper.selectByMchId(mchId);
    }

    public void exportOrders(List<Long> mchIds, Integer status, String mchOrderId, String sysOrderId, String accName,
                             Integer payType, Date tradeTime, Date startTime, Date endTime, HttpServletResponse response) {
        List<BankPayOrder> list = bankPayOrderMapper.getOrders(mchIds, status, mchOrderId, sysOrderId, accName, payType, tradeTime, startTime, endTime);

        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();

        Sheet sheet = sxssfWorkbook.createSheet("Sheet1");
        // 冻结最左边的两列、冻结最上面的一行
        // 即：滚动横向滚动条时，左边的第一、二列固定不动;滚动纵向滚动条时，上面的第一行固定不动。
        sheet.createFreezePane(2, 1);
        ExcelUtil.setSheet1(sheet);
        // 设置并获取到需要的样式
        XSSFCellStyle xssfCellStyleHeader = ExcelUtil.getAndSetXSSFCellStyleHeader(sxssfWorkbook);
        XSSFCellStyle xssfCellStyleOne = ExcelUtil.getAndSetXSSFCellStyleOne(sxssfWorkbook);
        XSSFCellStyle xssfCellStyleTwo = ExcelUtil.getAndSetXSSFCellStyleTwo(sxssfWorkbook);
        // 创建第一行,作为header表头
        Row header = sheet.createRow(0);
        // 循环创建header单元格(根据实际情况灵活创建即可)
        for (int cellnum = 0; cellnum < 13; cellnum++) {
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
                cell.setCellValue("通道");
            } else if (cellnum == 6) {
                cell.setCellValue("收款人");
            } else if (cellnum == 7) {
                cell.setCellValue("收款账户");
            } else if (cellnum == 8) {
                cell.setCellValue("金额/元");
            } else if (cellnum == 9) {
                cell.setCellValue("结算类型");
            } else if (cellnum == 10) {
                cell.setCellValue("手续费/元");
            } else if (cellnum == 11) {
                cell.setCellValue("状态");
            } else if (cellnum == 12) {
                cell.setCellValue("创建时间(北京时间)");
            }
        }
        // 遍历创建行,导出数据
        for (int rownum = 1; rownum <= list.size(); rownum++) {
            Row row = sheet.createRow(rownum);
            // 循环创建单元格
            for (int cellnum = 0; cellnum < 13; cellnum++) {
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
                    cell.setCellValue(list.get(rownum - 1).bankPaymentId == 1 ? "平安" : "先锋");
                } else if (cellnum == 6) {
                    cell.setCellValue(list.get(rownum - 1).accName);
                } else if (cellnum == 7) {
                    cell.setCellValue(list.get(rownum - 1).accNo);
                } else if (cellnum == 8) {
                    cell.setCellValue(String.format("%.2f", (list.get(rownum - 1).money / 100.0)));
                } else if (cellnum == 9) {
                    cell.setCellValue(list.get(rownum - 1).chargeType == BankPayOrder.PAY_TYPE_PERSON ? "对私" : "对公");
                } else if (cellnum == 10) {
                    cell.setCellValue(String.format("%.2f", (list.get(rownum - 1).chargeMoney / 100.0)));
                } else if (cellnum == 11) {
                    cell.setCellValue(BankPayOrder.statusFormat(list.get(rownum - 1).status));
                } else if (cellnum == 12) {
                    cell.setCellValue(ExcelUtil.convertZ8(list.get(rownum - 1).createTime));
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
}
