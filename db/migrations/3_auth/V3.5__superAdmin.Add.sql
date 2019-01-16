
-- 添加超级管理员
ALTER TABLE t_account MODIFY COLUMN id BIGINT;

INSERT INTO t_account (id, username, nickname)
VALUES (@account_super_id, 'admin', 'Super Man');

ALTER TABLE t_account MODIFY COLUMN id BIGINT AUTO_INCREMENT;
