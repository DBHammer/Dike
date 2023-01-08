SELECT c_discount, c_last, c_credit, w_tax
FROM table_customer
JOIN table_warehouse ON (w_id = c_w_id)
WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?;

SELECT d_tax, d_next_o_id
FROM table_district
WHERE d_w_id = ? AND d_id = ?
FOR UPDATE;

UPDATE table_district
SET d_next_o_id = d_next_o_id + 1
WHERE d_w_id = ? AND d_id = ?;

INSERT INTO table_oorder (
    o_id, o_d_id, o_w_id, o_c_id, o_entry_d,
    o_ol_cnt, o_all_local
) VALUES (?, ?, ?, ?, ?, ?, ?);

INSERT INTO table_new_order (
    no_o_id, no_d_id, no_w_id
) VALUES (?, ?, ?);

SELECT s_quantity, s_data, s_dist_01, s_dist_02, s_dist_03, s_dist_04,
       s_dist_05, s_dist_06, s_dist_07, s_dist_08, s_dist_09, s_dist_10
FROM table_stock
WHERE s_w_id = ? AND s_i_id = ?
FOR UPDATE;

SELECT i_price, i_name, i_data
FROM table_item
WHERE i_id = ?;

UPDATE table_stock
SET s_quantity = ?, s_ytd = s_ytd + ?,
    s_order_cnt = s_order_cnt + 1,
    s_remote_cnt = s_remote_cnt + ?
WHERE s_w_id = ? AND s_i_id = ?;

INSERT INTO table_order_line (
    ol_o_id, ol_d_id, ol_w_id, ol_number,
    ol_i_id, ol_supply_w_id, ol_quantity,
    ol_amount, ol_dist_info
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);