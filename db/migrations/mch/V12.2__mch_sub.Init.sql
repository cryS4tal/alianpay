CREATE TABLE t_mch_sub (
  mch_id BIGINT not null COMMENT '上级 account.id',
  sub_id BIGINT NOT NULL COMMENT '下级 account.id',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);