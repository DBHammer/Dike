# ----
# R graph to show how many transactions each terminal finished.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----
library(ggplot2)

# Read the runInfo.csv file.
run_prop <- read.csv("run.properties", sep="=", head=FALSE)
run_min <- as.numeric(as.character(run_prop[which(run_prop$V1 == "runMins"), "V2"]))
terminal <- as.numeric(as.character(run_prop[which(run_prop$V1 == "terminals"), "V2"]))

# Read the result.csv and then truncate data by second
raw_data <- read.csv("data/result.csv", head=TRUE)
valid_data <- raw_data[raw_data$rbk == "0" & raw_data$error == "0" & raw_data$elapsed <= run_min * 60000, ]
count_total <- setNames(
    aggregate(valid_data$elapsed, by=list(valid_data$terminalid, valid_data$txntype), FUN=NROW), 
    c('terminalid', 'txntype', 'count')
)

png("stackplot_terminal.png", width=@WIDTH@, height=@HEIGHT@)
par(mar=c(4,4,4,4), xaxp=c(10,200,19))

# Stack bar plot
ggplot(count_total) +
    aes(x=terminalid, y=count, fill=txntype) +
    geom_col(position='stack', width=0.6) +
    labs(x="Terminal ID", y="Transaction Count")
