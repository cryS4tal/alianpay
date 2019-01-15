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