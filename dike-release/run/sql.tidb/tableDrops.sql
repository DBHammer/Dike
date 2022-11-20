DROP TABLE IF EXISTS table_config;

DROP TABLE IF EXISTS table_new_order;

DROP TABLE IF EXISTS table_order_line;

DROP TABLE IF EXISTS table_oorder;

DROP TABLE IF EXISTS table_history;

DROP TABLE IF EXISTS table_customer;

DROP TABLE IF EXISTS table_stock;

ALTER TABLE
    table_item nocache;

DROP TABLE IF EXISTS table_item;

DROP TABLE IF EXISTS table_district;

DROP TABLE IF EXISTS table_warehouse;

DROP placement POLICY IF EXISTS tikv0;

DROP placement POLICY IF EXISTS tikv1;

DROP placement POLICY IF EXISTS tikv2;

DROP placement POLICY IF EXISTS tikv3;

DROP placement POLICY IF EXISTS tikv4;

DROP placement POLICY IF EXISTS tikv5;

DROP placement POLICY IF EXISTS tikv6;

DROP placement POLICY IF EXISTS tikv7;

DROP placement POLICY IF EXISTS tikv8;

DROP placement POLICY IF EXISTS tikv9;

DROP placement POLICY IF EXISTS tikv10;

DROP placement POLICY IF EXISTS tikv11;

DROP placement POLICY IF EXISTS tikv12;

DROP placement POLICY IF EXISTS tikv13;

DROP placement POLICY IF EXISTS tikv14;

DROP placement POLICY IF EXISTS tikv15;

DROP placement POLICY IF EXISTS broadcast;