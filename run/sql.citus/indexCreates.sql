CREATE INDEX table_customer_idx1 ON table_customer using BRIN(c_w_id, c_d_id, c_last, c_first);

CREATE INDEX table_oorder_idx1 ON table_oorder using BRIN(o_w_id, o_d_id, o_carrier_id, o_id);