ALTER SYSTEM
SET
    _clog_aggregation_buffer_amount = 8;

ALTER SYSTEM
SET
    _flush_clog_aggregation_buffer_timeout = '1ms';

SET
    global ob_query_timeout = 10000000;

SET
    global ob_trx_timeout = 10000000;

SET
    global max_allowed_packet = 67108864;

SET
    global ob_sql_work_area_percentage = 100;

SET
    global parallel_max_servers = 90;

SET
    global parallel_servers_target = 648;