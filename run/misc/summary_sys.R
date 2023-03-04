# ----
# R graph to show the summary information of the specific system in the cluster.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----

# Read the system info csv file
run_prop <- read.csv("run.properties", sep="=", head=FALSE)
server_list <- as.character(run_prop[which(run_prop$V1 == "osCollectorSSHAddr"), "V2"])
server_list <- strsplit(server_list, ",")[[1]]
server_num <- length(server_list)

io_name <- as.character(run_prop[which(run_prop$V1 == "osCollectorDevices"), "V2"])
io_name <- strsplit(io_name, ",")[[1]]
net_name <- io_name[1]
disk_name <- io_name[2]

data_dir <- "data"
disk_dir <- paste(disk_name, ".csv", sep="")
net_dir <- paste(net_name, ".csv", sep="")
sys_dir <- "sys_info.csv"

# Used for aggregation of 10 indicator of system resource usage
info_avg <- array(1:server_num * 10, dim=c(server_num, 10))
info_avg_str <- array(1:server_num * 10, dim=c(server_num, 10))
info_95th <- array(1:server_num * 10, dim=c(server_num, 10))
info_95th_str <- array(1:server_num * 10, dim=c(server_num, 10))
info_99th <- array(1:server_num * 10, dim=c(server_num, 10))
info_99th_str <- array(1:server_num * 10, dim=c(server_num, 10))
info_max <- array(1:server_num * 10, dim=c(server_num, 10))
info_max_str <- array(1:server_num * 10, dim=c(server_num, 10))

# Travel throughout data dir of each node
idx <- 1
for (server in server_list) {
	data_server_dir <- paste(data_dir, server, sep="/")
	if (!dir.exists(data_server_dir)) {
		next
	}
	disk_info_dir <- paste(data_server_dir, disk_dir, sep="/")
	net_info_dir <- paste(data_server_dir, net_dir, sep="/")
	sys_info_dir <- paste(data_server_dir, sys_dir, sep="/")
	if (!file.exists(disk_info_dir)) {
		stop(paste(server, "block csv data missing"))
	}
	if (!file.exists(net_info_dir)) {
		stop(paste(server, "net csv data missing"))
	}
	if (!file.exists(sys_info_dir)) {
		stop(paste(server, "sys csv data missing"))
	}

	disk_info <- read.csv(disk_info_dir, head=TRUE)
	net_info <- read.csv(net_info_dir, head=TRUE)
	sys_info <- read.csv(sys_info_dir, head=TRUE)
	mem_info <- (sys_info$mem_total - sys_info$mem_avail) / sys_info$mem_total * 100.0
	cpu_info <- sys_info$cpu_user * 100

	info_avg[idx, 1] <- mean(disk_info$rdiops, na.rm = TRUE)
	info_avg[idx, 2] <- mean(disk_info$rdkbps, na.rm = TRUE)
	info_avg[idx, 3] <- mean(disk_info$wriops, na.rm = TRUE)
	info_avg[idx, 4] <- mean(disk_info$wrkbps, na.rm = TRUE)
	info_avg[idx, 5] <- mean(net_info$rxpktsps, na.rm = TRUE)
	info_avg[idx, 6] <- mean(net_info$rxkbps, na.rm = TRUE)
	info_avg[idx, 7] <- mean(net_info$txpktsps, na.rm = TRUE)
	info_avg[idx, 8] <- mean(net_info$txkbps, na.rm = TRUE)
	info_avg[idx, 9] <- mean(mem_info, na.rm = TRUE)
	info_avg[idx, 10] <- mean(cpu_info, na.rm = TRUE)
	
	info_95th[idx, 1] <- c(quantile(disk_info$rdiops, probs=0.95))
	info_95th[idx, 2] <- c(quantile(disk_info$rdkbps, probs=0.95))
	info_95th[idx, 3] <- c(quantile(disk_info$wriops, probs=0.95))
	info_95th[idx, 4] <- c(quantile(disk_info$wrkbps, probs=0.95))
	info_95th[idx, 5] <- c(quantile(net_info$rxpktsps, probs=0.95))
	info_95th[idx, 6] <- c(quantile(net_info$rxkbps, probs=0.95))
	info_95th[idx, 7] <- c(quantile(net_info$txpktsps, probs=0.95))
	info_95th[idx, 8] <- c(quantile(net_info$txkbps, probs=0.95))
	info_95th[idx, 9] <- c(quantile(mem_info, probs=0.95))
	info_95th[idx, 10] <- c(quantile(cpu_info, probs=0.95))
	
	info_99th[idx, 1] <- c(quantile(disk_info$rdiops, probs=0.99))
	info_99th[idx, 2] <- c(quantile(disk_info$rdkbps, probs=0.99))
	info_99th[idx, 3] <- c(quantile(disk_info$wriops, probs=0.99))
	info_99th[idx, 4] <- c(quantile(disk_info$wrkbps, probs=0.99))
	info_99th[idx, 5] <- c(quantile(net_info$rxpktsps, probs=0.99))
	info_99th[idx, 6] <- c(quantile(net_info$rxkbps, probs=0.99))
	info_99th[idx, 7] <- c(quantile(net_info$txpktsps, probs=0.99))
	info_99th[idx, 8] <- c(quantile(net_info$txkbps, probs=0.99))
	info_99th[idx, 9] <- c(quantile(mem_info, probs=0.99))
	info_99th[idx, 10] <- c(quantile(cpu_info, probs=0.99))

	info_max[idx, 1] <- c(max(disk_info$rdiops, na.rm = TRUE))
	info_max[idx, 2] <- c(max(disk_info$rdkbps, na.rm = TRUE))
	info_max[idx, 3] <- c(max(disk_info$wriops, na.rm = TRUE))
	info_max[idx, 4] <- c(max(disk_info$wrkbps, na.rm = TRUE))
	info_max[idx, 5] <- c(max(net_info$rxpktsps, na.rm = TRUE))
	info_max[idx, 6] <- c(max(net_info$rxkbps, na.rm = TRUE))
	info_max[idx, 7] <- c(max(net_info$txpktsps, na.rm = TRUE))
	info_max[idx, 8] <- c(max(net_info$txkbps, na.rm = TRUE))
	info_max[idx, 9] <- c(max(mem_info, na.rm = TRUE))
	info_max[idx, 10] <- c(max(cpu_info, na.rm = TRUE))

	idx <- idx + 1
}

