-- notice 通知表
CREATE  TABLE t_notice (
  id  BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(64) NOT NULL
  COMMENT '通知类型 由使用方决定',
  out_id BIGINT(20) NOT NULL
  COMMENT '外部id',
  owner_id BIGINT(20) NOT NULL,
  title  VARCHAR(1024) NOT NULL
  COMMENT '标题',
  description VARCHAR(2048) NOT NULL
  COMMENT '内容',
  extras VARCHAR(2048) DEFAULT NULL
  COMMENT '扩展json',
  state INT NOT NULL DEFAULT 0,
  create_time  datetime  NOT NULL DEFAULT now(),
  modify_time  datetime  NOT NULL DEFAULT now()
);




