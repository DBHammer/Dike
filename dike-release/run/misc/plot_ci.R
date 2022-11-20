# ----
# R graph to show conflict intensity per second.
# 
# Copyright (C) 2022, Huidong Zhang, Luyi Qu
# ----
library(ggplot2)

# Read conflict intensity from the ci.csv
resultCI <- read.csv("data/ci.csv", head=TRUE)

# Group the elapsed time by interval seconds
interval <- 1
idiv <- interval * 1000.0
resultCI$elapsed <- trunc(resultCI$elapsed / idiv)

# Initialize conflict intensity data frame
avgCI <- data.frame()
if (NROW(resultCI) != 0) {
	avgCI <- setNames(
		aggregate(resultCI, by=list(resultCI$elapsed), FUN=mean), 
		c('section', 'NA', 'avg')
	)
} else {
	avgCI <- data.frame(section=c(), avg=c())
}

png("plot_ci.png", width=@WIDTH@, height=@HEIGHT@)
par(mar=c(4,4,4,4), xaxp=c(10,200,19))

ggplot(avgCI) + 
	geom_line(aes(x=section * interval, y=avg)) +
	labs(x="Elapsed Seconds", y="Average CI") +
	theme(line=element_line(size=1.5)) +
	theme(legend.background=element_rect(fill="lightblue", size=0.5, linetype="solid", colour ="darkblue"))
