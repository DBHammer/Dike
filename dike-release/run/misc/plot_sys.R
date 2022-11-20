# ----
# R graph to show the detail information of the specific machine in the cluster.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----
library(ggplot2)
library(gridExtra)

# Place the multiple plot together
multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  require(grid)
  png("plot_sys.png", type = "cairo", width=@WIDTH@, height=@HEIGHT@)
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

# Read properties from the runInfo.csv file
run_prop <- read.csv("run.properties", sep="=", head=FALSE)
run_min <- as.numeric(as.character(run_prop[which(run_prop$V1 == "run_min"),"V2"]))
server_list <- run_prop[which(run_prop$V1 == "osCollectorSSHAddr"),"V2"]
server_list <- strsplit(server_list, ",")[[1]]
io_name <- strsplit(run_prop[which(run_prop$V1 == "osCollectorDevices"), "V2"], ",")[[1]]
net_name <- io_name[1]
disk_name <- io_name[2]
data_dir <- "data"
disk_dir <- paste(disk_name, ".csv", sep="")
net_dir <- paste(net_name, ".csv", sep="")
sys_dir <- "sys_info.csv"

# Initialize system resource information list
ip_info <- c()
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

# Read data from directory of each client/server
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

  # Append system resource of each node to lists
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
  ip_info <- append(ip_info, rep(server, length(sys_info$cpu_user)))
}

info_frame <- data.frame(ip = as.factor(ip_info),
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
mem_plot <- ggplot(info_frame, aes(x=second, y=memory, color=ip)) + 
geom_line() +  
labs(title="Memory Per Machine", x="Second", y="Memory Usage (%)")
# cpu
cpu_plot <- ggplot(info_frame, aes(x=second, y=cpu, color=ip)) + 
geom_line() +  
labs(title="CPU Per Machine", x="Second", y="CPU Usage (%)")
# rdiops
rdiops_plot <- ggplot(info_frame, aes(x=second, y=rdiops, color=ip)) + 
geom_line() +  
labs(title="Rdiops Per Machine", x="Second", y="Rdiops")
# rdkbps
rdkbps_plot <- ggplot(info_frame, aes(x=second, y=rdkbps, color=ip)) + 
geom_line() +  
labs(title="Rdkbps Per Machine", x="Second", y="Rdkbps (KB)") 
# wriops
wriops_plot <- ggplot(info_frame, aes(x=second, y=wriops, color=ip)) + 
geom_line() +  
labs(title="Wriops Per Machine", x="Second", y="Wriops") 
# wrkbps
wrkbps_plot <- ggplot(info_frame, aes(x=second, y=wrkbps, color=ip)) + 
geom_line() +  
labs(title="Wrkbps Per Machine", x="Second", y="Wrkbps (KB)") 
# rxpktsps
rxpktsps_plot <- ggplot(info_frame, aes(x=second, y=rxpktsps, color=ip)) + 
geom_line() +  
labs(title="Rxpktsps Per Machine", x="Second", y="Rxpktsps") 
# rxkbps
rxkbps_plot <- ggplot(info_frame, aes(x=second, y=rxkbps, color=ip)) + 
geom_line() +  
labs(title="Rxkbps Per Machine", x="Second", y="Rxkbps (KB)") 
# txpktsps
txpktsps_plot <- ggplot(info_frame, aes(x=second, y=txpktsps, color=ip)) + 
geom_line() +  
labs(title="Txpktsps Per Machine", x="Second", y="Txpktsps")
# txkbps
txkbps_plot <- ggplot(info_frame, aes(x=second, y=txkbps, color=ip)) + 
geom_line() +  
labs(title="Txkbps Per Machine", x="Second", y="Txkbps (KB)")

multiplot(mem_plot, cpu_plot, rdiops_plot, rdkbps_plot, wriops_plot, wrkbps_plot, rxpktsps_plot, rxkbps_plot, txpktsps_plot, txkbps_plot, cols=5)