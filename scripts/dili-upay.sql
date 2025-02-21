USE dili_upay;

-- --------------------------------------------------------------------
-- 系统ID生成器数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_sequence_key`;
CREATE TABLE `upay_sequence_key` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `key` VARCHAR(40) NOT NULL COMMENT 'KEY标识',
    `name` VARCHAR(80) NOT NULL COMMENT 'KEY名称',
    `value` BIGINT NOT NULl COMMENT '起始值',
    `step` TINYINT UNSIGNED NOT NULL COMMENT '步长',
    `pattern` VARCHAR(60) COMMENT 'ID格式',
    `expired_on` DATE COMMENT '有效日期',
    `version` BIGINT NOT NULL COMMENT '数据版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sequence_key_key` (`key`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 字典配置表
-- 分组(group_code)管理参数配置，系统参数不允许用户编辑修改
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_data_dictionary`;
CREATE TABLE `upay_data_dictionary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_code` VARCHAR(40) NOT NULL COMMENT '组编码',
  `code` VARCHAR(40) NOT NULL COMMENT '参数编码',
  `name` VARCHAR(60) COMMENT '参数名称',
  `value` VARCHAR(20) NOT NULL COMMENT '参数值',
  `description` VARCHAR(200) COMMENT '备注',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_dictionary_code` (`code`, `group_code`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 商户表
-- 说明：商户表用于维护接入支付的商户，提供商户各专项资金账号管理
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_merchant`;
CREATE TABLE `upay_merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `name` VARCHAR(60) NOT NULL COMMENT '商户名称',
  `parent_id` BIGINT NOT NULL COMMENT '父商户ID',
  `profit_account` BIGINT NOT NULL COMMENT '收益账户',
  `vouch_account` BIGINT NOT NULL COMMENT '担保账户',
  `pledge_account` BIGINT NOT NULL COMMENT '押金账户',
  `param` JSON COMMENT '参数配置',
  `address` VARCHAR(128) COMMENT '商户地址',
  `linkman` VARCHAR(40) COMMENT '联系人',
  `telephone` VARCHAR(20) COMMENT '电话号码',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '商户状态',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_mchId` (`mch_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 商户应用表
-- 说明：商户应用表用于维护商户接入支付的各个应用，控制应用接入权限
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_application`;
CREATE TABLE `upay_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_id` BIGINT NOT NULL COMMENT '应用模块ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `name` VARCHAR(80) NOT NULL COMMENT '应用名称',
  `token` VARCHAR(40) COMMENT '授权Token',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_appId` (`app_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 资金账户表
-- 说明：账号类型分为个人、企业和商户，个人账户和企业账户针对于市场客户；
-- 个人、企业账户业务用途有交易账户，缴费账户，预存款账户；
-- 商户账户为市场特殊账户，业务用途包括：收益资金账户、担保资金账户和押金资金账户等；
-- 资金账号分主资金账号和子资金账号，通过parent_id标识，子资金账号暂时无任何业务用途；
-- parent_id=0为主账号，且登录账号记录资金账号所属的园区卡号。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_user_account`;
CREATE TABLE `upay_user_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `parent_id` BIGINT NOT NULL COMMENT '父账号ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '账号类型',
  `use_for` TINYINT UNSIGNED NOT NULL COMMENT '业务用途',
  `name` VARCHAR(40) NOT NULL COMMENT '用户名',
  `gender` TINYINT UNSIGNED COMMENT '性别',
  `telephone` VARCHAR(20) NOT NULL COMMENT '电话号码',
  `email` VARCHAR(40) COMMENT '邮箱地址',
  `id_type` TINYINT UNSIGNED COMMENT '证件类型',
  `id_code` VARCHAR(20) COMMENT '证件号码',
  `address` VARCHAR(128) COMMENT '联系地址',
  `password` VARCHAR(50) NOT NULL COMMENT '交易密码',
  `secret_key` VARCHAR(80) NOT NULL COMMENT '安全密钥',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '账号状态',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_account_accountId` (`account_id`) USING BTREE,
  KEY `idx_user_account_parentId` (`parent_id`) USING BTREE,
  KEY `idx_user_account_name` (`name`) USING BTREE,
  KEY `idx_user_account_telephone` (`telephone`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 账户资金表
-- 说明：资金冻结时余额不变化，账户余额包含冻结金额，便于资金流水期初余额连贯，
-- 解冻消费时扣减余额；应收金额用于中央结算时记录卖家担保交易应收总金额，
-- 此时还未进行园区-卖家的资金结算
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_fund_account`;
CREATE TABLE `upay_fund_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `balance` BIGINT NOT NULL COMMENT '账户余额-分',
  `frozen_amount` BIGINT NOT NULL COMMENT '冻结金额-分',
  `vouch_amount` BIGINT NOT NULL COMMENT '担保金额-分',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fund_account_accountId` (`account_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 账户资金流水表
-- 说明：任何一条资金流水（资金变动）都是一次交易订单或交易退款的支付行为产生；
-- 资金流水动作包含收入和支出，支出时流水金额amount为负值，否则为正值；
-- 资金类型包括账户资金、手续费和工本费等，余额balance为期初余额；
-- 资金流水的交易类型标识由哪种业务产生，包括：充值、提现、交易等；
-- 子账号用于标识主账号的资金流水为子账号交易产生，子账号无账户资金。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_fund_statement`;
CREATE TABLE `upay_fund_statement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `child_id` BIGINT COMMENT '子账号ID',
  `trade_type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
  `action` TINYINT UNSIGNED NOT NULL COMMENT '动作-收入 支出',
  `balance` BIGINT NOT NULL COMMENT '(前)余额-分',
  `amount` BIGINT NOT NULL COMMENT '金额-分(正值 负值)',
  `type` INT NOT NULL COMMENT '资金类型',
  `type_name` VARCHAR(80) COMMENT '费用描述',
  `description` VARCHAR(255) COMMENT '备注',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_fund_stmt_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_fund_stmt_accountId` (`account_id`, `created_time`) USING BTREE,
  KEY `idx_fund_stmt_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 交易订单表
-- 说明：交易订单表用于存储收款方(园区账户)信息，任何一笔交易至少有一个园区账户；
-- max_amount、amount用作处理部分退款，分别记录原始余额和退款后的金额；
-- 资金流水动作包含收入和支出，支出时流水金额amount为负值，否则为正值；
-- 资金类型包括账户资金、手续费和工本费等，余额balance为期初余额；
-- 资金流水的交易类型标识由哪种业务产生，包括：充值、提现、交易等；
-- 外部流水号记录业务系统发起支付时的业务单号，主要用于问题故障排查；
-- 账务周期用于支付系统与业务系统之间的资金对账，查询对账周期的交易明细；
-- 费用金额用于存储向收款方(园区账户)收取的费用。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_trade_order`;
CREATE TABLE `upay_trade_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `app_id` BIGINT NOT NULL COMMENT '应用ID',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
  `out_trade_no` VARCHAR(40) COMMENT '外部流水号',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `name` VARCHAR(40) COMMENT '账号名称',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `max_amount` BIGINT NOT NULL COMMENT '初始金额-分',
  `fee` BIGINT NOT NULL COMMENT '费用金额-分',
  `goods` VARCHAR(128) COMMENT '商品描述',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '交易状态',
  `description` VARCHAR(255) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_order_tradeId` (`trade_id`) USING BTREE,
  KEY `idx_trade_order_accountId` (`account_id`, `type`) USING BTREE,
  UNIQUE KEY `uk_trade_order_outTradeNo` (`out_trade_no`, `mch_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 交易支付表
-- 说明：交易支付表用于存储付款方信息，付款方包括园区账户、银行、第三方支付渠道等；
-- 数据模型理论上一条交易订单可以有多条支付记录，支付金额可以小于或等于交易订单金额；
-- 所有支付都对应一个支付渠道，即使现金；费用金额用于存储向付款方(园区账户)收取的费用；
-- 组合支付时一个交易订单对应多个支付记录，一个交易订单同一种支付渠道只能有一条记录；
-- 免密支付时需记录免密支付协议号。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_trade_payment`;
CREATE TABLE `upay_trade_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `channel_id` TINYINT UNSIGNED NOT NULL COMMENT '支付渠道',
  `pay_type` TINYINT UNSIGNED COMMENT '支付方式',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `name` VARCHAR(40) COMMENT '账号名称',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `fee` BIGINT NOT NULL COMMENT '费用金额-分',
  `protocol_id` VARCHAR(40) COMMENT '免密协议ID',
  `cycle_no` VARCHAR(20) COMMENT '账期编号',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '支付状态',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_payment_paymentId` (`payment_id`) USING BTREE,
  UNIQUE KEY `uk_trade_payment_tradeId` (`trade_id`, `channel_id`) USING BTREE,
  KEY `idx_trade_payment_accountId` (`account_id`) USING BTREE,
  KEY `idx_trade_payment_cycleNo`(`cycle_no`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 退款表
-- 说明：退款类型包括交易撤销、交易退款和交易冲正，都是对原交易记录的资金逆向操作；
-- 交易退款与交易支付相同都是对交易订单进行的资金操作，一个是支付一个是退款；
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_refund_payment`;
CREATE TABLE `upay_refund_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '退款支付ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '退款类型',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '原交易ID',
  `trade_type` TINYINT UNSIGNED NOT NULL COMMENT '原交易类型',
  `channel_id` TINYINT UNSIGNED NOT NULL COMMENT '原支付渠道',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `fee` BIGINT NOT NULL COMMENT '费用金额-分',
  `cycle_no` VARCHAR(20) COMMENT '账期编号',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '状态',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_payment_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_refund_payment_tradeId` (`trade_id`) USING BTREE,
  KEY `idx_refund_payment_cycleNo`(`cycle_no`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 支付费用表
-- 说明：支付费用表存储交易收取的费用明细，需记录在资金流水表中
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_payment_fee`;
CREATE TABLE `upay_payment_fee` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `use_for` TINYINT UNSIGNED COMMENT '费用用途',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `type` INT NOT NULL COMMENT '费用类型',
  `type_name` VARCHAR(60) COMMENT '类型说明',
  `description` VARCHAR(80) COMMENT '费用描述',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_fee_paymentId` (`payment_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 资金冻结表
-- 说明：支付时进行交易冻结，而非创建交易时冻结资金
-- 资金流水动作包含收入和支出，支出时流水金额amount为负值，否则为正值；
-- 资金类型包括账户资金、手续费和工本费等，余额balance为期初余额；
-- 资金流水的交易类型标识由哪种业务产生，包括：充值、提现、交易等。
-- 创建时间=冻结时间，修改时间=解冻时间，当交易冻结操作人信息为资金账号，否则外部传入
-- 扩展信息用于存储业务系统额外信息，包括：冻结人、解冻人等，由业务系统定义数据格式
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_frozen_order`;
CREATE TABLE `upay_frozen_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '冻结ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `child_id` BIGINT COMMENT '子账号ID',
  `name` VARCHAR(40) COMMENT '用户名',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '冻结类型-系统冻结 交易冻结',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '冻结状态-冻结 解冻',
  `extension` VARCHAR(200) COMMENT '扩展信息',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_frozen_order_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_frozen_order_accountId` (`account_id`, `type`) USING BTREE,
  KEY `idx_frozen_order_createdTime` (`created_time`, `account_id`) USING BTREE,
  KEY `idx_frozen_order_modifiedTime` (`modified_time`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 业务账单表
-- 说明：记录资金账号的业务流水，包含退款记录（退款不是交易不会产生交易订单，数据模型
-- 与交易模型也是独立的）；与收支明细不同，业务账单一笔支付交易，每个资金账号只会
-- 产生一条业务账单，且只会记录交易结果不会记录交易过程；比如：交易金额记录的是实际
-- 入账或出账金额（充值1000元，手续费4元，业务流水中交易金额为996元，费用为4元）。
-- 交易金额都是带符号的，正数表示入账，负数表示出账；费用金额始终为正数。
-- 子账号用于标识主账号的资金流水为子账号交易产生，子账号无账户资金。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_user_statement`;
CREATE TABLE `upay_user_statement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `channel_id` TINYINT UNSIGNED NOT NULL COMMENT '支付渠道',
  `account_id` BIGINT NOT NULL COMMENT '主账号ID',
  `child_id` BIGINT COMMENT '子账号ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '流水类型',
  `type_name` VARCHAR(80) NOT NULL COMMENT '流水说明',
  `amount` BIGINT NOT NULL COMMENT '交易金额-分',
  `fee` BIGINT NOT NULL COMMENT '费用-分',
  `balance` BIGINT NOT NULL COMMENT '期末余额',
  `frozen_amount` BIGINT NOT NULL COMMENT '期末冻结金额',
  `out_trade_no` VARCHAR(40) COMMENT '外部流水号',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '状态',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_statement_tradeId` (`trade_id`, `account_id`) USING BTREE,
  KEY `idx_user_statement_accountId` (`account_id`, `type`) USING BTREE,
  KEY `idx_user_statement_createdTime` (`created_time`, `account_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 担保账单表
-- 说明：记录资金账号担保金额的资金流水，担保交易及结算时"代收款"金额变化明细
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_vouch_statement`;
CREATE TABLE `upay_vouch_statement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `child_id` BIGINT COMMENT '子账号ID',
  `trade_type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
  `action` TINYINT UNSIGNED NOT NULL COMMENT '动作-收入 支出',
  `balance` BIGINT NOT NULL COMMENT '(前)余额-分',
  `amount` BIGINT NOT NULL COMMENT '金额-分(正值 负值)',
  `type` INT NOT NULL COMMENT '资金类型',
  `type_name` VARCHAR(80) COMMENT '费用描述',
  `description` VARCHAR(128) COMMENT '备注',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_vouch_stmt_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_vouch_stmt_accountId` (`account_id`, `created_time`) USING BTREE,
  KEY `idx_vouch_stmt_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 全局资金风控数据模型
-- 说明：用于对某个商户下的账户资金进行全局风险控制，目前支持充值、提现和交易三大类交易风控；
-- 全局风控采用JSON进行数据存储便于后期扩展，当账户未设置风控时，将使用商户级的全局风控设置；
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_global_permission`;
CREATE TABLE `upay_global_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `permission` INT NOT NULL COMMENT '账号权限',
  `deposit` JSON NOT NULL COMMENT '充值配置',
  `withdraw` JSON NOT NULL COMMENT '提现配置',
  `trade` JSON NOT NULL COMMENT '交易配置',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_global_permission_mchId` (`mch_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 资金风控数据模型
-- 说明：用于账户资金的风险控制，目前支持充值、提现和交易三大类交易风控；
-- 风控可细化至单笔限额、日限额、日次数和月限额等，各类风控采用JSON进行数据存储便于后期扩展；
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_user_permission`;
CREATE TABLE `upay_user_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `permission` INT NOT NULL COMMENT '账号权限',
  `deposit` VARCHAR(200) NOT NULL COMMENT '充值配置',
  `withdraw` VARCHAR(200) NOT NULL COMMENT '提现配置',
  `trade` VARCHAR(200) NOT NULL COMMENT '交易配置',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_permission_accountId` (`account_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 免密支付协议表
-- 说明：一个资金账号可以有多个不同类型的免密协议，且免密协议只能在指定金额范围内有效；
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_user_protocol`;
CREATE TABLE `upay_user_protocol` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `protocol_id` VARCHAR(40) NOT NULL COMMENT '协议ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `name` VARCHAR(40) COMMENT '用户名',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '协议类型',
  `min_amount` BIGINT NOT NULL COMMENT '最小金额-分',
  `max_amount` BIGINT NOT NULL COMMENT '最大金额-分',
  `start_on` DATETIME COMMENT '生效时间',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '协议状态',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_protocol_protocolId` (`protocol_id`) USING BTREE,
  UNIQUE KEY `uk_user_protocol_accountId` (`account_id`, `type`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 通道管理配置数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_pipeline`;
CREATE TABLE `upay_pipeline` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `pipeline_id` BIGINT NOT NULL COMMENT '通道ID',
  `channel_id` TINYINT UNSIGNED NOT NULL COMMENT '渠道', -- 建设银行、农业银行、微信、支付宝
  `type` TINYINT UNSIGNED NOT NULL COMMENT '通道类型', -- 银企直连通道、聚合支付通道、微信支付通道、支付宝通道
  `name` VARCHAR(40) NOT NULL COMMENT '通道名称',
  `uri` VARCHAR(60) NOT NULL COMMENT '通道URI',
  `param` JSON COMMENT '通道参数',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '通道状态',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY  `uk_payment_pipeline_pipelineId` (`pipeline_id`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 银企直连支付通道申请数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_bank_direct_payment`;
CREATE TABLE `upay_bank_direct_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `pipeline_id` BIGINT NOT NULL COMMENT '通道ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `name` VARCHAR(40) NOT NULL COMMENT '账号名称',
  `bank_account` VARCHAR(30) NOT NULL COMMENT '银行账户',
  `account_name` VARCHAR(40) COMMENT '银行账户名',
  `amount` BIGINT NOT NULL COMMENT '申请金额-分',
  `paid_time` DATETIME COMMENT '支付时间',
  `out_trade_no` VARCHAR(40) COMMENT '通道流水号',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '状态',
  `retry_times` INTEGER UNSIGNED NOT NULL COMMENT '重试次数',
  `notify_url` VARCHAR(128) COMMENT '业务回调地址',
  `notify_state` INTEGER UNSIGNED COMMENT '通知状态',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bank_payment_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_bank_payment_tradeId` (`trade_id`) USING BTREE,
  KEY `idx_bank_payment_accountId` (`account_id`) USING BTREE,
  KEY `idx_bank_payment_paidTime` (`paid_time`) USING BTREE,
  KEY `idx_bank_payment_outTradeNo` (`out_trade_no`) USING BTREE,
  KEY `idx_bank_payment_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 银行聚合支付通道申请数据模型 - 银行扫码支付，银行微信下单支付: 当前贵阳建行使用
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_bank_online_payment`;
CREATE TABLE `upay_bank_online_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `pipeline_id` BIGINT NOT NULL COMMENT '通道ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `name` VARCHAR(40) NOT NULL COMMENT '账号名称',
  `qr_code` VARCHAR(128) COMMENT '二维码信息',
  `goods_desc` VARCHAR(128) COMMENT '商品描述',
  `amount` BIGINT NOT NULL COMMENT '申请金额-分',
  `payer_id` VARCHAR(40) COMMENT '支付方', -- 比如：微信OpenId
  `paid_time` DATETIME COMMENT '支付时间',
  `out_trade_no` VARCHAR(40) COMMENT '通道流水号',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '状态',
  `retry_times` INTEGER UNSIGNED NOT NULL COMMENT '重试次数',
  `notify_url` VARCHAR(128) COMMENT '业务回调地址',
  `notify_state` INTEGER UNSIGNED COMMENT '通知状态',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_online_payment_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_online_payment_tradeId` (`trade_id`) USING BTREE,
  KEY `idx_online_payment_accountId` (`account_id`) USING BTREE,
  KEY `idx_online_payment_paidTime` (`paid_time`) USING BTREE,
  KEY `idx_online_payment_outTradeNo` (`out_trade_no`) USING BTREE,
  KEY `idx_online_payment_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 新增微信支付申请数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_wechat_payment`;
CREATE TABLE `upay_wechat_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '支付商户ID',
  `wx_mch_id` varchar(40) NOT NULL COMMENT '微信商户号',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `paid_type` TINYINT UNSIGNED NOT NULL COMMENT '支付方式', -- JSAPI NATIVE
  `pipeline_id` BIGINT NOT NULL COMMENT '通道ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `name` VARCHAR(40) NOT NULL COMMENT '账号名称',
  `goods` VARCHAR(128) NOT NULL COMMENT '商品描述',
  `amount` BIGINT NOT NULL COMMENT '申请金额-分',
  `prepay_id` VARCHAR(128) COMMENT '预支付ID', -- 微信预支付ID或二维码链接
  `payer_id` VARCHAR(40) COMMENT '支付方OpenId',
  `paid_time` DATETIME COMMENT '支付时间',
  `out_trade_no` VARCHAR(40) COMMENT '微信流水号',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '申请状态',
  `state_code` VARCHAR(20) COMMENT '微信支付状态',
  `notify_url` VARCHAR(128) COMMENT '业务回调链接',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `description` VARCHAR(256) COMMENT '备注',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wechat_payment_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_wechat_payment_tradeId` (`trade_id`) USING BTREE,
  KEY `idx_wechat_payment_accountId` (`account_id`) USING BTREE,
  KEY `idx_wechat_payment_paidTime` (`paid_time`, `state`) USING BTREE,
  KEY `idx_wechat_payment_outTradeNo` (`out_trade_no`) USING BTREE,
  KEY `idx_wechat_payment_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 银行CardBin基础数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_bank_card`;
CREATE TABLE `upay_bank_card` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `length` TINYINT UNSIGNED NOT NULL COMMENT '主账号长度',
  `bin_no` VARCHAR(20) NOT NULL COMMENT '卡BIN号',
  `bin_length` TINYINT UNSIGNED NOT NULL COMMENT '卡BIN号长度',
  `bank_no` VARCHAR(20) NOT NULL COMMENT '发卡机构代码',
  `bank_code` VARCHAR(10) DEFAULT NULL COMMENT '银行编码',
  `bank_name` VARCHAR(40) NOT NULL COMMENT '发卡机构名称',
  `card_code` VARCHAR(10) DEFAULT NULL COMMENT '卡种代码',
  `state_code` VARCHAR(10) DEFAULT NULL COMMENT '状态代码',
  `type_code` VARCHAR(10) DEFAULT NULL COMMENT '卡类型代码',
  `modified_date` DATE DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`),
  KEY `idx_bank_card_binNo` (`length`,`bin_no`,`bin_length`) USING BTREE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 开户行基础数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_bank_info`;
CREATE TABLE `upay_bank_info` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bank_no` VARCHAR(20) NOT NULL COMMENT '联行行号',
  `bank_code` varchar(10) DEFAULT NULL COMMENT '银行编码',
  `bank_name` VARCHAR(60) NOT NULL COMMENT '联行全称',
  `type_code` VARCHAR(10) NOT NULL COMMENT '行别代码',
  `ibank_no` VARCHAR(20) DEFAULT NULL COMMENT '参与行联行号',
  `node_code` VARCHAR(10) DEFAULT NULL COMMENT '所属节点编码',
  `urbn_code` VARCHAR(10) DEFAULT NULL COMMENT '所在城镇编码',
  PRIMARY KEY (`id`),
  KEY `idx_bank_info_bankName` (`bank_name`,`bank_no`) USING BTREE
) ENGINE=InnoDB;