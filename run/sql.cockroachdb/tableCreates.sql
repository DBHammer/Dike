
CREATE TABLE table_config (
  cfg_name    string PRIMARY KEY,
  cfg_value   string
);

CREATE TABLE table_warehouse (
  w_id        int NOT NULL,
  w_ytd       DECIMAL,
  w_tax       DECIMAL,
  w_name      string,
  w_street_1  string,
  w_street_2  string,
  w_city      string,
  w_state     string,
  w_zip       string,
  PRIMARY KEY(w_id)
) PARTITION BY RANGE (w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));

CREATE TABLE table_district (
  d_w_id       int NOT NULL,
  d_id         int NOT NULL,
  d_ytd        DECIMAL,
  d_tax        DECIMAL,
  d_next_o_id  int,
  d_name       string,
  d_street_1   string,
  d_street_2   string,
  d_city       string,
  d_state      string,
  d_zip        string,
  PRIMARY KEY (d_w_id, d_id)
) PARTITION BY RANGE (d_w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));

CREATE TABLE table_customer (
  c_w_id         int NOT NULL,
  c_d_id         int NOT NULL,
  c_id           int NOT NULL,
  c_discount     DECIMAL,
  c_credit       string,
  c_last         string,
  c_first        string,
  c_credit_lim   DECIMAL,
  c_balance      DECIMAL,
  c_ytd_payment  DECIMAL,
  c_payment_cnt  int,
  c_delivery_cnt int,
  c_street_1     string,
  c_street_2     string,
  c_city         string,
  c_state        string,
  c_zip          string,
  c_phone        string,
  c_since        TIMESTAMP,
  c_middle       string,
  c_data         string,
  PRIMARY KEY (c_w_id, c_d_id, c_id)
) PARTITION BY RANGE (c_w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));

CREATE TABLE table_history (
  hist_id  int,
  h_c_id   int,
  h_c_d_id int,
  h_c_w_id int,
  h_d_id   int,
  h_w_id   int,
  h_date   TIMESTAMP,
  h_amount DECIMAL,
  h_data   string
);

CREATE TABLE table_new_order (
  no_w_id  int NOT NULL,
  no_d_id  int NOT NULL,
  no_o_id  int NOT NULL,
  PRIMARY KEY (no_w_id, no_d_id, no_o_id)
) PARTITION BY RANGE (no_w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));

CREATE TABLE table_oorder (
  o_w_id       int NOT NULL,
  o_d_id       int NOT NULL,
  o_id         int NOT NULL,
  o_c_id       int,
  o_carrier_id int,
  o_ol_cnt     int,
  o_all_local  int,
  o_entry_d    TIMESTAMP,
  PRIMARY KEY (o_w_id, o_d_id, o_id)
) PARTITION BY RANGE (o_w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));

CREATE TABLE table_order_line (
  ol_w_id         int NOT NULL,
  ol_d_id         int NOT NULL,
  ol_o_id         int NOT NULL,
  ol_number       int NOT NULL,
  ol_i_id         int NOT NULL,
  ol_delivery_d   TIMESTAMP,
  ol_amount       DECIMAL,
  ol_supply_w_id  int,
  ol_quantity     int,
  ol_dist_info    string,
  PRIMARY KEY (ol_w_id, ol_d_id, ol_o_id, ol_number)
) PARTITION BY RANGE (ol_w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));

CREATE TABLE table_item (
  i_id     int NOT NULL,
  i_name   string,
  i_price  DECIMAL,
  i_data   string,
  i_im_id  int,
  PRIMARY KEY (i_id)
);

CREATE TABLE table_stock (
  s_w_id       int NOT NULL,
  s_i_id       int NOT NULL,
  s_quantity   int,
  s_ytd        int,
  s_order_cnt  int,
  s_remote_cnt int,
  s_data       string,
  s_dist_01    string,
  s_dist_02    string,
  s_dist_03    string,
  s_dist_04    string,
  s_dist_05    string,
  s_dist_06    string,
  s_dist_07    string,
  s_dist_08    string,
  s_dist_09    string,
  s_dist_10    string,
  PRIMARY KEY (s_w_id, s_i_id)
) PARTITION BY RANGE (s_w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));
ALTER TABLE table_item CONFIGURE ZONE USING num_replicas=9, global_reads='true';
ALTER PARTITION p0 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF TABLE table_warehouse CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
ALTER PARTITION p0 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF TABLE table_district CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
ALTER PARTITION p0 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF TABLE table_customer CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
ALTER PARTITION p0 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF TABLE table_new_order CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
ALTER PARTITION p0 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF TABLE table_oorder CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
ALTER PARTITION p0 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF TABLE table_order_line CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
ALTER PARTITION p0 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF TABLE table_stock CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
