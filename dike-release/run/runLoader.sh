#!/bin/bash
# ----
# Script to load data into database.
#
# Copyright (C) ...
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

if [ $# -lt 1 ] ; then
    echo "Usage: $(basename $0) PROPS_FILE [ARGS]" >&2
    exit 1
fi

# get properties file
PROPS=$1
if [ ! -f "${PROPS}" ] ; then
    echo "${PROPS}: no such file or directory" >&2
    exit 2
fi

source funcs.sh ${PROPS}

if [ $(getProp dataSource) = "csv" ] ; then
    ./runSQL.sh ${PROPS} "loadcsv"
    if [ $? -ne 0 ] ; then 
        echo "Error: fail to execute step loadcsv" >&2
        exit 3
    fi
else
    myCP="../lib/*:../dist/*"
    java -cp ${myCP} -Dprop=${PROPS} edu.ecnu.dike.data.LoadData
    if [ $? -ne 0 ] ; then 
        echo "Error: get exception while executing dataloader, detailed information in log file" >&2
        exit 3
    fi
fi