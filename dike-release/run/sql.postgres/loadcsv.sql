copy table_warehouse
from
    '@fileLocation@/dike.table_warehouse.csv' WITH (format CSV, null 'NULL');

copy table_district
from
    '@fileLocation@/dike.table_district.csv' WITH (format CSV, null 'NULL');

copy table_config
from
    '@fileLocation@/dike.table_config.csv' WITH (format CSV, null 'NULL');

copy table_item
from
    '@fileLocation@/dike.table_item.csv' WITH (format CSV, null 'NULL');

copy table_oorder
from
    '@fileLocation@/dike.table_oorder.csv' WITH (format CSV, null 'NULL');

copy table_stock
from
    '@fileLocation@/dike.table_stock.csv' WITH (format CSV, null 'NULL');

copy table_history
from
    '@fileLocation@/dike.table_history.csv' WITH (format CSV, null 'NULL');

copy table_new_order
from
    '@fileLocation@/dike.table_new_order.csv' WITH (format CSV, null 'NULL');

copy table_order_line
from
    '@fileLocation@/dike.table_order_line.csv' WITH (format CSV, null 'NULL');

copy table_customer
from
    '@fileLocation@/dike.table_customer.csv' WITH (format CSV, null 'NULL');