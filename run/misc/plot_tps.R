# ----
# R graph to show transaction per second of each transaction type.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----
library(ggplot2)
suppressMessages(library(dplyr, warn.conflict = FALSE, quietly = TRUE))

# Read the run.properties file.
run_prop <- read.csv("run.properties", sep="=", head=FALSE)
run_min <- as.numeric(as.character(run_prop[which(run_prop$V1 == "runMins"), "V2"]))

# Read the result.csv and then filter data by txntype
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

# Elapsed and latency collected in the form of millisecond
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

# Initialize the data frame for each transaction type
count_total <- data.frame()
count_new_order <- data.frame()
count_payment <- data.frame()
count_order_status <- data.frame()
count_stock_level <- data.frame()
count_delivery <- data.frame()
count_update_item <- data.frame()
count_update_stock <- data.frame()
count_global_snapshot <- data.frame()
count_global_deadlock <- data.frame()

# Aggregate the counts of each transaction type
if (NROW(valid_data) != 0) {
	# Calculate the count per second in a section
	count_total <- setNames(
		aggregate(valid_data$elapsed, by=list(valid_data$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_total$count <- count_total$count / interval
	# Add type column
	count_total <- transform(count_total, type=rep(c("TOTAL"), times=c(length(count_total$section))))
} else {
	count_total <- data.frame(section=c(), count=c())
}

if (NROW(new_order) != 0) {
	count_new_order <- setNames(
		aggregate(new_order$elapsed, by=list(new_order$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_new_order$count <- count_new_order$count / interval
	count_new_order <- transform(count_new_order, type=rep(c("NEW_ORDER"), times=c(length(count_new_order$section))))
} else {
	count_new_order <- data.frame(section=c(), count=c())
}

if (NROW(payment) != 0) {
	count_payment <- setNames(
		aggregate(payment$elapsed, by=list(payment$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_payment$count <- count_payment$count / interval
	count_payment <- transform(count_payment, type=rep(c("PAYMENT"), times=c(length(count_payment$section))))
} else {
	count_payment <- data.frame(section=c(), count=c())
}

if (NROW(order_status) != 0) {
	count_order_status <- setNames(
		aggregate(order_status$elapsed, by=list(order_status$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_order_status$count <- count_order_status$count / interval
	count_order_status <- transform(count_order_status, type=rep(c("ORDER_STATUS"), times=c(length(count_order_status$section))))
} else {
	count_order_status <- data.frame(section=c(), count=c())
}

if (NROW(stock_level) != 0) {
	count_stock_level <- setNames(
		aggregate(stock_level$elapsed, by=list(stock_level$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_stock_level$count <- count_stock_level$count / interval
	count_stock_level <- transform(count_stock_level, type=rep(c("STOCK_LEVEL"), times=c(length(count_stock_level$section))))
} else {
	count_stock_level <- data.frame(section=c(), count=c())
}

if (NROW(delivery) != 0) {
	count_delivery <- setNames(
		aggregate(delivery$elapsed, by=list(delivery$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_delivery$count <- count_delivery$count / interval
	count_delivery <- transform(count_delivery, type=rep(c("DELIVERY"), times=c(length(count_delivery$section))))
} else {
	count_delivery <- data.frame(section=c(), count=c())
}

if (NROW(update_item) != 0) {
	count_update_item <- setNames(
		aggregate(update_item$elapsed, by=list(update_item$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_update_item$count <- count_update_item$count / interval
	count_update_item <- transform(count_update_item, type=rep(c("UPDATE_ITEM"), times=c(length(count_update_item$section))))
} else {
	count_update_item <- data.frame(section=c(), count=c())
}

if (NROW(update_stock) != 0) {
	count_update_stock <- setNames(
		aggregate(update_stock$elapsed, by=list(update_stock$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_update_stock$count <- count_update_stock$count / interval
	count_update_stock <- transform(count_update_stock, type=rep(c("UPDATE_STOCK"), times=c(length(count_update_stock$section))))
} else {
	count_update_stock <- data.frame(section=c(), count=c())
}

if (NROW(global_snapshot) != 0) {
	count_global_snapshot <- setNames(
		aggregate(global_snapshot$elapsed, by=list(global_snapshot$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_global_snapshot$count <- count_global_snapshot$count / interval
	count_global_snapshot <- transform(count_global_snapshot, type=rep(c("GLOBAL_SNAPSHOT"), times=c(length(count_global_snapshot$section))))
} else {
	count_global_snapshot <- data.frame(section=c(), count=c())
}

if (NROW(global_deadlock) != 0) {
	count_global_deadlock <- setNames(
		aggregate(global_deadlock$elapsed, by=list(global_deadlock$elapsed), FUN=NROW), 
		c('section', 'count')
	)
	count_global_deadlock$count <- count_global_deadlock$count / interval
	count_global_deadlock <- transform(count_global_deadlock, type=rep(c("GLOBAL_DEADLOCK"), times=c(length(count_global_deadlock$section))))
} else {
	count_global_deadlock <- data.frame(section=c(), count=c())
}

png("plot_tps.png", width=@WIDTH@, height=@HEIGHT@)
par(mar=c(4,4,4,4), xaxp=c(10,200,19))

# Right bind all the count data together
# Since not all transactions exist in the benchmark, we only plot those exist
xmax <- run_min * 60 / interval
count_bind <- 
	rbind(count_total, count_new_order, count_payment, count_order_status, count_stock_level, count_delivery, count_update_item, count_update_stock, global_snapshot, global_deadlock)
# Remove the data that exceed the boundary
count_bind <- count_bind[count_bind$section <= xmax, ]

cpu_load <- as.character(run_prop[which(run_prop$V1 == "cpuLoad"), "V2"])
disk_read <- as.character(run_prop[which(run_prop$V1 == "disk_read"), "V2"])
disk_write <- as.character(run_prop[which(run_prop$V1 == "disk_write"), "V2"])
network_delay <- as.character(run_prop[which(run_prop$V1 == "networkDelay"), "V2"])
shutdown <- as.character(run_prop[which(run_prop$V1 == "shutdown"), "V2"])
chaos <- cpu_load=="true" || disk_read=="true" || disk_write=="true" || network_delay=="true" || shutdown=="true"
if (!is.na(chaos)) {
	chaos_time <- as.numeric(as.character(run_prop[which(run_prop$V1 == "chaosTime"), "V2"])) / run_min * xmax
	before_tps <- as.integer(mean(count_total$count[1:chaos_time]))
	after_count <- filter(count_total, section > chaos_time & !is.na(count))
	after_tps <- as.integer(mean(after_count$count))
	ymax <- max(count_total$count)
	ggplot(count_bind) + 
		geom_line(aes(x=section * interval, y=count, color=type)) +
		labs(x="Elapsed Seconds", y="Average Tps") +
		theme(line=element_line(linewidth=1.5)) +
		theme(legend.background=element_rect(fill="lightblue", linewidth=0.5, linetype="solid", colour="darkblue")) +
		scale_colour_discrete("Transaction Type") +
		annotate("text", x=chaos_time/2, y=ymax*1.1, size=5, family="serif", label=paste("avg tps: ", before_tps)) +
		annotate("text", x=(xmax+chaos_time)/2, y=ymax*1.1, size=5, family="serif", label=paste("avg tps: ", after_tps)) +
		annotate("segment", x=0, xend=chaos_time, y=ymax*1.02, yend=ymax*1.02, cex=.8, color="blue") + 
		annotate("segment", x=chaos_time, xend=xmax, y=ymax*1.02, yend=ymax*1.02, cex=.8, color="red")
} else {
	ggplot(count_bind) + 
		geom_line(aes(x=section * interval, y=count, color=type)) +
		labs(x="Elapsed Seconds", y="Average Tps") +
		theme(line=element_line(linewidth=1.5)) +
		theme(legend.background=element_rect(fill="lightblue", linewidth=0.5, linetype="solid", colour="darkblue")) +
		scale_colour_discrete("Transaction Type")
}
