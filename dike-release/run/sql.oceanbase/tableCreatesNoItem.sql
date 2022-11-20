CREATE TABLE table_config (
  cfg_name VARCHAR(30) PRIMARY KEY,
  cfg_value VARCHAR(50)
);

CREATE tablegroup dike_group BINDING true PARTITION BY HASH PARTITIONS 9;

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
  PRIMARY KEY(w_id)
) tablegroup = 'dike_group' PARTITION BY HASH(w_id) PARTITIONS 9;

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
  PRIMARY KEY (d_w_id, d_id)
) tablegroup = 'dike_group' PARTITION BY HASH(d_w_id) PARTITIONS 9;

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
  PRIMARY KEY (c_w_id, c_d_id, c_id)
) tablegroup = 'dike_group' PARTITION BY HASH(c_w_id) PARTITIONS 9;

CREATE TABLE table_history (
  hist_id INTEGER NOT NULL AUTO_INCREMENT,
  h_c_id INTEGER,
  h_c_d_id INTEGER,
  h_c_w_id INTEGER,
  h_d_id INTEGER,
  h_w_id INTEGER,
  h_date TIMESTAMP,
  h_amount DECIMAL(6, 2),
  h_data VARCHAR(24)
) tablegroup = 'dike_group' PARTITION BY HASH(h_w_id) PARTITIONS 9;

CREATE TABLE table_new_order (
  no_w_id INTEGER NOT NULL,
  no_d_id INTEGER NOT NULL,
  no_o_id INTEGER NOT NULL,
  PRIMARY KEY (no_w_id, no_d_id, no_o_id)
) tablegroup = 'dike_group' PARTITION BY HASH(no_w_id) PARTITIONS 9;

CREATE TABLE table_oorder (
  o_w_id INTEGER NOT NULL,
  o_d_id INTEGER NOT NULL,
  o_id INTEGER NOT NULL,
  o_c_id INTEGER,
  o_carrier_id INTEGER,
  o_ol_cnt INTEGER,
  o_all_local INTEGER,
  o_entry_d TIMESTAMP,
  PRIMARY KEY (o_w_id, o_d_id, o_id)
) tablegroup = 'dike_group' PARTITION BY HASH(o_w_id) PARTITIONS 9;

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
  PRIMARY KEY (ol_w_id, ol_d_id, ol_o_id, ol_number)
) tablegroup = 'dike_group' PARTITION BY HASH(ol_w_id) PARTITIONS 9;

CREATE TABLE table_item (
  i_id INTEGER NOT NULL,
  i_name VARCHAR(24),
  i_price DECIMAL(5, 2),
  i_data VARCHAR(50),
  i_im_id INTEGER,
  PRIMARY KEY (i_id)
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
  PRIMARY KEY (s_w_id, s_i_id)
) tablegroup = 'dike_group' PARTITION BY HASH(s_w_id) PARTITIONS 9;