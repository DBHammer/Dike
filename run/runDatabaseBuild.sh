#!/bin/bash
# ----
# Script to create schemas and load data for Dike.
#
# Copyright (C) ...
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

if [ $# -lt 1 ] ; then
    echo "Usage: $(basename $0) PROPS" >&2
    exit 1
fi

# get properties file
PROPS=$1
if [ ! -f "${PROPS}" ] ; then
    echo "${PROPS}: no such file or directory" >&2
    exit 2
fi

source funcs.sh ${PROPS}

schema=$(getProp schemaScript)
index=$(getProp indexScript)

case "$(getProp db)" in
    mysql)
        BEFORE_LOAD="${schema}"
        AFTER_LOAD="indexCreates buildFinish"
        ;;
    oceanbase)
        BEFORE_LOAD="${schema} obproxyOptimal loadOptimizeTest loadOptimizeSys"
        AFTER_LOAD="indexCreates benchmarkOptimizeTest benchmarkOptimizeSys"
        ;;
    tidb)
        BEFORE_LOAD="${schema} optimize"
        AFTER_LOAD=""
        ;;
    polardb)
        BEFORE_LOAD="${schema}"
        AFTER_LOAD="indexCreates"
        ;;
    postgres)
        BEFORE_LOAD="${schema}"
        AFTER_LOAD="indexCreates buildFinish"
        ;;
    citus)
        BEFORE_LOAD="${schema}"
        AFTER_LOAD="indexCreates buildFinish"
        ;;	
    cockroachdb)
        BEFORE_LOAD="optimize ${schema} ${index}"
        AFTER_LOAD=""
esac

for step in ${BEFORE_LOAD} ; do
    ./runSQL.sh ${PROPS} ${step}
    if [ $? -ne 0 ] ; then 
        echo "Error: fail to execute step ${step}" >&2
        exit 3
    fi
done

./runLoader.sh ${PROPS}
if [ $? -ne 0 ] ; then
    echo "Error: fail to load data into database" >&2
    exit 4
fi

for step in ${AFTER_LOAD} ; do
    ./runSQL.sh ${PROPS} ${step}
    if [ $? -ne 0 ] ; then
        echo "Error: fail to execute step ${step}" >&2
        exit 3
    fi
done
