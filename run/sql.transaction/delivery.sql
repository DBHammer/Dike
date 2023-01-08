SELECT no_o_id
FROM table_new_order
WHERE no_w_id = ? AND no_d_id = ?
ORDER BY no_o_id ASC;

DELETE FROM table_new_order
WHERE no_w_id = ? AND no_d_id = ? AND no_o_id = ?;

SELECT o_c_id
FROM table_oorder
WHERE o_w_id = ? AND o_d_id = ? AND o_id = ?;

UPDATE table_oorder
SET o_carrier_id = ?
WHERE o_w_id = ? AND o_d_id = ? AND o_id = ?;

SELECT sum(ol_amount) AS sum_ol_amount
FROM table_order_line
WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ?;

UPDATE table_order_line
SET ol_delivery_d = ?
WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ?;

UPDATE table_customer
SET c_balance = c_balance + ?,
    c_delivery_cnt = c_delivery_cnt + 1
WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?;