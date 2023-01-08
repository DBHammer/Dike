# Properties Files in template/ Directory
These files provide the template configurations we used in the Experimental Evaluation part of [Paper Dike](https://github.com/TPGenerator/Dike). 
Dike.properties provides TPC-C like workloads, and on the basis of that, we provide pluggable parameters to customize the testing scenario.

## Experiments in Paper Dike
### For Standalone Database System(mysql/postgresql)
- dynamicConflict.properties(Section 6.2.5)

### For distributed database system(oceanbase/tidb/cockroachdb)
- distributedTransaction.properties(Section 6.1.1/6.2.1/6.2.4)
- dynamicConflict.properties(Section 6.1.2/6.2.5)
- dataImbalance.properties(Section 6.1.3/6.4.1)
- dynamicLoad.properties(Section 6.1.4)
- dynamicTransaction.properties(Section 6.1.4)
- distributedQuery.properties(Section 6.2.2/6.2.4)
- noTablegroup.properties(Section 6.2.3)
- noItem.properties(Section 6.2.3)
- dike.properties(Section 6.2.4)
- cpuLoad(Section 6.3)
- networkDelay.properties(Section 6.3)
- diskRead.properties(Section 6.3)
- diskWrite.properties(Section 6.3)
- shudown.properties(Section 6.3)
- hotSpot.properties(Section 6.4.2)