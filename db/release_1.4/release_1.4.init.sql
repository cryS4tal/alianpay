/* CNT法币 */
insert into t_sys_channel (code,name,state) values("CNT","CNT支付",1);

insert into t_sys_channel (code,name,state) values("CT","畅通支付",1);

insert into t_sys_channel (code,name,state) values("GP","GPay支付",1);

ALTER TABLE `t_bill`
ADD COLUMN `reserve_work`  varchar(512) NULL COMMENT '系统保留字' AFTER `reserve`;