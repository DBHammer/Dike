LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_warehouse.csv' INTO TABLE table_warehouse fields terminated BY ',';

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_district.csv' INTO TABLE table_district fields terminated BY ',';

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_config.csv' INTO TABLE table_config fields terminated BY ',';

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_item.csv' INTO TABLE table_item fields terminated BY ',';

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_oorder.csv' INTO TABLE table_oorder fields terminated BY ',';

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_stock.csv' INTO TABLE table_stock fields terminated BY ',';

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_history.csv' INTO TABLE table_history fields terminated BY ',' (
    h_c_id,
    h_c_d_id,
    h_c_w_id,
    h_d_id,
    h_w_id,
    h_date,
    h_amount,
    h_data
);

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_new_order.csv' INTO TABLE table_new_order fields terminated BY ',';

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_order_line.csv' INTO TABLE table_order_line fields terminated BY ',';

LOAD DATA
/*+ parallel(80) */
INFILE '@fileLocation@/dike.table_customer.csv' INTO TABLE table_customer fields terminated BY ',';