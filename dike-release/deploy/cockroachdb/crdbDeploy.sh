#!/bin/bash
# ----
# Deploy cockroachdb cluster according to the given config file.
#
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

if [ $# -ne 1 ] ; then 
    echo "Usage: $(basename $0) CONF_FILE" >&2
    exit 1
fi

# parse yaml config file
source parseyaml.sh
create_variables $1
ips=($(echo ${joinlist} | tr "," "\n"))

# install cockroachdb in client
curl https://binaries.cockroachdb.com/cockroach-v22.1.1.linux-amd64.tgz | tar -xz
cp -i cockroach-v22.1.1.linux-amd64/cockroach /usr/local/bin/
if [ $? -ne 0 ] ; then 
    echo "Error: fail to install cockroachdb in client" >&2
    exit 2
fi

# deploy cockroachdb cluster
for i in ${!ips[@]} ; do 
zone=`expr ${i} / 3`
scp -r cockroach-v22.1.1.linux-amd64 root@${ips[i]}:/root
ssh root@${ips[i]} "
# curl https://binaries.cockroachdb.com/cockroach-v22.1.1.linux-amd64.tgz | tar -xz
cp -i cockroach-v22.1.1.linux-amd64/cockroach /usr/local/bin/
mkdir -p /usr/local/lib/cockroach
cp -i cockroach-v22.1.1.linux-amd64/lib/libgeos.so /usr/local/lib/cockroach/
cp -i cockroach-v22.1.1.linux-amd64/lib/libgeos_c.so /usr/local/lib/cockroach/
"
if [ $? -ne 0 ] ; then 
    echo "Error: fail to install cockroachdb in root@${ips[i]}" >&2
    exit 3
fi
ssh -t root@${ips[i]} "
cockroach start --insecure --advertise-addr=${ips[i]} --join=${joinlist} --http-port=8081 --cache=.25 --background --locality=zone=${zone},rack=${i} --store=/data/cockroachdb
"
if [ $? -ne 0 ] ; then 
    echo "Error: fail to start cockroachdb in root@${ips[i]}" >&2
    exit 4
fi
done

# initialize cockroachdb cluster
cockroach init --insecure --host=${ips[0]}
sleep 10s
if [ $? -ne 0 ] ; then 
    echo "Error: fail to initialize cockroachdb cluster" >&2
    exit 5
fi

# start haproxy
cockroach gen haproxy --insecure --host=${ips[0]} --port=26257
nohup haproxy -f haproxy.cfg &