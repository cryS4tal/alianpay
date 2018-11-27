CREATE TABLE t_user_base (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  user_type INTEGER  COMMENT '用户类型：1-公司，2-个人',
  merchant_no VARCHAR(64) NOT NULL COMMENT '商户号',
  name VARCHAR(128) NOT NULL COMMENT '姓名',
  identity_card VARCHAR(64) NOT NULL COMMENT '商户号',
  phone VARCHAR(32) NOT NULL COMMENT '手机',
  email VARCHAR(32) NOT NULL COMMENT '邮箱',
  images VARCHAR(1024) COMMENT '图片说明 图片id列表',
  company_name VARCHAR(256) COMMENT '公司姓名',
  address VARCHAR(512) COMMENT '开户支行',
  business_license VARCHAR(128) COMMENT '营业执照',
  legal_person VARCHAR(128) COMMENT '法人',
  legal_phone VARCHAR(32) COMMENT '法人手机',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_user_base`
ADD UNIQUE INDEX `u_user_id` (`user_id`) ;

ALTER TABLE `t_user_base`
ADD COLUMN `state`  int NULL COMMENT '审核状态：1-通过，0-不通过' AFTER `legal_phone`;
