# Statistics Report
1. summary_txn  
    Include the basic aggreation information of each transaction type, such as transaction count, percentile latency, rollback/error, distributed transaction rate/span node(only make sense for neworder and stocklevel).  
    The output report is data/summary_sys.csv.
2. summary_sys  
    Include the average/95th/99th/max values of disk read iops(rdiops), disk read KBps(rdkbps), disk write iops(wriops), disk write KBps(wrkbps), network receive packetps(rxpktsps), network receive KBps(rxkbps), network send packetps(txpktsps), network send KBps(txkbps), memory usage(memusage), cpu usage(cpuusage) for each node(client or server), ps is an abbreviation for per second.   
    The output report is data/summary_txn.csv.
3. plot_tps  
    Include the transaction count per second of NewOrder/Payment/OrderStatus/Delivery/StockLevel/UpdateItem/UpdateStock/GlobalSnapshot/GlobalDeadlock, and their total amount. If chaos test is chosen(cpuLoad, diskRead, diskWrite, networkDelay, shutdown), the tps before and after exceptions injection are marked on the chart.  
    The output report is plot_tps.png.
4. plot_latency  
    Include the transaction average latency of NewOrder/Payment/OrderStatus/Delivery/StockLevel/UpdateItem/UpdateStock/GlobalSnapshot/GlobalDeadlock, and the total average latency.  
    The output report is plot_latency.png.
6. plot_sys  
    Include rdiops, rdkbps, wriops, wrkbps, rxpktsps, rxkbps, txpktsps, txkbps, memusage, cpuusage for each node(client or server) per second.  
    The output report is plot_sys.png.
7. boxplot_sys   
    Include boxplot of rdiops, rdkbps, wriops, wrkbps, rxpktsps, rxkbps, txpktsps, txkbps, memusage, cpuusage for each node.  
    The output report is boxplot_sys.png.
8. stackplot_terminal  
    Include the stacking bar chart of how many transaction that each transaction finished.
    The output report is stackplot_terminal.png.
9. plot_ci  
    Include the conflict intensity per second if dynamicConflict is set to true.  
    The output report is plot_ci.png.