# Generate the system summary and write it to
# data/sys_summary.csv
info_name <- c(
	'rdiops',
	'rdKBps',
	'wriops',
	'wrKBps',
	'rxpktsps',
	'rxKBps',
	'txpktsps',
	'txKBps',
	'memusage',
	'cpuusage'
)

for (idx in 1:server_num) {
	info_avg_str[idx, ] <- c(
		sprintf("%.2f", info_avg[idx, 1]),
		sprintf("%.2fKB", info_avg[idx, 2]),
		sprintf("%.2f", info_avg[idx, 3]),
		sprintf("%.2fKB", info_avg[idx, 4]),
		sprintf("%.2f", info_avg[idx, 5]),
		sprintf("%.2fKB", info_avg[idx, 6]),
		sprintf("%.2f", info_avg[idx, 7]),
		sprintf("%.2fKB", info_avg[idx, 8]),
		sprintf("%.2f%%", info_avg[idx, 9]),
		sprintf("%.2f%%", info_avg[idx, 10])
	)

	info_95th_str[idx, ] <- c(
		sprintf("%.f", info_95th[idx, 1]),
		sprintf("%.2fKB", info_95th[idx, 2]),
		sprintf("%.f", info_95th[idx, 3]),
		sprintf("%.2fKB", info_95th[idx, 4]),
		sprintf("%.f", info_95th[idx, 5]),
		sprintf("%.2fKB", info_95th[idx, 6]),
		sprintf("%.f", info_95th[idx, 7]),
		sprintf("%.2fKB", info_95th[idx, 8]),
		sprintf("%.2f%%", info_95th[idx, 9]),
		sprintf("%.2f%%", info_95th[idx, 10])
	)

	info_99th_str[idx, ] <- c(
		sprintf("%.f", info_99th[idx, 1]),
		sprintf("%.2fKB", info_99th[idx, 2]),
		sprintf("%.f", info_99th[idx, 3]),
		sprintf("%.2fKB", info_99th[idx, 4]),
		sprintf("%.f", info_99th[idx, 5]),
		sprintf("%.2fKB", info_99th[idx, 6]),
		sprintf("%.f", info_99th[idx, 7]),
		sprintf("%.2fKB", info_99th[idx, 8]),
		sprintf("%.2f%%", info_99th[idx, 9]),
		sprintf("%.2f%%", info_99th[idx, 10])
	)

	info_max_str[idx, ] <- c(
		sprintf("%.f", info_max[idx, 1]),
		sprintf("%.2fKB", info_max[idx, 2]),
		sprintf("%.f", info_max[idx, 3]),
		sprintf("%.2fKB", info_max[idx, 4]),
		sprintf("%.f", info_max[idx, 5]),
		sprintf("%.2fKB", info_max[idx, 6]),
		sprintf("%.f", info_max[idx, 7]),
		sprintf("%.2fKB", info_max[idx, 8]),
		sprintf("%.2f%%", info_max[idx, 9]),
		sprintf("%.2f%%", info_max[idx, 10])
    )
}

name <- c("indicator")
for (idx in 1:server_num) {
	name <- append(name, paste("node", idx, "avg"))
	name <- append(name, paste("node", idx, "95th"))
	name <- append(name, paste("node", idx, "99th"))
	name <- append(name, paste("node", idx, "max"))
}

sys_info <- rbind(c(), info_name)
for (idx in 1:server_num) {
	sys_info <- rbind(sys_info, info_avg_str[idx, ], info_95th_str[idx, ], info_99th_str[idx, ], info_max_str[idx, ])
}
sys_info <- cbind(name, sys_info)

write.csv(sys_info, file = "data/summary_sys.csv", quote = FALSE, na = "N/A",
	row.names = FALSE)