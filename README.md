# How Good is the Distributed Databases in Supporting Transaction Processing  
Dike is an open source project for benchmarking traditional relational databases together with NewSQL systems. Dike provides several features that are crucial for OLTP applications oriented systems, including quantitative distributed transactions/queries, uneven data distribution, dynamic workload and fault injection. Dike is forked from benchmarksql v5.0 and distributed under the GNU General Public License version 2.0 (GPLv2) license. The detailed [technique report](./Technique_Report.pdf) and [source code](dike-release) are open.  We show how to use Dike as follows. 


## Preparation
1. A client machine to run Dike benchmarking program and workload proxy (ObProxy for OceanBase cluster, Haproxy for TiDB/CockroachDB cluster). Proxy is unnecessary in the case of Mysql/PostgreSQL/Singleton-OceanBase/Singleton-TiDB/Singleton-CockroachDB. 
2. A cluster of 1/3/6/9/12/15 machines to deploy a database service. 
3. Modify [hostname](./dike-release/run/hostname.txt), remove redundant clients and servers, set correct user and password.

## How To Build Dike (CentOS 7)
1. Apache ivy is used to manage dependencies, ant is used to build the project and jdk8 is required.
```bash
yum install -y ant java-1.8* apache-ivy
```
2. Clone the project to the client machine and build the project.
```bash
yum install -y git
git clone https://github.com/DBHammer/Dike.git
cd ./dike-release/benchmarkStartSimple
ant resolve
ant
```
3. Install necessary tools, copy client ssh key to servers, adjust server parameters and synchronize clocks.
```bash
wget https://github.com/chaosblade-io/chaosblade/releases/download/v1.6.1/chaosblade-linux-amd64.tar.gz
tar -zxvf chaosblade-linux-amd64.tar.gz
sudo yum install -y sshpass
ssh-keygen -t rsa
./cluster_initialize.sh DATABASE_NAME [oceanbase/tidb/cockroachdb]
```

## How To Deploy Database Cluster (CentOS 7)
1. All deployment scripts and configuration files are placed in the deploy directory, refer to [rdeploy configuration](./dike-release/deploy/readme.md) for details about deployment procedure.

## How To Use
1. Connect to database server with mysql client or postgresql client and create database dike.
```sql
CREATE DATABASE IF NOT EXISTS dike;
```
2. Modify the parameters in the property file, details about the meaning of each parameter can be found in [configuration parameter](./dike-release/config/readme.md). 
3. Create tables and load data with the modified property file.
```bash
./runDatabaseBuild.sh ../config/oceanbase.properties
```
Otherwise, use the given template property files instead (also need to modify connection parameters first). Check [template](./dike-release/config/template/readme.md) for more details about each template properties file.
```bash
./runDatabaseBuild.sh ../config/template/oceanbase/dike.properties
```

## How To Generate Report
Though a detailed report is outputted to standard output stream at the end of benchmarking, more detailed statistical indicators such as throughput per second and system resource usage are also provided.
1. Install R language tool.
```bash
sudo yum install -y R
R
```
```R
# install R packages
install.packages('ggplot2')
install.packages('gridExtra')
install.packages('dplyr')
```
2. Suppose the result directory is 'results/oceanbase/dike/my_result_%tY-%tm-%td_%tH%tM%tS', generate statistics reports by running generateResults.sh. Check [misc](./dike-release/run/misc/readme.md) for details about outputs.
```bash
./generateResults.sh results/oceanbase/dike/my_result_%tY-%tm-%td_%tH%tM%tS
```
