CREATE placement POLICY IF NOT EXISTS tikv0 primary_region = "tikv0" regions = "tikv0,tikv3,tikv6";
 CREATE placement POLICY IF NOT EXISTS tikv1 primary_region = "tikv1" regions = "tikv1,tikv4,tikv7";
 CREATE placement POLICY IF NOT EXISTS tikv2 primary_region = "tikv2" regions = "tikv2,tikv5,tikv8";
 CREATE placement POLICY IF NOT EXISTS tikv3 primary_region = "tikv3" regions = "tikv3,tikv6,tikv0";
 CREATE placement POLICY IF NOT EXISTS tikv4 primary_region = "tikv4" regions = "tikv4,tikv7,tikv1";
 CREATE placement POLICY IF NOT EXISTS tikv5 primary_region = "tikv5" regions = "tikv5,tikv8,tikv2";
 CREATE placement POLICY IF NOT EXISTS tikv6 primary_region = "tikv6" regions = "tikv6,tikv0,tikv3";
 CREATE placement POLICY IF NOT EXISTS tikv7 primary_region = "tikv7" regions = "tikv7,tikv1,tikv4";
 CREATE placement POLICY IF NOT EXISTS tikv8 primary_region = "tikv8" regions = "tikv8,tikv2,tikv5";

CREATE TABLE table_config (
  cfg_name VARCHAR(30) PRIMARY KEY,
  cfg_value VARCHAR(50)
);

CREATE TABLE table_warehouse (
  w_id INTEGER NOT NULL,
  w_ytd DECIMAL(12, 2),
  w_tax DECIMAL(4, 4),
  w_name VARCHAR(10),
  w_street_1 VARCHAR(20),
  w_street_2 VARCHAR(20),
  w_city VARCHAR(20),
  w_state CHAR(2),
  w_zip CHAR(9),
  CONSTRAINT pk_warehouse PRIMARY KEY (w_id)
) PARTITION BY RANGE(w_id) (
PARTITION p0 VALUES LESS THAN (21) placement POLICY = tikv0,
PARTITION p1 VALUES LESS THAN (41) placement POLICY = tikv1,
PARTITION p2 VALUES LESS THAN (61) placement POLICY = tikv2,
PARTITION p3 VALUES LESS THAN (81) placement POLICY = tikv3,
PARTITION p4 VALUES LESS THAN (101) placement POLICY = tikv4,
PARTITION p5 VALUES LESS THAN (121) placement POLICY = tikv5,
PARTITION p6 VALUES LESS THAN (141) placement POLICY = tikv6,
PARTITION p7 VALUES LESS THAN (161) placement POLICY = tikv7,
PARTITION p8 VALUES LESS THAN (181) placement POLICY = tikv8);

CREATE TABLE table_district (
  d_w_id INTEGER NOT NULL,
  d_id INTEGER NOT NULL,
  d_ytd DECIMAL(12, 2),
  d_tax DECIMAL(4, 4),
  d_next_o_id INTEGER,
  d_name VARCHAR(10),
  d_street_1 VARCHAR(20),
  d_street_2 VARCHAR(20),
  d_city VARCHAR(20),
  d_state CHAR(2),
  d_zip CHAR(9),
  CONSTRAINT pk_district PRIMARY KEY (d_w_id, d_id)
) PARTITION BY RANGE(d_w_id) (
PARTITION p0 VALUES LESS THAN (21) placement POLICY = tikv0,
PARTITION p1 VALUES LESS THAN (41) placement POLICY = tikv1,
PARTITION p2 VALUES LESS THAN (61) placement POLICY = tikv2,
PARTITION p3 VALUES LESS THAN (81) placement POLICY = tikv3,
PARTITION p4 VALUES LESS THAN (101) placement POLICY = tikv4,
PARTITION p5 VALUES LESS THAN (121) placement POLICY = tikv5,
PARTITION p6 VALUES LESS THAN (141) placement POLICY = tikv6,
PARTITION p7 VALUES LESS THAN (161) placement POLICY = tikv7,
PARTITION p8 VALUES LESS THAN (181) placement POLICY = tikv8);

