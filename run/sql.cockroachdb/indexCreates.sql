
CREATE INDEX table_customer_idx1 ON table_customer (c_w_id, c_d_id, c_last, c_first);
ALTER INDEX table_customer_idx1 PARTITION BY RANGE(c_w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));

CREATE INDEX table_oorder_idx1 ON table_oorder (o_w_id, o_d_id, o_carrier_id, o_id);
ALTER INDEX table_oorder_idx1 PARTITION BY RANGE(o_w_id) (
PARTITION p0 VALUES from (1) to (21),
PARTITION p1 VALUES from (21) to (41),
PARTITION p2 VALUES from (41) to (61),
PARTITION p3 VALUES from (61) to (81),
PARTITION p4 VALUES from (81) to (101),
PARTITION p5 VALUES from (101) to (121),
PARTITION p6 VALUES from (121) to (141),
PARTITION p7 VALUES from (141) to (161),
PARTITION p8 VALUES from (161) to (181));
ALTER PARTITION p0 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF INDEX table_customer_idx1 CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
ALTER PARTITION p0 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=0]]';
ALTER PARTITION p1 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=1]]';
ALTER PARTITION p2 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=0]', lease_preferences='[[+rack=2]]';
ALTER PARTITION p3 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=3]]';
ALTER PARTITION p4 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=4]]';
ALTER PARTITION p5 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=1]', lease_preferences='[[+rack=5]]';
ALTER PARTITION p6 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=6]]';
ALTER PARTITION p7 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=7]]';
ALTER PARTITION p8 OF INDEX table_oorder_idx1 CONFIGURE ZONE USING constraints='[+zone=2]', lease_preferences='[[+rack=8]]';
