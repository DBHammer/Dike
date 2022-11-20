ALTER proxyconfig
SET
    enable_strict_kernel_release = false;

ALTER proxyconfig
SET
    automatic_match_work_thread = false;

ALTER proxyconfig
SET
    proxy_mem_limited = '4G';

ALTER proxyconfig
SET
    enable_compression_protocol = false;

ALTER proxyconfig
SET
    slow_proxy_process_time_threshold = '500ms';

ALTER proxyconfig
SET
    enable_ob_protocol_v2 = false;

ALTER proxyconfig
SET
    enable_qos = false;

ALTER proxyconfig
SET
    syslog_level = 'error';

alter proxyconfig set enable_compression_protocol=false; --关闭压缩，降低cpu%
alter proxyconfig set proxy_mem_limited='16G'; --防止oom，可根据实际环境动态调整
alter proxyconfig set enable_prometheus=false;
alter proxyconfig set enable_metadb_used=false;
alter proxyconfig set enable_standby=false;
alter proxyconfig set enable_strict_stat_time=false;
alter proxyconfig set use_local_dbconfig=true;
ALTER PROXYCONFIG SET work_thread_num=128;
ALTER PROXYCONFIG SET enable_async_log=true;