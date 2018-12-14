/* 系统角色权限重新定义 */

DROP TABLE IF EXISTS `t_role_permission`;
CREATE TABLE `t_role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`) USING BTREE
);

-- ----------------------------
-- Records of t_role_permission
-- ----------------------------
INSERT INTO `t_role_permission` VALUES ('1', '1', '31001', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_role_permission` VALUES ('2', '1', '31002', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_role_permission` VALUES ('3', '1', '31004', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_role_permission` VALUES ('4', '1001', '30001', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_role_permission` VALUES ('5', '1001', '30002', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_role_permission` VALUES ('6', '1', '91001', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_role_permission` VALUES ('7', '1', '91002', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_role_permission` VALUES ('8', '1', '91003', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_role_permission` VALUES ('9', '1', '91004', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_role_permission` VALUES ('10', '1', '81001', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_role_permission` VALUES ('11', '1', '81002', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_role_permission` VALUES ('12', '1', '41001', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_role_permission` VALUES ('13', '1', '71001', '2018-12-14 05:49:12', '2018-12-14 05:49:12');

DROP TABLE IF EXISTS `t_permission`;
CREATE TABLE `t_permission` (
  `id` bigint(20) NOT NULL,
  `name` varchar(128) NOT NULL COMMENT '权限名',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `type` tinyint(4) NOT NULL COMMENT '0-组织权限 1-系统权限',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of t_permission
-- ----------------------------
INSERT INTO `t_permission` VALUES ('30001', '角色管理', '修改角色权限、添加角色及设置角色权限', '0', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_permission` VALUES ('30002', '人员管理', '组织机构内管理人员并设置角色', '0', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_permission` VALUES ('31001', '角色管理', '修改系统角色权限、添加角色及设置角色权限', '1', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_permission` VALUES ('31002', '人员管理', '管理系统内管理人员并设置角色', '1', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_permission` VALUES ('31004', '组织机构管理', '组织架构设置、组织机构默认角色的权限设置、组织机构分组管理、设置组织机构管理员', '1', '2018-12-14 05:49:10', '2018-12-14 05:49:10');
INSERT INTO `t_permission` VALUES ('41001', '商户账单管理', '商户账单管理', '1', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_permission` VALUES ('71001', '数据统计', '数据统计', '1', '2018-12-14 05:49:12', '2018-12-14 05:49:12');
INSERT INTO `t_permission` VALUES ('81001', '钱包管理', '给用户充值', '1', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_permission` VALUES ('81002', '提现管理', '管理用户提现请求', '1', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_permission` VALUES ('91001', '商户提现费率管理', '设置商户提现费率', '1', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_permission` VALUES ('91002', '商户应用管理', '管理商户应用', '1', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_permission` VALUES ('91003', '商户基础信息管理', '管理商户基础信息', '1', '2018-12-14 05:49:11', '2018-12-14 05:49:11');
INSERT INTO `t_permission` VALUES ('91004', '系统用户管理', '管理系统用户', '1', '2018-12-14 05:49:11', '2018-12-14 05:49:11');


/*  加入新通道 123支付中心 ，通道定义为unknown  */
INSERT INTO `t_sys_channel` VALUES ('3', 'unknown', '123支付中心', '0', '2018-12-14 18:12:11', '2018-12-14 18:12:11');
