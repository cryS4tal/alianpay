ALTER TABLE `t_account`
ADD INDEX `n_state` (`state`) ;

CREATE TABLE t_bank_payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) COMMENT '标识',
  name VARCHAR(128) COMMENT '代付渠道',
  state TINYINT COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

INSERT INTO `t_bank_payment` VALUES ('1', 'pingAn', '平安', '1', '2018-12-11 10:29:58', '2018-12-11 10:29:58');
INSERT INTO `t_bank_payment` VALUES ('2', 'xianFen', '先锋', '0', '2018-12-11 10:30:26', '2018-12-11 10:30:26');