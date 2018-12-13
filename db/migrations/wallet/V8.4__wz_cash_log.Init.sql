CREATE TABLE t_wz_cash_log (
  id  BIGINT PRIMARY KEY AUTO_INCREMENT,
  log_id BIGINT COMMENT '=提现申请id',
  fail_count int(4) DEFAULT '0' COMMENT '失败次数',
  errcode VARCHAR(32)
  COMMENT '错误码',
  errmsg VARCHAR(256)
  COMMENT '错误msg'
);