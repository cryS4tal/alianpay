CREATE TABLE t_user_key (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  secret_key VARCHAR(128) NOT NULL
  COMMENT '商家私钥',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_user_key`
ADD UNIQUE INDEX `u_user_id` (`user_id`) ;