CREATE TABLE table_customer (
  c_w_id INTEGER NOT NULL,
  c_d_id INTEGER NOT NULL,
  c_id INTEGER NOT NULL,
  c_discount DECIMAL(4, 4),
  c_credit CHAR(2),
  c_last VARCHAR(16),
  c_first VARCHAR(16),
  c_credit_lim DECIMAL(12, 2),
  c_balance DECIMAL(12, 2),
  c_ytd_payment DECIMAL(12, 2),
  c_payment_cnt INTEGER,
  c_delivery_cnt INTEGER,
  c_street_1 VARCHAR(20),
  c_street_2 VARCHAR(20),
  c_city VARCHAR(20),
  c_state CHAR(2),
  c_zip CHAR(9),
  c_phone CHAR(16),
  c_since TIMESTAMP,
  c_middle CHAR(2),
  c_data VARCHAR(500),
  CONSTRAINT pk_customer PRIMARY KEY (c_w_id, c_d_id, c_id),
  CONSTRAINT KEY table_customer_idx1(c_w_id, c_d_id, c_last, c_first)
) PARTITION BY RANGE(c_w_id) (
PARTITION p0 VALUES LESS THAN (21) placement POLICY = tikv0,
PARTITION p1 VALUES LESS THAN (41) placement POLICY = tikv1,
PARTITION p2 VALUES LESS THAN (61) placement POLICY = tikv2,
PARTITION p3 VALUES LESS THAN (81) placement POLICY = tikv3,
PARTITION p4 VALUES LESS THAN (101) placement POLICY = tikv4,
PARTITION p5 VALUES LESS THAN (121) placement POLICY = tikv5,
PARTITION p6 VALUES LESS THAN (141) placement POLICY = tikv6,
PARTITION p7 VALUES LESS THAN (161) placement POLICY = tikv7,
PARTITION p8 VALUES LESS THAN (181) placement POLICY = tikv8);

CREATE TABLE table_history (
  hist_id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  h_c_id INTEGER,
  h_c_d_id INTEGER,
  h_c_w_id INTEGER,
  h_d_id INTEGER,
  h_w_id INTEGER,
  h_date TIMESTAMP,
  h_amount DECIMAL(6, 2),
  h_data VARCHAR(24)
);

CREATE TABLE table_new_order (
  no_w_id INTEGER NOT NULL,
  no_d_id INTEGER NOT NULL,
  no_o_id INTEGER NOT NULL,
  CONSTRAINT pk_new_order PRIMARY KEY (no_w_id, no_d_id, no_o_id)
) PARTITION BY RANGE(no_w_id) (
PARTITION p0 VALUES LESS THAN (21) placement POLICY = tikv0,
PARTITION p1 VALUES LESS THAN (41) placement POLICY = tikv1,
PARTITION p2 VALUES LESS THAN (61) placement POLICY = tikv2,
PARTITION p3 VALUES LESS THAN (81) placement POLICY = tikv3,
PARTITION p4 VALUES LESS THAN (101) placement POLICY = tikv4,
PARTITION p5 VALUES LESS THAN (121) placement POLICY = tikv5,
PARTITION p6 VALUES LESS THAN (141) placement POLICY = tikv6,
PARTITION p7 VALUES LESS THAN (161) placement POLICY = tikv7,
PARTITION p8 VALUES LESS THAN (181) placement POLICY = tikv8);

CREATE TABLE table_oorder (
  o_w_id INTEGER NOT NULL,
  o_d_id INTEGER NOT NULL,
  o_id INTEGER NOT NULL,
  o_c_id INTEGER,
  o_carrier_id INTEGER,
  o_ol_cnt INTEGER,
  o_all_local INTEGER,
  o_entry_d TIMESTAMP,
  CONSTRAINT pk_oorder PRIMARY KEY (o_w_id, o_d_id, o_id),
  CONSTRAINT table_oorder_idx1 UNIQUE KEY(o_w_id, o_d_id, o_carrier_id, o_id)
) PARTITION BY RANGE(o_w_id) (
PARTITION p0 VALUES LESS THAN (21) placement POLICY = tikv0,
PARTITION p1 VALUES LESS THAN (41) placement POLICY = tikv1,
PARTITION p2 VALUES LESS THAN (61) placement POLICY = tikv2,
PARTITION p3 VALUES LESS THAN (81) placement POLICY = tikv3,
PARTITION p4 VALUES LESS THAN (101) placement POLICY = tikv4,
PARTITION p5 VALUES LESS THAN (121) placement POLICY = tikv5,
PARTITION p6 VALUES LESS THAN (141) placement POLICY = tikv6,
PARTITION p7 VALUES LESS THAN (161) placement POLICY = tikv7,
PARTITION p8 VALUES LESS THAN (181) placement POLICY = tikv8);

