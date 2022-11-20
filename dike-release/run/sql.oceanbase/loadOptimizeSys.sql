ALTER SYSTEM
SET
    enable_sql_audit = 'false';

ALTER SYSTEM
SET
    memory_chunk_cache_size = '16G';

ALTER SYSTEM
SET
    trx_try_wait_lock_timeout = '0ms';

ALTER SYSTEM
SET
    large_query_threshold = '1s';

ALTER SYSTEM
SET
    trace_log_slow_query_watermark = '500ms';

ALTER SYSTEM
SET
    syslog_io_bandwidth_limit = '30m';

ALTER SYSTEM
SET
    enable_async_syslog = true;

ALTER SYSTEM
SET
    merger_warm_up_duration_time = '0';

ALTER SYSTEM
SET
    merger_switch_leader_duration_time = '0';

ALTER SYSTEM
SET
    large_query_worker_percentage = 10;

ALTER SYSTEM
SET
    builtin_db_data_verify_cycle = 0;

ALTER SYSTEM
SET
    enable_merge_by_turn = 'false';

ALTER SYSTEM
SET
    minor_merge_concurrency = 30;

ALTER SYSTEM
SET
    memory_limit_percentage = 85;

ALTER SYSTEM
SET
    memstore_limit_percentage = 80;

ALTER SYSTEM
SET
    freeze_trigger_percentage = 30;

ALTER SYSTEM
SET
    enable_syslog_recycle = 'true';

ALTER SYSTEM
SET
    max_syslog_file_count = 100;

ALTER SYSTEM
SET
    minor_freeze_times = 500;

ALTER SYSTEM
SET
    minor_compact_trigger = 5;

ALTER SYSTEM
SET
    max_kept_major_version_number = 1;

ALTER SYSTEM
SET
    sys_bkgd_io_high_percentage = 90;

ALTER SYSTEM
SET
    sys_bkgd_io_low_percentage = 70;

ALTER SYSTEM
SET
    merge_thread_count = 45;

ALTER SYSTEM
SET
    merge_stat_sampling_ratio = 1;

ALTER SYSTEM
SET
    writing_throttling_trigger_percentage = 75 tenant = 'test';

ALTER SYSTEM
SET
    writing_throttling_maximum_duration = '15m';

SET
    global ob_plan_cache_percentage = 20;

ALTER SYSTEM
SET
    use_large_pages = 'true';

ALTER SYSTEM
SET
    micro_block_merge_verify_level = 0;

ALTER SYSTEM
SET
    builtin_db_data_verify_cycle = 20;

ALTER SYSTEM
SET
    net_thread_count = 4;