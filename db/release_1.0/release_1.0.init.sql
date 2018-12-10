/* 修改 t_account_password to t_password */
ALTER TABLE t_account_password RENAME TO t_password;

DROP TABLE IF EXISTS `t_user_base`;
CREATE TABLE t_user_base (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '商户id',
  mch_name VARCHAR(256) COMMENT '商户名称',
  nick_name VARCHAR(128) COMMENT '商户简称',
  email VARCHAR(64) COMMENT '联系人邮箱',
  link_name VARCHAR(64) COMMENT '联系人',
  link_phone VARCHAR(32) COMMENT '联系人手机',
  legal_name VARCHAR(64) COMMENT '法人',
  legal_phone VARCHAR(32) COMMENT '法人手机',
  org_code VARCHAR(128) COMMENT '组织机构代码',
  business_license VARCHAR(128) COMMENT '营业执照',
  state INTEGER DEFAULT 0 COMMENT '审核状态：0-new,1-pass,2-fail',
	card_images VARCHAR(1024) COMMENT '身份证照片',
  license_images VARCHAR(1024) COMMENT '营业执照照片',
  other_images VARCHAR(1024) COMMENT '其他照片',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_mch_id` (`mch_id`),
  UNIQUE KEY `u_org_code` (`org_code`),
  UNIQUE KEY `u_business_license` (`business_license`),
  KEY `n_state` (`state`)
);

DROP TABLE IF EXISTS `t_sys_app`;
CREATE TABLE t_sys_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
  rate INTEGER  COMMENT '最低费率',
  status TINYINT NOT NULL COMMENT '0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

DROP TABLE IF EXISTS `t_user_app`;
CREATE TABLE t_user_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  app_id BIGINT NOT NULL COMMENT '应用id',
  rate INTEGER  COMMENT '商户费率',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_mch_app` (`mch_id`,`app_id`)
);