#!/bin/bash
# ----
# Script to generate the detail results of a Dike run.
#
# Copyright (C) 2016, Denis Lussier
# Copyright (C) 2016, Jan Wieck
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

if [ $# -lt 1 ] ; then
    echo "Usage: $(basename $0) RESULT_DIR [...]" >&2
    exit 1
fi

WIDTH=2400
HEIGHT=600

SUMMARYS="summary_txn summary_sys plot_tps plot_latency plot_sys boxplot_sys stackplot_terminal plot_ci"

for resdir in $* ; do
    cd "${resdir}" || exit 2
    for summary in ${SUMMARYS} ; do
		echo -n "Generating ${resdir} ${summary}... "
		echo ""
		out=$(sed -e "s/@WIDTH@/${WIDTH}/g" -e "s/@HEIGHT@/${HEIGHT}/g" \
			<../../../../misc/${summary}.R | R --no-save)
		if [ $? -ne 0 ] ; then
			echo "Error: ${out}" >&2
			exit 3
		fi
    done
    cd ..
done
