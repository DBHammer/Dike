# Properties Files in template/ Directory
These files provide the template configurations we used in the Experimental Evaluation part of [Dike's Technique Report](https://github.com/DBHammer/Dike/Technique_Report.pdf). 
Dike.properties provides TPC-C like workloads, and on the basis of that, we provide pluggable parameters to customize the testing scenario.

## Experiments in Paper Dike
### For Standalone Database System(mysql/postgresql)
- dynamicConflict.properties(Section 6.3.5)

### For distributed database system(oceanbase/tidb/cockroachdb)
- distributedTransaction.properties(Section 6.2.1/6.3.1/6.3.4)
- dynamicConflict.properties(Section 6.2.2/6.3.5)
- dataImbalance.properties(Section 6.2.3/6.5.1)
- dynamicLoad.properties(Section 6.2.4)
- dynamicTransaction.properties(Section 6.2.4)
- distributedQuery.properties(Section 6.3.2/6.3.4)
- noTablegroup.properties(Section 6.3.3)
- noItem.properties(Section 6.3.3)
- dike.properties(Section 6.3.4)
- cpuLoad(Section 6.4)
- networkDelay.properties(Section 6.4)
- diskRead.properties(Section 6.4)
- diskWrite.properties(Section 6.4)
- shudown.properties(Section 6.4)
- hotSpot.properties(Section 6.5.2)
