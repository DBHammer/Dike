#!/bin/bash
# ----
# Script to run sqls in sql file.
#
# Copyright (C) ...
# Copyright (C) 2022, Huidong Zhang, Luyi Qu 
# ----

# check command line usage
if [ $# -ne 2 ] ; then
    echo "Usage: $(basename $0) PROPS_FILE SQL_FILE_NAME" >&SQL_FILE_NAME
    exit 1
fi

source funcs.sh $1

SQL_FILE_NAME=$2
if [ -f "./sql.$(getProp db)/${SQL_FILE_NAME}.sql" ] ; then
    SQL_FILE="./sql.$(getProp db)/${SQL_FILE_NAME}.sql"
else
    echo "Error: cannot locate sql file for ${SQL_FILE_NAME}" >&SQL_FILE_NAME
    exit 2
fi

# set class path and parameters
myCP="../lib/*:../dist/*"
myOPTS="-Dprop=${PROPS} -DcommandFile=${SQL_FILE}"

# run sqls
if [ ${SQL_FILE_NAME} = "tableCreates" -o ${SQL_FILE_NAME} = "tableCreatesNoItem" -o ${SQL_FILE_NAME} = "tableCreatesNoTablegroup" ] ; then
    partitions=$(getProp partitions)
    sed -i "s/PARTITIONS [0-9]*;$/PARTITIONS ${partitions};/g" ${SQL_FILE}
    java -cp ${myCP} ${myOPTS} edu.ecnu.dike.jdbc.ExecJDBC
    if [ $? -ne 0 ] ; then
        echo "Error: get exception while executing sqls in ${SQL_FILE}, detailed information in log file" >&2
        exit 3
    fi
elif [ ${SQL_FILE_NAME} = "loadcsv" ] ; then
    # alter fileLocation according to 'fileLocation' in properties file
    fileLocation=$(getProp fileLocation)
    conn=$(getProp conn)
    sed -i "s|@fileLocation@|${fileLocation}|" ${SQL_FILE}
    if [ $(getProp db) = "oceanbase" ]; then
        sed -i "s|conn=.*$|conn=$(getProp rootServerConn)|" ${PROPS}
    fi
    java -cp ${myCP} ${myOPTS} edu.ecnu.dike.jdbc.ExecJDBC
    if [ $? -ne 0 ] ; then 
        echo "Error: get exception while executing sqls in ${SQL_FILE}, detailed information in log file" >&2
        sed -i "s|${fileLocation}|@fileLocation@|" ${SQL_FILE}
        if [ $(getProp db) = "oceanbase" ]; then
            sed -i "s|conn=.*$|conn=${conn}|" ${PROPS}
        fi
        exit 3
    fi
    sed -i "s|${fileLocation}|@fileLocation@|" ${SQL_FILE}
    if [ $(getProp db) = "oceanbase" ]; then
        sed -i "s|conn=.*$|conn=${conn}|" ${PROPS}
    fi
elif [ ${SQL_FILE_NAME} = "obproxyOptimal" -o ${SQL_FILE_NAME} = "loadOptimizeSys" -o ${SQL_FILE_NAME} = "benchmarkOptimizeSys" ] ; then
    # alter tenant into sys when benchmarking oceanbase
    set=$(getProp set) 
    sed -i -e "s/user=.*$/user=root@sys/" -e "s/set=.*$/set=oceanbase/" ${PROPS}
    java -cp ${myCP} ${myOPTS} edu.ecnu.dike.jdbc.ExecJDBC
    if [ $? -ne 0 ] ; then 
        sed -i -e "s/user=.*$/user=root@test/" -e "s/set=.*$/set=${set}/" ${PROPS}
        echo "Error: get exception while executing sqls in ${SQL_FILE}, detailed information in log file" >&2
        exit 3
    fi
    sed -i -e "s/user=.*$/user=root@test/" -e "s/set=.*$/set=${set}/" ${PROPS}
else
    java -cp ${myCP} ${myOPTS} edu.ecnu.dike.jdbc.ExecJDBC
    if [ $? -ne 0 ] ; then 
        echo "Error: get exception while executing sqls in ${SQL_FILE}, detailed information in log file" >&2
        exit 3
    fi
fi
