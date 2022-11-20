ALTER SYSTEM
SET
    _clog_aggregation_buffer_amount = 8;

ALTER SYSTEM
SET
    _flush_clog_aggregation_buffer_timeout = '1ms';

SET
    global ob_query_timeout = 1000000000;