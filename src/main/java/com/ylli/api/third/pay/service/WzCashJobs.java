package com.ylli.api.third.pay.service;


//@Component
public class WzCashJobs {

    /*@Value("${pay.wz.fail.count}")
    public Integer failCount;

    private static Logger LOGGER = LoggerFactory.getLogger(WzCashJobs.class);

    @Autowired
    WzCashLogMapper wzCashLogMapper;

    @Autowired
    WzClient wzClient;

    @Autowired
    CashService cashService;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    *//**
     * 自动轮询提现请求（网众支付）
     *//*
    //@Scheduled(cron = "0 0/5 * * * ?")
    @Transactional
    public void autoQuery() {
        if (!isRunning.compareAndSet(false, true)) {
            LOGGER.info("auto query is running, please waiting");
            return;
        }

        Gson gson = new Gson();
        List<WzCashLog> logs = wzCashLogMapper.selectAll();

        try {
            logs.stream().forEach(item -> {
                if (item.failCount > failCount) {
                    cashService.successJobs(item.logId, false);
                    wzCashLogMapper.delete(item);
                } else {
                    try {
                        String str = wzClient.cashRes(item.logId.toString());
                        if (str == null) {
                            return;
                        }
                        WzRes wzRes = gson.fromJson(str, WzRes.class);
                        if (wzRes.code.equals("200")) {
                            cashService.successJobs(item.logId, true);
                            wzCashLogMapper.delete(item);
                        } else {
                            item.failCount = item.failCount + 1;
                            item.errcode = wzRes.code;
                            item.errmsg = wzRes.msg;
                            wzCashLogMapper.updateByPrimaryKeySelective(item);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            isRunning.set(false);
        }
    }*/
}
