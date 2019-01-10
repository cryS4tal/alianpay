package com.ylli.api.wallet.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.auth.mapper.PasswordMapper;
import com.ylli.api.auth.model.Password;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.model.ResponseEnum;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.sys.service.ChannelService;
import com.ylli.api.third.pay.enums.CNTEnum;
import com.ylli.api.third.pay.model.CNTCards;
import com.ylli.api.third.pay.model.CNTResponse;
import com.ylli.api.third.pay.service.CntClient;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.model.CNTAuth;
import com.ylli.api.wallet.model.CNTCash;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.Wallet;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CNTCashService {

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    ChannelService channelService;

    @Autowired
    WalletService walletService;

    @Autowired
    PasswordMapper passwordMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    CntClient cntClient;

    @Value("${cash.charge}")
    public Integer cashCharge;

    @Value("${cash.min}")
    public Integer min;

    @Value("${cash.max}")
    public Integer max;

    @Transactional
    public Object convert(CNTAuth auth) throws Exception {
        if (auth.primaryId == auth.subId) {
            return ResponseEnum.A998("主账户不能等于子账户");
        }
        String secretKey = mchKeyService.getKeyById(auth.subId);
        if (secretKey == null) {
            return ResponseEnum.A998("请先设置密钥");
        }
        Map<String, String> map = SignUtil.objectToMap(auth);
        if (!SignUtil.generateSignature(map, secretKey).equals(auth.sign.toUpperCase())) {
            return ResponseEnum.A001(null, auth);
        }
        if (!("CNT").equals(channelService.getCurrentChannel(auth.subId).code)
                || !("CNT").equals(channelService.getCurrentChannel(auth.primaryId).code)) {
            return ResponseEnum.A998("非法的请求");
        }
        walletService.rechargeConvert(auth.primaryId, auth.subId);

        return ResponseEnum.SUCCESS();
    }

    @Transactional
    public Object cash(CNTCash cntCash) throws Exception {
        //签名校验
        String secretKey = mchKeyService.getKeyById(cntCash.mchId);
        if (secretKey == null) {
            return ResponseEnum.A998("请先设置密钥");
        }
        if (!("CNT").equals(channelService.getCurrentChannel(cntCash.mchId).code)) {
            return ResponseEnum.A998("非法的请求");
        }
        Map<String, String> map = SignUtil.objectToMap(cntCash);
        if (Strings.isNullOrEmpty(cntCash.sign) || !SignUtil.generateSignature(map, secretKey).equals(cntCash.sign.toUpperCase())) {
            return ResponseEnum.A001(null, cntCash);
        }
        Password password = passwordMapper.selectByPrimaryKey(cntCash.mchId);
        if (Strings.isNullOrEmpty(cntCash.password) || !BCrypt.checkpw(cntCash.password, password.password)) {
            return ResponseEnum.A998("密码校验失败");
        }
        //余额校验
        Wallet wallet = walletService.getOwnWallet(cntCash.mchId);

        if (cntCash.money > max || cntCash.money < min) {
            return ResponseEnum.A998(String.format("%s - %s 元", min / 100, max / 100));
        }
        if (cntCash.money > wallet.total + cashCharge) {
            return ResponseEnum.A998("当前最大提现金额为 :" + String.format("%.2f", ((wallet.recharge - cashCharge) / 100.0)));
        }

        Gson gson = new Gson();

        //获取商户绑卡列表
        String cards = cntClient.findCards(cntCash.mchId.toString());
        if (Strings.isNullOrEmpty(cards)) {
            //当前提现服务不可用。请联系管理员
            return ResponseEnum.A998("服务不可用,super response empty");
        }

        CNTCards cntCards = gson.fromJson(cards, CNTCards.class);
        if ("0000".equals(cntCards.resultCode)) {
            //删除历史银行卡
            for (int i = 0; i < cntCards.data.size(); i++) {
                String delete = cntClient.delCard(cntCards.data.get(i).id.toString());
                CNTResponse response = gson.fromJson(delete, CNTResponse.class);
                if (!"0000".equals(response.resultCode)) {
                    //提现失败：%s
                    return ResponseEnum.A998(response.resultMsg);
                }
            }
            //绑定新卡
            String add = cntClient.addCard(cntCash.mchId.toString(), cntCash.accName, cntCash.accNo, cntCash.openBank, cntCash.subBank);
            CNTResponse response = gson.fromJson(add, CNTResponse.class);
            if (!"0000".equals(response.resultCode)) {
                //提现失败：%S message
                return ResponseEnum.A998(response.resultMsg);
            }
            //提现下单
            //分转换元
            String mz = String.format("%.2f", (cntCash.money / 100.0));

            //记录日志
            CashLog log = new CashLog();
            modelMapper.map(cntCash, log);
            log.state = CashLog.NEW;
            cashLogMapper.insertSelective(log);

            walletService.pendingSuc(wallet, cntCash.money, cashCharge);

            //使用提现日志作为系统订单号。
            String cntOrder = cntClient.createCntOrder(log.id.toString(), cntCash.mchId.toString(), mz, CNTEnum.UNIONPAY.getValue(), CNTEnum.CASH.getValue());
            CNTResponse cntResponse = gson.fromJson(cntOrder, CNTResponse.class);

            if ("0000".equals(cntResponse.resultCode)) {
                // 更新日志状态为处理中
                log.state = CashLog.PROCESS;
                log.type = CashLog.CNT;
                cashLogMapper.updateByPrimaryKeySelective(log);
                //等待 cnt 回调确认状态.

                return ResponseEnum.SUCCESS();
            } else {
                //提现失败
                log.state = CashLog.FAILED;
                cashLogMapper.updateByPrimaryKeySelective(log);

                walletService.cashFail(log.mchId, log.money);
                return ResponseEnum.A998(cntResponse.resultMsg);
            }
        } else {
            //提现失败：%s
            return ResponseEnum.A998(cntCards.resultMsg);
        }
    }
}
