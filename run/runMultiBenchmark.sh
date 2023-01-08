#!/bin/bash
# ----
#
# Copyright (C) 2022, Huidong Zhang, Luyi Qu 
# ----

cat hostname.txt | while read hostname user ipaddr passwd
do
case ${hostname} in 
    client*)
    echo "Start benchmark on ${hostname} ${ipaddr} ..."
    ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "
    cd /root/Dike/run
    ./runBenchmark.sh ${PROPS}
    " &
    ;;
    *)
    exit 0
    ;;
esac
done