-- 添加名片正反面字段
ALTER TABLE t_customer ADD COLUMN business_card_front VARCHAR(500) COMMENT '名片正面图片路径' AFTER whatsapp_qrcode;
ALTER TABLE t_customer ADD COLUMN business_card_back VARCHAR(500) COMMENT '名片背面图片路径' AFTER business_card_front;
