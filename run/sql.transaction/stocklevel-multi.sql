SELECT  count(*) AS low_stock
FROM (
    SELECT s_w_id, s_i_id, s_quantity
    FROM table_stock
    WHERE s_w_id in (1, 2, 3) AND s_quantity < 30 AND s_i_id IN (
        SELECT ol_i_id
        FROM table_district
        JOIN table_order_line ON ol_w_id = d_w_id
        AND ol_d_id = d_id
        AND ol_o_id >= d_next_o_id - 20
        AND ol_o_id < d_next_o_id
        WHERE d_w_id = s_w_id AND d_id = 2
    )
) AS L;