# ----
# R graph to show conflict intensity per second.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----
library(ggplot2)

# Read conflict intensity from the ci.csv
resultCI <- read.csv("data/ci.csv", head=TRUE)

# Read the run.properties file.
run_prop <- read.csv("run.properties", sep="=", head=FALSE)
run_min <- as.numeric(as.character(run_prop[which(run_prop$V1 == "runMins"), "V2"]))
xmax <- run_min * 60

# Group the elapsed time by interval seconds
interval <- 1
idiv <- interval * 1000.0
resultCI$elapsed <- trunc(resultCI$elapsed / idiv)


# Initialize conflict intensity data frame
if (NROW(resultCI) != 0) {
	avgCI <- setNames(
		aggregate(resultCI, by=list(resultCI$elapsed), FUN=mean), 
		c('section', 'avg')
	)
} else {
	avgCI <- data.frame(section=seq(1, xmax, interval), avg=c(0))
}

png("plot_ci.png", width=@WIDTH@, height=@HEIGHT@)
par(mar=c(4,4,4,4), xaxp=c(10,200,19))

ggplot(avgCI) + 
	geom_line(aes(x=section * interval, y=avg)) +
	labs(x="Elapsed Seconds", y="Average CI") +
	theme(line=element_line(linewidth=1.5)) +
	theme(legend.background=element_rect(fill="lightblue", linewidth=2, linetype="solid", colour ="darkblue"))
