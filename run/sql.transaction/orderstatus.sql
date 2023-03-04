SELECT c_id
FROM table_customer
WHERE c_w_id = ? AND c_d_id = ? AND c_last = ?
ORDER BY c_first;

SELECT c_w_id, c_d_id, c_id
FROM table_customer
WHERE c_phone = ?;

SELECT c_first, c_middle, c_last, c_balance
FROM table_customer
WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?;

SELECT o_id, o_entry_d, o_carrier_id
FROM table_oorder
WHERE o_w_id = ? AND o_d_id = ? AND o_c_id = ? AND o_id = (
    SELECT max(o_id)
    FROM table_oorder
    WHERE o_w_id = ? AND o_d_id = ? AND o_c_id = ?
);

SELECT ol_i_id, ol_supply_w_id, ol_quantity, ol_amount, ol_delivery_d
FROM table_order_line
WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ?
ORDER BY ol_w_id, ol_d_id, ol_o_id, ol_number;