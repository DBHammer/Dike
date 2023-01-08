# ----
# R graph to show the summary information of all transaction types.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

# Read run minutes from the runInfo.csv file
run_prop <- read.csv("run.properties", sep="=", head=FALSE)
run_min <- as.numeric(as.character(run_prop[which(run_prop$V1 == "runMins"), "V2"]))

# Read the result.csv and group by transaction type
raw_data <- read.csv("data/result.csv", head=TRUE)
valid_data <- raw_data[raw_data$rbk == "0" & raw_data$error == "0" & raw_data$elapsed <= run_min * 60000, ]
new_order <- valid_data[valid_data$txntype == 'NEW_ORDER', ]
payment <- valid_data[valid_data$txntype == 'PAYMENT', ]
order_status <- valid_data[valid_data$txntype == 'ORDER_STATUS', ]
stock_level <- valid_data[valid_data$txntype == 'STOCK_LEVEL', ]
delivery <- valid_data[valid_data$txntype == 'DELIVERY', ]
update_item <- valid_data[valid_data$txntype == 'UPDATE_ITEM', ]
update_stock <- valid_data[valid_data$txntype == 'UPDATE_STOCK', ]
global_snapshot <- valid_data[valid_data$txntype == 'GLOBAL_SNAPSHOT', ]
global_deadlock <- valid_data[valid_data$txntype == 'GLOBAL_DEADLOCK', ]

# Generate the transaction summary and write it to
# data/tx_summary.csv
tx_total <- NROW(valid_data)
tx_name <- c(
	'NEW_ORDER',
	'PAYMENT',
	'ORDER_STATUS',
	'STOCK_LEVEL',
	'DELIVERY',
	'UPDATE_ITEM',
	'UPDATE_STOCK',
	'GLOBAL_SNAPSHOT',
	'GLOBAL_DEADLOCK',
	'TOTAL'
)

tx_count <- c(
	NROW(new_order),
	NROW(payment),
	NROW(order_status),
	NROW(stock_level),
	NROW(delivery),
	NROW(update_item),
	NROW(update_stock),
	NROW(global_snapshot),
	NROW(global_deadlock),
	tx_total
)

tx_percent <- c(
	sprintf("%.3f%%", NROW(new_order) / tx_total * 100.0),
	sprintf("%.3f%%", NROW(payment) / tx_total * 100.0),
	sprintf("%.3f%%", NROW(order_status) / tx_total * 100.0),
	sprintf("%.3f%%", NROW(stock_level) / tx_total * 100.0),
	sprintf("%.3f%%", NROW(delivery) / tx_total * 100.0),
	sprintf("%.3f%%", NROW(update_item) / tx_total * 100.0),
	sprintf("%.3f%%", NROW(update_stock) / tx_total * 100.0),
	sprintf("%.3f%%", NROW(global_snapshot) / tx_total * 100.0),
	sprintf("%.3f%%", NROW(global_deadlock) / tx_total * 100.0),
	NA
)
	
tx_min <- c(
	if (NROW(new_order)) sprintf("%.fms", min(new_order$latency, na.rm = TRUE)) else NA,
	if (NROW(payment)) sprintf("%.fms", min(payment$latency, na.rm = TRUE)) else NA,
	if (NROW(order_status)) sprintf("%.fms", min(order_status$latency, na.rm = TRUE)) else NA,
	if (NROW(stock_level)) sprintf("%.fms", min(stock_level$latency, na.rm = TRUE)) else NA,
	if (NROW(delivery)) sprintf("%.fms", min(delivery$latency, na.rm = TRUE)) else NA,
	if (NROW(update_item)) sprintf("%.fms", min(update_item$latency, na.rm = TRUE)) else NA,
	if (NROW(update_stock)) sprintf("%.fms", min(update_stock$latency, na.rm = TRUE)) else NA,
	if (NROW(global_snapshot)) sprintf("%.fms", min(global_snapshot$latency, na.rm = TRUE)) else NA,
	if (NROW(global_deadlock)) sprintf("%.fms", min(global_deadlock$latency, na.rm = TRUE)) else NA,
	if (NROW(valid_data)) sprintf("%.fms", min(valid_data$latency, na.rm = TRUE)) else NA
)

