SET CLUSTER SETTING kv.dist_sender.concurrency_limit = 2016;
SET CLUSTER SETTING kv.snapshot_rebalance.max_rate = '256 MiB';
SET CLUSTER SETTING kv.snapshot_recovery.max_rate = '256 MiB';
SET CLUSTER SETTING sql.stats.automatic_collection.enabled = false;
SET CLUSTER SETTING schemachanger.backfiller.max_buffer_size = '5 GiB';
SET CLUSTER SETTING rocksdb.min_wal_sync_interval = '500us';
SET CLUSTER SETTING kv.range_merge.queue_enabled = false;
ALTER RANGE default CONFIGURE ZONE USING gc.ttlseconds = 600;
SET CLUSTER SETTING cluster.organization = 'Account Pool 1';
SET CLUSTER SETTING enterprise.license = 'crl-0-EKX9wZkGGAIiDkFjY291bnQgUG9vbCAx';