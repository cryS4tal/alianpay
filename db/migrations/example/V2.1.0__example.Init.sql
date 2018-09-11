/* 示例 */
CREATE TABLE t_example (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL
  COMMENT '名称',
  enabled CHAR(1) NOT NULL
  COMMENT '启用状态',
  images VARCHAR(128)
  COMMENT 'int数组',
  image_urls VARCHAR(256)
  COMMENT 'string数组',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

