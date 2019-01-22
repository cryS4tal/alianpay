/* CNT法币 */
insert into t_sys_channel (code,name,state) values("CNT","CNT支付",1);

insert into t_sys_channel (code,name,state) values("CT","畅通支付",1);

insert into t_sys_channel (code,name,state) values("GP","GPay支付",1);

ALTER TABLE `t_bill`
ADD COLUMN `reserve_work`  varchar(512) NULL COMMENT '系统保留字' AFTER `reserve`;

CREATE TABLE t_mch_bank_pay_rate (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  rate INTEGER  COMMENT '商户费率',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_mch_app` (`mch_id`)
);

insert into t_sys_channel (code,name,state) values("EAZY","eazy支付",1);

ALTER TABLE `t_bank_pay_order`
ADD COLUMN `msg`  varchar(256) NULL AFTER `status`;

insert into t_sys_channel (code,name,state) values("QrCode","BNT",1);

ALTER TABLE `t_bill`
ADD COLUMN `qr_owner`  bigint NULL AFTER `msg`;

ALTER TABLE `t_qr_code`
ADD COLUMN `enable`  tinyint(4) NULL DEFAULT 1 AFTER `uid`;

ALTER TABLE `t_qr_code`
ADD COLUMN `code_name`  varchar(64) NULL AFTER `auth_id`;


/* 待执行 */
ALTER TABLE `t_user_base`
ADD COLUMN `is_agency`  tinyint(4) NULL DEFAULT 0 AFTER `other_images`;

ALTER TABLE `t_sys_app`
ADD COLUMN `code`  varchar(32) NULL AFTER `app_name`;

CREATE TABLE t_mch_sub (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  sub_id BIGINT NOT NULL COMMENT '子账户id',
  type INTEGER NOT NULL COMMENT '代理商类型：1-支付，2-代付',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `t_mch_sub` (`mch_id`,`sub_id`,`type`)
);

UPDATE t_sys_app SET app_name = '支付宝',`code` = 'alipay' WHERE id = 1;
UPDATE t_sys_app SET app_name = '微信',`code` = 'wx' WHERE id = 2;
DELETE FROM t_sys_app WHERE id >=3;

ALTER TABLE `t_sys_app`
ADD UNIQUE INDEX `u_code` (`code`) ;

UPDATE t_bill SET app_id = 1 WHERE app_id = 3;
UPDATE t_bill SET app_id = 2 WHERE app_id = 4;

DELETE FROM t_user_app WHERE app_id > 2;