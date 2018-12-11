CREATE TABLE t_user_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  app_id BIGINT NOT NULL COMMENT '应用id',
  rate INTEGER  COMMENT '商户费率',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_mch_app` (`mch_id`,`app_id`)
);

CREATE TABLE t_sys_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
  rate INTEGER  COMMENT '最低费率',
  status TINYINT NOT NULL COMMENT '0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

INSERT INTO `t_sys_app` VALUES ('1', '支付宝H5', '300', '1', '2018-12-11 17:17:32', '2018-12-11 17:17:32');
INSERT INTO `t_sys_app` VALUES ('2', '微信H5', '300', '1', '2018-12-11 17:17:59', '2018-12-11 17:17:59');
INSERT INTO `t_sys_app` VALUES ('3', '支付宝扫码', '300', '1', '2018-12-11 17:18:29', '2018-12-11 17:18:29');
INSERT INTO `t_sys_app` VALUES ('4', '微信扫码', '300', '1', '2018-12-11 17:18:41', '2018-12-11 17:18:41');
