-- 为mt_order表添加易宝支付相关字段
ALTER TABLE mt_order ADD COLUMN yeepay_transaction_id VARCHAR(64) COMMENT '易宝支付的唯一订单号';
ALTER TABLE mt_order ADD COLUMN yeepay_status VARCHAR(32) COMMENT '易宝支付状态，如PENDING、SUCCESS、FAIL';
ALTER TABLE mt_order ADD COLUMN payment_gateway VARCHAR(32) COMMENT '支付网关信息，值为YEEPAY';

-- 创建易宝支付通知日志表
CREATE TABLE mt_yeepay_notification_log (
  ID int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  ORDER_ID varchar(64) DEFAULT NULL COMMENT '关联的订单ID',
  NOTIFICATION_DATA text COMMENT '完整的原始通知数据',
  PROCESS_STATUS varchar(32) DEFAULT NULL COMMENT '处理状态，如PROCESSED、FAILED',
  ERROR_MESSAGE varchar(255) DEFAULT NULL COMMENT '错误信息',
  CREATE_TIME datetime DEFAULT NULL COMMENT '通知接收时间',
  UPDATE_TIME datetime DEFAULT NULL COMMENT '处理更新时间',
  PRIMARY KEY (ID),
  KEY idx_order_id (ORDER_ID),
  KEY idx_process_status (PROCESS_STATUS),
  KEY idx_create_time (CREATE_TIME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='易宝支付通知日志表';