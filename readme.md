# Dike: A Benchmark Suite for Distributed Transactional Databases
Dike is a new benchmark suite for benchmarking distributed transactional databases (DDBMSs), which is extended from the popular TPC-C benchmark. Dike provides several features that are crucial for DDBMSs, including quantitative distributed transactions/queries, imbalanced data and workload distribution, dynamic load variation and fault injection. Details about our designs of the aforementioned features and experiments on three open-source DDBMSs ([OceanBase](https://www.oceanbase.com/), [TiDB](https://docs.pingcap.com/zh/tidb/stable), [CockroachDB](https://www.cockroachlabs.com/)) can be found in the [Technique Report](./doc/report/Technique_Report.pdf). We also provide a front-end and back-end interactive [tool](./dike-demo/) to demonstrate the benchmark suite ([video](./doc/demo/dike.mp4)). We show how to use Dike as follows.

Dike project is forked from benchmarksql v5.0 and distributed under the GNU General Public License version 2.0 (GPLv2) license. 

## Preparation
1. A client machine to run Dike benchmarking program and workload proxy (ObProxy for OceanBase cluster, Haproxy for TiDB/CockroachDB cluster). Proxy is unnecessary in the case of Mysql/PostgreSQL/Singleton-OceanBase/Singleton-TiDB/Singleton-CockroachDB. 
2. A cluster of 1/3/6/9/12/15 machines to deploy database service. 
3. Modify [hostname](run/hostname.txt), remove redundant clients and servers, set correct user and password (make sure that client0 and server1 exist).

## How To Build Dike (CentOS 7)
1. Apache ivy is used to manage dependencies, ant is used to build the project and jdk8 is required.
```bash
yum install -y ant java-1.8* apache-ivy
```
2. Clone the project to the client machine and build the project.
```bash
yum install -y git
git clone https://github.com/DBHammer/Dike --recursive
cd Dike
ant resolve
ant
```
3. Install necessary tools, copy client ssh key to servers, adjust server parameters and synchronize clocks.
```bash
wget https://github.com/chaosblade-io/chaosblade/releases/download/v1.6.1/chaosblade-linux-amd64.tar.gz
tar -zxvf chaosblade-linux-amd64.tar.gz
sudo yum install -y sshpass
ssh-keygen -t rsa
# make sure that you can connect to the client and servers in root user
# copy ssh key from client to servers
./cluster_initialize.sh PROPERTIES_FILE copySshKey
# install chaosblade on servers for fault injection
./cluster_initialize.sh PROPERTIES_FILE installChaosblade
# configure system parameters for client and servers
./cluster_initialize.sh PROPERTIES_FILE configureSystem
# synchronize ntp clock in servers (change network segment first)
./cluster_initialize.sh PROPERTIES_FILE ntpSynchronize
# copy os_collector script to servers
./cluster_initialize.sh PROPERTIES_FILE copyOsCollector
```

## How To Deploy Database Cluster (CentOS 7)
1. All deployment scripts and config files are placed in the deploy directory, refer to [readme](deploy/readme.md) for details about deployment procedure.

## How To Use
1. Connect to database server with mysql client or psql and create database dike.
```sql
CREATE DATABASE IF NOT EXISTS dike;
```
2. Modify the parameters in the property file, details about the meaning of each parameter can be found in [readme](config/readme.md). 
3. Create tables and load in data with the modified property file.
```bash
./runDatabaseBuild.sh ../config/oceanbase.properties
```
Otherwise, use the given template property files instead (also need to modify connection parameters first). Check [template](config/template/readme.md) for more details about each template properties file.
```bash
./runDatabaseBuild.sh ../config/template/oceanbase/dike.properties
```

## How To Generate Report
Though a brief report is printed to standard output stream at the end of benchmark, more detailed statistical indicators such as throughput per second and system resource usage are also provided.
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
2. Suppose the result directory is 'results/oceanbase/dike/my_result_%tY-%tm-%td_%tH%tM%tS', generate statistics reports by running generateResults.sh. Check [misc](run/misc/readme.md) for details about outputs.
```bash
./generateResults.sh results/oceanbase/dike/my_result_%tY-%tm-%td_%tH%tM%tS
```
