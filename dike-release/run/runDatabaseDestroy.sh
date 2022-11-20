#!/bin/bash
# ----
# Script to clear benchmark environment for Dike.
#
# Copyright (C) ...
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

if [ $# -ne 1 ] ; then
    echo "Usage: $(basename $0) PROPS" >&2
    exit 1
fi

PROPS=$1
if [ ! -f "${PROPS}" ] ; then
    echo "${PROPS}: no such file or directory" >&2
    exit 2
fi

STEPS="tableDrops"

for step in ${STEPS} ; do
    ./runSQL.sh "${PROPS}" ${step}
    if [ $? -ne 0 ] ; then 
        echo "Error: fail to execute step ${step}" >&2
        exit 3
    fi
done
