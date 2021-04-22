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



CREATE TABLE `spark_db`.`seller`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL DEFAULT '' COMMENT '名称',
  `created_at` datetime(0) NOT NULL DEFAULT '2020-01-01 00:00:00' COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT '2020-01-01 00:00:00' COMMENT '更新时间',
  `remark_score` decimal(2, 1) NOT NULL DEFAULT 0 COMMENT '排序权重分数',
  `disabled_flag` int(0) NOT NULL DEFAULT 0 COMMENT '商户状态：1-生效 0-失效',
  PRIMARY KEY (`id`)
);


CREATE TABLE `spark_db`.`category`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(0) NOT NULL DEFAULT '2020-01-01 00:00:00' COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT '2020-01-01 00:00:00' COMMENT '更新时间',
  `name` varchar(20) NOT NULL DEFAULT '' COMMENT '名称',
  `icon_url` varchar(200) NOT NULL DEFAULT ''COMMENT '图片url',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '权重',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_unique_index`(`name`) USING BTREE
);


CREATE TABLE `spark_db`.`shop`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(0) NOT NULL DEFAULT '2020-01-01 00:00:00' COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT '2020-01-01 00:00:00' COMMENT '更新时间',
  `name` varchar(80) NOT NULL DEFAULT '' COMMENT '名称',
  `remark_score` decimal(2, 1) NOT NULL DEFAULT 0 COMMENT '排序权重分数',
  `price_per_man` int(0) NOT NULL DEFAULT 0 COMMENT '人均价格',
  `latitude` decimal(10, 6) NOT NULL DEFAULT 0 COMMENT '纬度',
  `longitude` decimal(10, 6) NOT NULL DEFAULT 0 COMMENT '经度',
  `category_id` int(0) NOT NULL DEFAULT 0 COMMENT'品类Id',
  `tags` varchar(2000) NOT NULL DEFAULT '' COMMENT '标签',
  `start_time` varchar(200) NOT NULL DEFAULT ''COMMENT '开业时间',
  `end_time` varchar(200) NOT NULL DEFAULT ''COMMENT '关门时间',
  `address` varchar(200) NOT NULL DEFAULT '' COMMENT '地址',
  `seller_id` int(0) NOT NULL DEFAULT 0 COMMENT '商户Id',
  `icon_url` varchar(100) NOT NULL DEFAULT '' COMMENT '门店图标url',
  PRIMARY KEY (`id`)
);

CREATE TABLE `recommend` (
  `id` int(11) NOT NULL,
  `recommend` varchar(1000) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;