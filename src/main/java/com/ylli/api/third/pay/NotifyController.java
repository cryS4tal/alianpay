package com.ylli.api.third.pay;

import com.google.gson.Gson;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.third.pay.modelVo.deprecate.GPNotify;
import com.ylli.api.third.pay.modelVo.easy.EazyNotify;
import com.ylli.api.third.pay.service.alipayhb.AliPayHBService;
import com.ylli.api.third.pay.service.cxt.CXTNotify;
import com.ylli.api.third.pay.service.cxt.CXTService;
import com.ylli.api.third.pay.service.deprecate.CTService;
import com.ylli.api.third.pay.service.deprecate.CntService;
import com.ylli.api.third.pay.service.deprecate.GPayService;
import com.ylli.api.third.pay.service.deprecate.UnknownPayService;
import com.ylli.api.third.pay.service.deprecate.WzService;
import com.ylli.api.third.pay.service.deprecate.YfbService;
import com.ylli.api.third.pay.service.eazy.EazyPayService;
import com.ylli.api.third.pay.service.hrjf.HRJFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class NotifyController {

    private static Logger LOGGER = LoggerFactory.getLogger(NotifyController.class);

    @Autowired
    CntService cntService;

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    CTService ctService;

    @Autowired
    EazyPayService eazyPayService;

    @Autowired
    GPayService gPayService;

    @Autowired
    HRJFService hrjfService;

    @Autowired
    UnknownPayService unknownPayService;

    @Autowired
    WzService wzService;

    @Autowired
    YfbService yfbService;

    @Autowired
    CXTService cxtService;

    @Autowired
    AliPayHBService aliPayHBService;

    @PostMapping("/cnt/notify")
    public String cntnotify(@RequestParam(required = false) String userId,
                            @RequestParam(required = false) String orderId,
                            @RequestParam(required = false) String userOrder,
                            @RequestParam(required = false) String number,
                            @RequestParam(required = false) String date,
                            @RequestParam(required = false) String resultCode,
                            @RequestParam(required = false) String resultMsg,
                            @RequestParam(required = false) String appID,
                            @RequestParam(required = false) String chkValue,
                            @RequestParam(required = false) String remark,
                            @RequestParam(required = false) String merPriv,
                            @RequestParam(required = false) String isPur) throws Exception {
        LOGGER.info("received cnt notify: userId = [" + userId + "] orderId = [ " + orderId + "] userOrder = ["
                + userOrder + "] isPur = [" + isPur + "] resultCode = [" + resultCode + "] resultMsg = [" + resultMsg);

        return cntService.payNotify(userId, orderId, userOrder, number, remark, merPriv, date, resultCode, resultMsg, appID, isPur, chkValue);
    }

    static class CTNotify {
        public Boolean result;
        public String resultCode;
        public String attach;
        public String totalFee;
        public String sign;
    }

    @PostMapping("/ct/notify")
    public String ctnotify(@RequestBody CTNotify notify) throws Exception {
        return ctService.paynotify(notify.result, notify.resultCode, notify.attach, notify.totalFee, notify.sign);
    }

    @PostMapping("/eazy/notify")
    public String easynotify(@RequestBody EazyNotify notify) throws Exception {
        return eazyPayService.paynotify(notify);
    }

    @PostMapping("/gpay/notify")
    public void gpayotify(@RequestBody String str) throws Exception {
        GPNotify notify = new Gson().fromJson(str, GPNotify.class);
        gPayService.paynotify(notify);
    }

    /**
     * @param orderid    商户订单号
     * @param opstate    订单结果：0支付成功 -1 请求参数无效 -2 签名错误
     * @param ovalue     订单金额
     * @param sign       MD5 签名
     * @param sysorderid 易付宝订单号
     * @param systime    易付宝订单时间
     * @param attach     备注信息
     * @param msg        订单结果说明
     * @return
     */
    @GetMapping("/hrjf/notify")
    public String hrjfnotify(@AwesomeParam(required = false) String orderid,
                             @AwesomeParam(required = false) String opstate,
                             @AwesomeParam(required = false) String ovalue,
                             @AwesomeParam(required = false) String sign,
                             @AwesomeParam(required = false) String sysorderid,
                             @AwesomeParam(required = false) String systime,
                             @AwesomeParam(required = false) String attach,
                             @AwesomeParam(required = false) String msg) throws Exception {
        return hrjfService.payNotify(orderid, opstate, ovalue, sign, sysorderid, systime, attach, msg);
    }

    @GetMapping("/unknown/notify")
    public String unknownnotify(@AwesomeParam String orderid,
                                @AwesomeParam String price,
                                @AwesomeParam String codeid,
                                @AwesomeParam String key) throws Exception {
        return unknownPayService.payNotify(orderid, price, codeid, key);
    }

    @GetMapping("/wz/notify")
    public String wznotify(@AwesomeParam Long spid,
                           @AwesomeParam String md5,
                           @AwesomeParam String oid,
                           @AwesomeParam String sporder,
                           @AwesomeParam String mz,
                           @AwesomeParam String zdy,
                           @AwesomeParam Long spuid) throws Exception {
        return wzService.payNotify(spid, md5, oid, sporder, mz, zdy, spuid);
    }


    /**
     * @param orderid    商户订单号
     * @param opstate    订单结果：0支付成功 -1 请求参数无效 -2 签名错误
     * @param ovalue     订单金额
     * @param sign       MD5 签名
     * @param sysorderid 易付宝订单号
     * @param systime    易付宝订单时间
     * @param attach     备注信息
     * @param msg        订单结果说明
     * @return
     */
    @GetMapping("/yfb/notify")
    public String yfbnotify(@AwesomeParam(required = false) String orderid,
                            @AwesomeParam(required = false) String opstate,
                            @AwesomeParam(required = false) String ovalue,
                            @AwesomeParam(required = false) String sign,
                            @AwesomeParam(required = false) String sysorderid,
                            @AwesomeParam(required = false) String systime,
                            @AwesomeParam(required = false) String attach,
                            @AwesomeParam(required = false) String msg) throws Exception {
        return yfbService.payNotify(orderid, opstate, ovalue, sign, sysorderid, systime, attach, msg);
    }

    @PostMapping("/cxt/notify")
    public String cxtnotify(@RequestBody CXTNotify notify) throws Exception {
        return cxtService.payNotify(notify);
    }

    @GetMapping("/zfbhb/notify")
    public String zfbhbnotify(@AwesomeParam String merchantId,
                              @AwesomeParam String outTradeNo,
                              @AwesomeParam String tradeNo,
                              @AwesomeParam String payTime,
                              @AwesomeParam String tranAmt,
                              @AwesomeParam String settleStatus,
                              @AwesomeParam String payStatus,
                              @AwesomeParam String msg,
                              @AwesomeParam String timeStamp,
                              @AwesomeParam String sign) throws Exception {
        return aliPayHBService.zfbhbnotify(merchantId, outTradeNo, tradeNo, payTime, tranAmt, settleStatus, payStatus, msg, timeStamp, sign);
    }
}
