#!/bin/bash
# ----
# Destroy cockroachdb cluster.
#
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

source parseyaml.sh
create_variables ${1}

# destroy cockroachdb cluster
ips=($(echo ${joinlist} | tr "," "\n"))
for ip in ${ips[@]} ; do
    ssh root@${ip} "ps aux | grep cockroach | grep -v grep | awk '{print \$2}' | xargs kill -9"
    # if [ $? -ne 0 ] ; then
    #     echo "Error: fail to stop cockroachdb server in root@${ip}" >&2
    #     exit 1
    # fi
    ssh root@${ip} "rm -rf /data/cockroachdb"
    # if [ $? -ne 0 ] ; then
    #     echo "Error: fail to remove cockroachdb datafile in root@${ip}" >&2
    #     exit 2
    # fi
done

# stop haproxy
kill $(ps aux | grep haproxy | grep -v grep | awk '{print $2}')
