#!/bin/bash
# ----
# Stop cockroachdb cluster.
#
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

source parseyaml.sh
create_variables ${1}
ips=($(echo ${joinlist} | tr "," "\n"))

# stop crdb cluster
for ip in ${ips[@]} ; do
    ssh root@${ip} "ps aux | grep cockroach | grep -v grep | awk '{print \$2}' | xargs kill -9"
    if [ $? -ne 0 ] ; then
        echo "Error: fail to stop cockroachdb in root@${ips[i]}" >&2
        exit 1
    fi
done

# stop haproxy
kill $(ps aux | grep haproxy | grep -v grep | awk '{print $2}')