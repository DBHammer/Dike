SELECT w_name, w_street_1, w_street_2, w_city, w_state, w_zip
FROM table_warehouse
WHERE w_id = ?;

SELECT d_name, d_street_1, d_street_2, d_city, d_state, d_zip
FROM table_district
WHERE d_w_id = ? AND d_id = ?;

SELECT c_id
FROM table_customer
WHERE c_w_id = ? AND c_d_id = ? AND c_last = ?
ORDER BY c_first;

SELECT c_w_id, c_d_id, c_id
FROM table_customer
WHERE c_phone = ?;

SELECT c_first, c_middle, c_last, c_street_1, c_street_2,
       c_city, c_state, c_zip, c_phone, c_since, c_credit,
       c_credit_lim, c_discount, c_balance
FROM table_customer
WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?
FOR UPDATE;

SELECT c_data
FROM table_customer
WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?;

UPDATE table_warehouse
SET w_ytd = w_ytd + ?
WHERE w_id = ?;

UPDATE table_district
SET d_ytd = d_ytd + ?
WHERE d_w_id = ? AND d_id = ?;

UPDATE table_customer
SET c_balance = c_balance - ?,
    c_ytd_payment = c_ytd_payment + ?,
    c_payment_cnt = c_payment_cnt + 1
WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?;

UPDATE table_customer
SET c_balance = c_balance - ?,
    c_ytd_payment = c_ytd_payment + ?,
    c_payment_cnt = c_payment_cnt + 1,
    c_data = ?
WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?;

INSERT INTO table_history (
    h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id,
    h_date, h_amount, h_data
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