CREATE TABLE table_order_line (
  ol_w_id INTEGER NOT NULL,
  ol_d_id INTEGER NOT NULL,
  ol_o_id INTEGER NOT NULL,
  ol_number INTEGER NOT NULL,
  ol_i_id INTEGER NOT NULL,
  ol_delivery_d TIMESTAMP,
  ol_amount DECIMAL(6, 2),
  ol_supply_w_id INTEGER,
  ol_quantity INTEGER,
  ol_dist_info CHAR(24),
  CONSTRAINT pk_order_line PRIMARY KEY (ol_w_id, ol_d_id, ol_o_id, ol_number)
) PARTITION BY RANGE(ol_w_id) (
PARTITION p0 VALUES LESS THAN (21) placement POLICY = tikv0,
PARTITION p1 VALUES LESS THAN (41) placement POLICY = tikv1,
PARTITION p2 VALUES LESS THAN (61) placement POLICY = tikv2,
PARTITION p3 VALUES LESS THAN (81) placement POLICY = tikv3,
PARTITION p4 VALUES LESS THAN (101) placement POLICY = tikv4,
PARTITION p5 VALUES LESS THAN (121) placement POLICY = tikv5,
PARTITION p6 VALUES LESS THAN (141) placement POLICY = tikv6,
PARTITION p7 VALUES LESS THAN (161) placement POLICY = tikv7,
PARTITION p8 VALUES LESS THAN (181) placement POLICY = tikv8);

CREATE TABLE table_item (
  i_id INTEGER NOT NULL,
  i_name VARCHAR(24),
  i_price DECIMAL(5, 2),
  i_data VARCHAR(50),
  i_im_id INTEGER,
  CONSTRAINT pk_item PRIMARY KEY (i_id)
);

CREATE TABLE table_stock (
  s_w_id INTEGER NOT NULL,
  s_i_id INTEGER NOT NULL,
  s_quantity INTEGER,
  s_ytd INTEGER,
  s_order_cnt INTEGER,
  s_remote_cnt INTEGER,
  s_data VARCHAR(50),
  s_dist_01 CHAR(24),
  s_dist_02 CHAR(24),
  s_dist_03 CHAR(24),
  s_dist_04 CHAR(24),
  s_dist_05 CHAR(24),
  s_dist_06 CHAR(24),
  s_dist_07 CHAR(24),
  s_dist_08 CHAR(24),
  s_dist_09 CHAR(24),
  s_dist_10 CHAR(24),
  CONSTRAINT pk_stock PRIMARY KEY (s_w_id, s_i_id)
) PARTITION BY RANGE(s_w_id) (
PARTITION p0 VALUES LESS THAN (21) placement POLICY = tikv0,
PARTITION p1 VALUES LESS THAN (41) placement POLICY = tikv1,
PARTITION p2 VALUES LESS THAN (61) placement POLICY = tikv2,
PARTITION p3 VALUES LESS THAN (81) placement POLICY = tikv3,
PARTITION p4 VALUES LESS THAN (101) placement POLICY = tikv4,
PARTITION p5 VALUES LESS THAN (121) placement POLICY = tikv5,
PARTITION p6 VALUES LESS THAN (141) placement POLICY = tikv6,
PARTITION p7 VALUES LESS THAN (161) placement POLICY = tikv7,
PARTITION p8 VALUES LESS THAN (181) placement POLICY = tikv8);
