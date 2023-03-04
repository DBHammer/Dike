# ----
# R graph to show the latency per second of each transaction type.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----
library(ggplot2)

# Read run time from the run.properties file.
run_prop <- read.csv("run.properties", sep="=", head=FALSE)
run_min <- as.numeric(as.character(run_prop[which(run_prop$V1 == "runMins"), "V2"]))

# Read the result.csv and then get transactions by txntype
raw_data <- read.csv("data/result.csv", head=TRUE)
valid_data <- raw_data[raw_data$rbk == "0" & raw_data$error == "0", ]
new_order <- valid_data[valid_data$txntype == "NEW_ORDER", ]
payment <- valid_data[valid_data$txntype == "PAYMENT", ]
order_status <- valid_data[valid_data$txntype == "ORDER_STATUS", ]
stock_level <- valid_data[valid_data$txntype == "STOCK_LEVEL", ]
delivery <- valid_data[valid_data$txntype == "DELIVERY", ]
update_item <- valid_data[valid_data$txntype == "UPDATE_ITEM", ]
update_stock <- valid_data[valid_data$txntype == "UPDATE_STOCK", ]
global_snapshot <- valid_data[valid_data$txntype == "GLOBAL_SNAPSHOT", ]
global_deadlock <- valid_data[valid_data$txntype == "GLOBAL_DEADLOCK", ]

# Elapsed and latency are collected in the form of millisecond
# Figure plot interval in the form of second
interval <- 1
idiv <- interval * 1000.0

# Group the elapsed time by interval seconds
valid_data$elapsed <- trunc(valid_data$elapsed / idiv)
new_order$elapsed <- trunc(new_order$elapsed / idiv)
payment$elapsed <- trunc(payment$elapsed / idiv)
order_status$elapsed <- trunc(order_status$elapsed / idiv)
stock_level$elapsed <- trunc(stock_level$elapsed / idiv)
delivery$elapsed <- trunc(delivery$elapsed / idiv)
update_item$elapsed <- trunc(update_item$elapsed / idiv)
update_stock$elapsed <- trunc(update_stock$elapsed / idiv)
global_snapshot$elapsed <- trunc(global_snapshot$elapsed / idiv)
global_deadlock$elapsed <- trunc(global_deadlock$elapsed / idiv)

# Initialize transaction average latency dataframe
avg_total <- data.frame()
avg_new_order <- data.frame()
avg_payment <- data.frame()
avg_order_status <- data.frame()
avg_stock_level <- data.frame()
avg_delivery <- data.frame()
avg_update_item <- data.frame()
avg_update_stock <- data.frame()
avg_global_snapshot <- data.frame()
avg_global_deadlock <- data.frame()

# Aggregate average latency of each txntype
if (NROW(valid_data) != 0) {
	avg_total <- setNames(
		aggregate(valid_data$latency, by=list(valid_data$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_total <- transform(avg_total, type=rep(c("TOTAL"), times=c(length(avg_total$section))))
} else {
	avg_total <- data.frame(section=c(), mean=c())
}

if (NROW(new_order) != 0) {
	avg_new_order <- setNames(
		aggregate(new_order$latency, by=list(new_order$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_new_order <- transform(avg_new_order, type=rep(c("NEW_ORDER"), times=c(length(avg_new_order$section))))
} else {
	avg_new_order <- data.frame(section=c(), mean=c())
}

if (NROW(payment) != 0) {
	avg_payment <- setNames(
		aggregate(payment$latency, by=list(payment$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_payment <- transform(avg_payment, type=rep(c("PAYMENT"), times=c(length(avg_payment$section))))
} else {
	avg_payment <- data.frame(section=c(), mean=c())
}

if (NROW(order_status) != 0) {
	avg_order_status <- setNames(
		aggregate(order_status$latency, by=list(order_status$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_order_status <- transform(avg_order_status, type=rep(c("ORDER_STATUS"), times=c(length(avg_order_status$section))))
} else {
	avg_order_status <- data.frame(section=c(), mean=c())
}

if (NROW(stock_level) != 0) {
	avg_stock_level <- setNames(
		aggregate(stock_level$latency, by=list(stock_level$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_stock_level <- transform(avg_stock_level, type=rep(c("STOCK_LEVEL"), times=c(length(avg_stock_level$section))))
} else {
	avg_stock_level <- data.frame(section=c(), mean=c())
}

if (NROW(delivery) != 0) {
	avg_delivery <- setNames(
		aggregate(delivery$latency, by=list(delivery$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_delivery <- transform(avg_delivery, type=rep(c("DELIVERY"), times=c(length(avg_delivery$section))))
} else {
	avg_delivery <- data.frame(section=c(), mean=c())
}

if (NROW(update_item) != 0) {
	avg_update_item <- setNames(
		aggregate(update_item$latency, by=list(update_item$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_update_item <- transform(avg_update_item, type=rep(c("UPDATE_ITEM"), times=c(length(avg_update_item$section))))
} else {
	avg_update_item <- data.frame(section=c(), mean=c())
}

if (NROW(update_stock) != 0) {
	avg_update_stock <- setNames(
		aggregate(update_stock$latency, by=list(update_stock$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_update_stock <- transform(avg_update_stock, type=rep(c("UPDATE_STOCK"), times=c(length(avg_update_stock$section))))
} else {
	avg_update_stock <- data.frame(section=c(), mean=c())
}

if (NROW(global_snapshot) != 0) {
	avg_global_snapshot <- setNames(
		aggregate(global_snapshot$latency, by=list(global_snapshot$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_global_snapshot <- transform(avg_global_snapshot, type=rep(c("GLOBAL_SNAPSHOT"), times=c(length(avg_global_snapshot$section))))
} else {
	avg_global_snapshot <- data.frame(section=c(), mean=c())
}

if (NROW(global_deadlock) != 0) {
	avg_global_deadlock <- setNames(
		aggregate(global_deadlock$latency, by=list(global_deadlock$elapsed), FUN=mean), 
		c('section', 'mean')
	)
	avg_global_deadlock <- transform(avg_global_deadlock, type=rep(c("GLOBAL_DEADLOCK"), times=c(length(avg_global_deadlock$section))))
} else {
	avg_global_deadlock <- data.frame(section=c(), mean=c())
}

png("plot_latency.png", width=@WIDTH@, height=@HEIGHT@)
par(mar=c(4,4,4,4), xaxp=c(10,200,19))

# Right bind all the average data together
# Since not all transactions exist in the benchmark, we only plot those exist
xmax = run_min * 60 / interval
count_bind <- 
	rbind(avg_total, avg_new_order, avg_payment, avg_order_status, avg_stock_level, avg_delivery, avg_update_item, avg_update_stock, avg_global_snapshot, avg_global_deadlock)
count_bind <- count_bind[count_bind$section <= xmax, ]

# Line plot
ggplot(count_bind) + 
	geom_line(aes(x=section * interval, y=mean, color=type)) +
	labs(x="Elapsed Seconds", y="Average Tps") +
	theme(line=element_line(linewidth=1.5)) +
	theme(legend.background=element_rect(fill="lightblue", linewidth=0.5, linetype="solid", colour ="darkblue")) +
	scale_colour_discrete("Transaction Type")
