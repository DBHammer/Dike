ALTER SYSTEM
SET
    writing_throttling_trigger_percentage = 100 tenant = 'test';

ALTER SYSTEM
SET
    writing_throttling_maximum_duration = '1h';

ALTER SYSTEM
SET
    memstore_limit_percentage = 80;

ALTER SYSTEM
SET
    freeze_trigger_percentage = 30;

ALTER SYSTEM
SET
    large_query_threshold = '200s';

ALTER SYSTEM
SET
    trx_try_wait_lock_timeout = '0ms';

ALTER SYSTEM
SET
    cpu_quota_concurrency = 4;

ALTER SYSTEM
SET
    minor_warm_up_duration_time = 0;

ALTER SYSTEM
SET
    minor_freeze_times = 500;

ALTER SYSTEM
SET
    minor_compact_trigger = 3;

ALTER SYSTEM
SET
    sys_bkgd_io_high_percentage = 90;

ALTER SYSTEM
SET
    sys_bkgd_io_low_percentage = 70;

ALTER SYSTEM
SET
    minor_merge_concurrency = 20;

ALTER SYSTEM
SET
    builtin_db_data_verify_cycle = 0;

ALTER SYSTEM
SET
    trace_log_slow_query_watermark = '10s';

ALTER SYSTEM
SET
    gts_refresh_interval = '500us';

ALTER SYSTEM
SET
    server_permanent_offline_time = '36000s';

ALTER SYSTEM
SET
    weak_read_version_refresh_interval = 0;

ALTER SYSTEM
SET
    _ob_get_gts_ahead_interval = '5ms';

ALTER SYSTEM
SET
    bf_cache_priority = 10;

ALTER SYSTEM
SET
    user_block_cache_priority = 5;

ALTER SYSTEM
SET
    merge_stat_sampling_ratio = 0;

ALTER SYSTEM
SET
    syslog_level = 'PERF';

ALTER SYSTEM
SET
    max_syslog_file_count = 100;

ALTER SYSTEM
SET
    enable_syslog_recycle = 'true';

ALTER SYSTEM
SET
    ob_enable_batched_multi_statement = 'true' tenant = all;

ALTER SYSTEM
SET
    _cache_wash_interval = '1m';

ALTER SYSTEM
SET
    plan_cache_evict_interval = '30s';

ALTER SYSTEM
SET
    enable_one_phase_commit = 'false';

ALTER SYSTEM
SET
    enable_monotonic_weak_read = 'false';

ALTER SYSTEM major freeze;