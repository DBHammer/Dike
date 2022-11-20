#!/bin/bash
# ----
# Script to run Dike benchmark and chaos test(if configured).
#
# Copyright (C) ... 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

if [ $# -ne 1 ] ; then
    echo "Usage: $(basename $0) PROPS_FILE" >&2
    exit 2
fi

function runChaos() {
    chaosNode=$(getProp chaosNode)
    chaosNodes=($(echo ${chaosNode} | tr "," "\n"))
    runMins=$(getProp runMins)
    chaosStartTime=$(getProp chaosTime)
    chaosDurationTime=$(((${runMins} - ${chaosStartTime}) * 60 + 10))
    if [ "$(getProp cpuLoad)" = "true" ] ; then
        sleep ${chaosStartTime}m
        for node in ${chaosNodes[@]} ; do
            ssh ${node} "cd /root/chaosblade && ./blade create cpu load --cpu-list 0-5 --timeout ${chaosDurationTime}"
        done
    elif [ "$(getProp stressMemory)" = "true" ] ; then
        sleep ${chaosStartTime}m
        for node in ${chaosNodes[@]} ; do
            ssh ${node} "cd /root/chaosblade && ./blade create mem load --timeout ${chaosDurationTime} --mode ram --mem-percent 100 --rate 1000"
        done
    elif [ "$(getProp diskRead)" = "true" ] ; then
        sleep ${chaosStartTime}m
        for node in ${chaosNodes[@]} ; do
            ssh ${node} "cd /root/chaosblade && ./blade create disk burn --timeout ${chaosDurationTime} --read --path /data"
        done
    elif [ "$(getProp diskWrite)" = "true" ] ; then
        sleep ${chaosStartTime}m
        for node in ${chaosNodes[@]} ; do
            ssh ${node} "cd /root/chaosblade && ./blade create disk burn --timeout ${chaosDurationTime} --write --path /data"
        done
    elif [ "$(getProp networkDelay)" = "true" ] ; then
        sleep ${chaosStartTime}m
        for node in ${chaosNodes[@]} ; do
            ssh ${node} "cd /root/chaosblade && ./blade create network delay --timeout ${chaosDurationTime} --time 50 --interface eth0"
        done
    elif [ "$(getProp datafile)" = "true" ] ; then
        sleep ${chaosStartTime}m
        for node in ${chaosNodes[@]} ; do
            case $(getProp db) in
                oceanbase)
                    ssh ${node} "rm -rf /data/obdata"
                    ;;
                tidb)
                    ssh ${node} "rm -rf /data/tidb-data/tikv-20160/db/*.sst"
                    ;;
                cockroachdb)
                    ssh ${node} "rm -rf /data/cockroachdb/*.sst"
                    ;;
            esac
        done
    elif [ "$(getProp shutdown)" = "true" ] ; then
        sleep ${chaosStartTime}m
        for node in ${chaosNodes[@]} ; do
            ssh ${node} "shutdown -h now"
        done
    fi
}

PROPS=$1
source funcs.sh ${PROPS}

myCP="../lib/*:../dist/*"
myOPTS="-Dprop=${PROPS}"

java -cp ${myCP} ${myOPTS} edu.ecnu.dike.control.Client &
runChaos
wait