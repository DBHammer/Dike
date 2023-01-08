#!/bin/bash
# ----
# Script with util functions.

# Copyright (C) ... 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu 
# ----

PROPS=$1

# get a config value from the properties file
function getProp() {
    grep "^$1=" ${PROPS} | sed -e "s/^$1=//"
}
 
# make sure that we support the database type in properties file
case "$(getProp db)" in
    mysql|oceanbase|tidb|polardb|postgres|citus|cockroachdb)
	;;
    "")	echo "Error: missing database tpye 'db' in ${PROPS}" >&2
	exit 1
	;;
    *)	echo "Error: unsupported database type '$(getProp db)' in ${PROPS}" >&2
	exit 2
	;;
esac
