/**
 * 存储表
 */
CREATE TABLE t_storage (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  path VARCHAR(1024) NOT NULL
  COMMENT '存储路径，存储目录的相对路径',
  content_type VARCHAR(128) NOT NULL
  COMMENT '文件类型',
  committer_id BIGINT NOT NULL
  COMMENT '提交者Id',
  name VARCHAR(128)
  COMMENT '名称',
  meta VARCHAR(1024)
  COMMENT '用于存储文件的额外信息 Json格式',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

