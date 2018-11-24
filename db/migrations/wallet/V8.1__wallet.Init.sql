/**
 * 先锋银行代码表.
 */
CREATE TABLE t_wallet (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  total_money INTEGER  DEFAULT 0 COMMENT '总金额',
  avaliable_money INTEGER  DEFAULT 0 COMMENT '可用金额',
  abnormal_money INTEGER  DEFAULT 0 COMMENT '异常金额',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_wallet`
ADD UNIQUE INDEX `u_user_id` (`user_id`) ;