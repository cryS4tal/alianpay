
/* 代付池 */
ALTER TABLE `t_wallet`
ADD COLUMN `reservoir`  INTEGER DEFAULT 0 AFTER bonus;