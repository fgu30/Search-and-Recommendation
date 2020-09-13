CREATE TABLE `spark_db`.`user` (
	`id` int NOT NULL AUTO_INCREMENT  COMMENT '用户id',
	`created_at` datetime NOT NULL DEFAULT '2020-01-01 00:00:00' COMMENT '创建时间',
	`updated_at` datetime NOT NULL DEFAULT '2020-01-01 00:00:00' COMMENT '更新时间',
	`telephone` varchar(40) NOT NULL DEFAULT ''  COMMENT '电话号码',
	`password` varchar(200) NOT NULL DEFAULT ''  COMMENT '密码',
	`nick_name` varchar(40) NOT NULL DEFAULT ''  COMMENT '名称',
	`gender` int NOT NULL DEFAULT 0  COMMENT '性别',
	PRIMARY KEY (`id`),
	UNIQUE `telephone_unique_index` USING BTREE (`telephone`) comment '电话号码唯一索引'
) COMMENT='用户表';