tx_avg <- c(
	sprintf("%.3fms", mean(new_order$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(payment$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(order_status$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(stock_level$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(delivery$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(update_item$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(update_stock$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(global_snapshot$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(global_deadlock$latency, na.rm = TRUE)),
	sprintf("%.3fms", mean(valid_data$latency, na.rm = TRUE))
)

tx_50th <- c(
	sprintf("%.fms", quantile(new_order$latency, probs=0.5)),
	sprintf("%.fms", quantile(payment$latency, probs=0.5)),
	sprintf("%.fms", quantile(order_status$latency, probs=0.5)),
	sprintf("%.fms", quantile(stock_level$latency, probs=0.5)),
	sprintf("%.fms", quantile(delivery$latency, probs=0.5)),
	sprintf("%.fms", quantile(update_item$latency, probs=0.5)),
	sprintf("%.fms", quantile(update_stock$latency, probs=0.5)),
	sprintf("%.fms", quantile(global_snapshot$latency, probs=0.5)),
	sprintf("%.fms", quantile(global_deadlock$latency, probs=0.5)),
	sprintf("%.fms", quantile(valid_data$latency, probs=0.5))
)

tx_99th <- c(
	sprintf("%.fms", quantile(new_order$latency, probs=0.99)),
	sprintf("%.fms", quantile(payment$latency, probs=0.99)),
	sprintf("%.fms", quantile(order_status$latency, probs=0.99)),
	sprintf("%.fms", quantile(stock_level$latency, probs=0.99)),
	sprintf("%.fms", quantile(delivery$latency, probs=0.99)),
	sprintf("%.fms", quantile(update_item$latency, probs=0.99)),
	sprintf("%.fms", quantile(update_stock$latency, probs=0.99)),
	sprintf("%.fms", quantile(global_snapshot$latency, probs=0.99)),
	sprintf("%.fms", quantile(global_deadlock$latency, probs=0.99)),
	sprintf("%.fms", quantile(valid_data$latency, probs=0.99))
)

tx_max <- c(
	if (NROW(new_order)) sprintf("%.fms", max(new_order$latency, na.rm = TRUE)) else NA,
	if (NROW(payment)) sprintf("%.fms", max(payment$latency, na.rm = TRUE)) else NA,
	if (NROW(order_status)) sprintf("%.fms", max(order_status$latency, na.rm = TRUE)) else NA,
	if (NROW(stock_level)) sprintf("%.fms", max(stock_level$latency, na.rm = TRUE)) else NA,
	if (NROW(delivery)) sprintf("%.fms", max(delivery$latency, na.rm = TRUE)) else NA,
	if (NROW(update_item)) sprintf("%.fms", max(update_item$latency, na.rm = TRUE)) else NA,
	if (NROW(update_stock)) sprintf("%.fms", max(update_stock$latency, na.rm = TRUE)) else NA,
	if (NROW(global_snapshot)) sprintf("%.fms", max(global_snapshot$latency, na.rm = TRUE)) else NA,
	if (NROW(global_deadlock)) sprintf("%.fms", max(global_deadlock$latency, na.rm = TRUE)) else NA,
	if (NROW(valid_data)) sprintf("%.fms", max(valid_data$latency, na.rm = TRUE)) else NA
)

tx_rbk <- c(
	sprintf("%.3f%%", sum(new_order$rbk) / NROW(new_order) * 100.0),
	sprintf("%.3f%%", sum(payment$rbk) / NROW(payment) * 100.0),
	sprintf("%.3f%%", sum(order_status$rbk) / NROW(order_status) * 100.0),
	sprintf("%.3f%%", sum(stock_level$rbk) / NROW(stock_level) * 100.0),
	sprintf("%.3f%%", sum(delivery$rbk) / NROW(delivery) * 100.0),
	sprintf("%.3f%%", sum(update_item$rbk) / NROW(update_item) * 100.0),
	sprintf("%.3f%%", sum(update_stock$rbk) / NROW(update_stock) * 100.0),
	sprintf("%.3f%%", sum(global_snapshot$rbk) / NROW(global_snapshot) * 100.0),
	sprintf("%.3f%%", sum(global_deadlock$rbk) / NROW(global_deadlock) * 100.0),
	sprintf("%.3f%%", sum(valid_data$rbk) / tx_total * 100.0)
)

tx_error <- c(
	sum(new_order$error),
	sum(payment$error),
	sum(order_status$error),
	sum(stock_level$error),
	sum(delivery$error),
	sum(update_item$error),
	sum(update_stock$error),
	sum(global_snapshot$error),
	sum(global_deadlock$error),
	sum(valid_data$error)
)

tx_distributed <- c(
	sprintf("%.3f%%", sum(new_order$distributed) / NROW(new_order) * 100.0),
	NA,
	NA,
	sprintf("%.3f%%", sum(stock_level$distributed) / NROW(stock_level) * 100.0),
	NA,
	NA,
	NA,
	NA,
	NA,
	NA
)

tx_spannode <- c(
	sprintf("%.3f", sum(new_order$spannode) / NROW(new_order)),
	NA,
	NA,
	sprintf("%.3f", sum(stock_level$spannode) / NROW(stock_level)),
	NA,
	NA,
	NA,
	NA,
	NA,
	NA
)

tx_info <- data.frame(
	tx_name,
	tx_count,
	tx_percent,
	tx_min,
	tx_avg,
	tx_50th,
	tx_99th,
	tx_max,
	tx_rbk,
	tx_error,
	tx_distributed,
	tx_spannode
)

write.csv(tx_info, file = "data/summary_txn.csv", quote = FALSE, na = "N/A",
	row.names = FALSE)