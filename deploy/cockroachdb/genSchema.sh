#!/bin/bash
# ----
# Generate schema script for cockroachdb, 
# according to RACK_NUM(the number of servers in the cluster) and WAREHOUSE_NUM(the scale factor of Dike)
# 
# Generate sql script to pin partitions to racks for cockroachdb, 
# according to RACK_NUM(the number of servers in the cluster).
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

if [ $# -ne 3 ]; then
  echo "Usage: $(basename $0) RACK_NUM WAREHOUSE_NUM PARTITION_NUM" >&2
  exit 1
fi

warehouse=$2
partition=$3
partitions=($(seq ${partition}))
partitionSize=($(expr ${warehouse} / ${partition}))
leftBound=1
rightBound=($(expr ${leftBound} + ${partitionSize}))

# partitions
partitionSuffix=""
for p in ${!partitions[@]}; do
  partitionSuffix+="PARTITION p${p} VALUES from (${leftBound}) to (${rightBound}),\n"
  leftBound=${rightBound}
  rightBound=($(expr ${leftBound} + ${partitionSize}))
done
partitionSuffix=${partitionSuffix::-3}
partitionSuffix+=");"

# table_config
echo "
CREATE TABLE table_config (
  cfg_name    string PRIMARY KEY,
  cfg_value   string
);" > ../../run/sql.cockroachdb/tableCreates.sql

# table_warehouse
echo "
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
) PARTITION BY RANGE (w_id) (" >> ../../run/sql.cockroachdb/tableCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/tableCreates.sql

# table_district
echo "
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
) PARTITION BY RANGE (d_w_id) (" >> ../../run/sql.cockroachdb/tableCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/tableCreates.sql

# table_customer
echo "
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
) PARTITION BY RANGE (c_w_id) (" >> ../../run/sql.cockroachdb/tableCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/tableCreates.sql

# table_history
echo "
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
);" >> ../../run/sql.cockroachdb/tableCreates.sql

# table_new_order
echo "
CREATE TABLE table_new_order (
  no_w_id  int NOT NULL,
  no_d_id  int NOT NULL,
  no_o_id  int NOT NULL,
  PRIMARY KEY (no_w_id, no_d_id, no_o_id)
) PARTITION BY RANGE (no_w_id) (" >> ../../run/sql.cockroachdb/tableCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/tableCreates.sql

# table_oorder
echo "
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
) PARTITION BY RANGE (o_w_id) (" >> ../../run/sql.cockroachdb/tableCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/tableCreates.sql

# table_order_line
echo "
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
) PARTITION BY RANGE (ol_w_id) (" >> ../../run/sql.cockroachdb/tableCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/tableCreates.sql

# table_item
echo "
CREATE TABLE table_item (
  i_id     int NOT NULL,
  i_name   string,
  i_price  DECIMAL,
  i_data   string,
  i_im_id  int,
  PRIMARY KEY (i_id)
);" >> ../../run/sql.cockroachdb/tableCreates.sql

# table_stock
echo "
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
) PARTITION BY RANGE (s_w_id) (" >> ../../run/sql.cockroachdb/tableCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/tableCreates.sql

# table_customer_idx1
echo "
CREATE INDEX table_customer_idx1 ON table_customer (c_w_id, c_d_id, c_last, c_first);" > ../../run/sql.cockroachdb/indexCreates.sql
echo "ALTER INDEX table_customer_idx1 PARTITION BY RANGE(c_w_id) (" >> ../../run/sql.cockroachdb/indexCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/indexCreates.sql

# table_oorder_idx1
echo "
CREATE INDEX table_oorder_idx1 ON table_oorder (o_w_id, o_d_id, o_carrier_id, o_id);" >> ../../run/sql.cockroachdb/indexCreates.sql
echo "ALTER INDEX table_oorder_idx1 PARTITION BY RANGE(o_w_id) (" >> ../../run/sql.cockroachdb/indexCreates.sql
echo -e ${partitionSuffix} >> ../../run/sql.cockroachdb/indexCreates.sql

# pin partitions to racks
tableList=("table_warehouse" "table_district" "table_customer" "table_new_order" "table_oorder" "table_order_line" "table_stock")
echo "ALTER TABLE table_item CONFIGURE ZONE USING num_replicas=$1, global_reads='true';" >> ../../run/sql.cockroachdb/tableCreates.sql
for table in ${tableList[@]}; do
    for p in ${!partitions[@]}; do
        zone=$(expr ${p} / 3 % 3)
        rack=$(expr ${p} % $1)
        echo "ALTER PARTITION p${p} OF TABLE ${table} CONFIGURE ZONE USING constraints='[+zone=${zone}]', lease_preferences='[[+rack=${rack}]]';" >> ../../run/sql.cockroachdb/tableCreates.sql
        if [ ${table} = 'table_customer' ]; then
            echo "ALTER PARTITION p${p} OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=${zone}]', lease_preferences='[[+rack=${rack}]]';" >> ../../run/sql.cockroachdb/indexCreates.sql
        elif [ ${table} = 'table_oorder' ]; then
            echo "ALTER PARTITION p${p} OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=${zone}]', lease_preferences='[[+rack=${rack}]]';" >> ../../run/sql.cockroachdb/indexCreates.sql
        fi
    done
done
