#!/bin/bash
# ----
# Start cockroachdb cluster.
#
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

if [ $# -ne 1 ] ; then 
    echo "Usage: $(basename $0) CONF_FILE" >&2
    exit 1
fi

source parseyaml.sh
create_variables $1

# start cockroachdb cluster
ips=($(echo ${joinlist} | tr "," "\n"))
for i in ${!ips[@]} ; do 
    zone=`expr ${i} / 3`
    ssh -t root@${ips[i]} "
    cockroach start --insecure --advertise-addr=${ips[i]} --join=${joinlist} --http-port=8081 --cache=.25 --background --locality=zone=${zone},rack=${i} --store=/data/cockroachdb
    "
    if [ $? -ne 0 ] ; then
        echo "Error: fail to start cockroachdb in root@${ips[i]}" >&2
        exit 1
    fi
done

# start haproxy
nohup haproxy -f haproxy.cfg &