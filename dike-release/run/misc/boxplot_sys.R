# ----
# R graph to show the detail information of the specific machine in the cluster.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----
library(ggplot2)
library(gridExtra)

# Plot multiple figures in chart
multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  require(grid)
  png("boxplot_sys.png", type = "cairo", width=@WIDTH@, height=@HEIGHT@)
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  num_plots = length(plots)
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(num_plots/cols)),
                    ncol = cols, nrow = ceiling(num_plots/cols))
  }
  if (num_plots==1) {
    print(plots[[1]])
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    # Make each plot, in the correct location
    for (i in 1:num_plots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

# Automatically read server_list in the form of user@host from run.properties
run_prop <- read.csv("run.properties", sep="=", head=FALSE)
server_list <- as.character(run_prop[which(run_prop$V1 == "osCollectorSSHAddr"),"V2"])
server_list <- strsplit(server_list, ",")[[1]]

# Automatically read device name from run.properties
io_name <- strsplit(run_prop[which(run_prop$V1 == "osCollectorDevices"), "V2"], ",")[[1]]
net_name <- io_name[1]
disk_name <- io_name[2]
data_dir <- "data"
disk_dir <- paste(disk_name, ".csv", sep="")
net_dir <- paste(net_name, ".csv", sep="")
sys_dir <- "sys_info.csv"

# Initialize system resource information
ip_info <- c()
tag_info <- c()
cpu_info <- c()
mem_info <- c()
rdiops_info <- c()
rdkbps_info <- c()
wriops_info <- c()
wrkbps_info <- c()
rxpktsps_info <- c()
rxkbps_info <- c()
txpktsps_info <- c()
txkbps_info <- c()
second_info <- c()

# Read data from directoy of each client/server
idx <- 1
for (server in server_list) {
  data_server_dir <- paste(data_dir, server, sep="/")
  # Check the existence of data dir
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
  # Append metric data of each server
	disk_info <- read.csv(disk_info_dir, head=TRUE)
	net_info <- read.csv(net_info_dir, head=TRUE)
	sys_info <- read.csv(sys_info_dir, head=TRUE)
  mem_info <- append(mem_info, (sys_info$mem_total - sys_info$mem_avail) / sys_info$mem_total * 100.0)
  cpu_info <- append(cpu_info, sys_info$cpu_user * 100)
  rdiops_info <- append(rdiops_info, disk_info$rdiops)
  rdkbps_info <- append(rdkbps_info, disk_info$rdkbps)
  wriops_info <- append(wriops_info, disk_info$wriops)
  wrkbps_info <- append(wrkbps_info, disk_info$wrkbps)
  rxpktsps_info <- append(rxpktsps_info, net_info$rxpktsps)
  rxkbps_info <- append(rxkbps_info, net_info$rxkbps)
  txpktsps_info <- append(txpktsps_info, net_info$txpktsps)
  txkbps_info <- append(txkbps_info, net_info$txkbps)
  second_info <- append(second_info, sys_info$elapsed / 1000)
  tag_info <- append(tag_info, rep(idx, length(sys_info$cpu_user)))
  ip_info <- append(ip_info, rep(server, length(sys_info$cpu_user)))
  idx <- idx + 1
}

info_frame <- data.frame(ip = as.factor(ip_info),
                        tag = tag_info,
                        memory = mem_info, 
                        cpu = cpu_info, 
                        rdiops = rdiops_info, 
                        rdkbps = rdkbps_info, 
                        wriops = wriops_info,
                        wrkbps = wrkbps_info,
                        rxpktsps = rxpktsps_info,
                        rxkbps = rxkbps_info,
                        txpktsps = txpktsps_info,
                        txkbps = txkbps_info,
                        second = second_info
)

# Generate the boxplot of each metric in the cluster
# memory
mem_plot <- ggplot(info_frame, aes(x=tag, y=memory, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Memory Per Machine", x="Machine ID", y="Memory Usage (%)")
# cpu
cpu_plot <- ggplot(info_frame, aes(x=tag, y=cpu, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="CPU Per Machine", x="Machine ID", y="CPU Usage (%)")
# rdiops
rdiops_plot <- ggplot(info_frame, aes(x=tag, y=rdiops, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Rdiops Per Machine", x="Machine ID", y="Rdiops") 
# rdkbps
rdkbps_plot <- ggplot(info_frame, aes(x=tag, y=rdkbps, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Rdkbps Per Machine", x="Machine ID", y="Rdkbps (KB)") 
# wriops
wriops_plot <- ggplot(info_frame, aes(x=tag, y=wriops, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Wriops Per Machine", x="Machine ID", y="Wriops")
# wrkbps
wrkbps_plot <- ggplot(info_frame, aes(x=tag, y=wrkbps, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Wrkbps Per Machine", x="Machine ID", y="Wrkbps (KB)") 
# rxpktsps
rxpktsps_plot <- ggplot(info_frame, aes(x=tag, y=rxpktsps, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Rxpktsps Per Machine", x="Machine ID", y="Rxpktsps")
# rxkbps
rxkbps_plot <- ggplot(info_frame, aes(x=tag, y=rxkbps, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Rxkbps Per Machine", x="Machine ID", y="Rxkbps (KB)") 
# txpktsps
txpktsps_plot <- ggplot(info_frame, aes(x=tag, y=txpktsps, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Txpktsps Per Machine", x="Machine ID", y="Txpktsps") 
# txkbps
txkbps_plot <- ggplot(info_frame, aes(x=tag, y=txkbps, color=ip)) + 
stat_boxplot(geom = "errorbar", width=0.15) + 
geom_boxplot() +  
labs(title="Txkbps Per Machine", x="Machine ID", y="Txkbps (KB)") 
# multiplot
multiplot(mem_plot, cpu_plot, rdiops_plot, rdkbps_plot, wriops_plot, wrkbps_plot, rxpktsps_plot, rxkbps_plot, txpktsps_plot, txkbps_plot, cols=5)