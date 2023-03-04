# How To Use Deploy Tools & Database Deploy Procedure
## Mysql v8.0
```bash
# install mysql server
wget https://dev.mysql.com/get/mysql80-community-release-el7-7.noarch.rpm
sudo yum install mysql80-community-release-el7-7.noarch.rpm
sudo yum-config-manager --disable mysql57-community
sudo yum-config-manager --enable mysql80-community
yum repolist enabled | grep "mysql.*-community.*"
sudo yum install mysql-community-server

# replace /etc/my.cnf with mysql config file template

# start mysqld service
mkdir -p /data/mysql/data
systemctl start mysqld

# login with root by initialized password
cat /var/log/mysqld.log | grep password
mysql -uroot -p

# change password/connection config, execute in mysql client
set global validate_password.policy=0;
set global validate_password.length=4;
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'YOUR_PASSWORD';
CREATE USER 'root'@'10.24.14.194' IDENTIFIED WITH mysql_native_password BY 'YOUR_PASSWORD';
GRANT ALL PRIVILEGES ON *.* to root@'10.24.14.194' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

## OceanBase v4.0.0.0
1. We provide 3/6/9/12/15-node config files in deploy/oceanbase/ob*node.yaml.

```bash
# install oceanbase deploy tool obd
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://mirrors.aliyun.com/oceanbase/OceanBase.repo
sudo yum install -y ob-deploy
source /etc/profile.d/obd.sh

# deploy & start oceanbase cluster
obd cluster deploy clustername -c ob9node.yaml
obd cluster start clustername

```

```bash
# login with root@sys
mysql --host *.*.*.* --port 2883 -uroot@sys
```

```sql
-- check cluster cpu remain
SELECT min(CPU_CAPACITY - CPU_ASSIGNED) AS cpu-remain FROM oceanbase.GV$OB_SERVERS

-- check cluster memory remain
SELECT floor(min(MEM_CAPACITY - MEM_ASSIGNED) / (1024 * 1024)) AS memory-remain FROM oceanbase.GV$OB_SERVERS;

-- check cluster log disk remain
SELECT floor(min(LOG_DISK_CAPACITY - LOG_DISK_ASSIGNED) / (1024 * 1024)) as disk-remain FROM oceanbase.GV$OB_SERVERS;

-- create resource unit, resource pool and test tenant
CREATE RESOURCE UNIT testunit
    max_cpu = cpu-remain,
    memory_size = memory-remain M,
    log_disk_size = disk-remain M;

CREATE RESOURCE POOL testpool
    UNIT = 'testunit',
    UNIT_NUM = 3,
    ZONE_LIST = ('zone0', 'zone1', 'zone2');

CREATE TENANT IF NOT EXISTS test 
    charset='utf8mb4', 
    replica_num=3, 
    zone_list=('zone0','zone1','zone2'), 
    primary_zone='RANDOM', 
    resource_pool_list=('testpool');

ALTER TENANT test SET VARIABLES ob_tcp_invited_nodes='%';
```

```bash
# login with root@test
mysql --host *.*.*.* --port 2883 -uroot@test
```

```sql
-- enable load in file
set global secure_file_priv="";
```


## TiDB v6.0.0
1. We provide 3/6/9/12/15-node config files in deploy/tidb/tidb*node.yaml.
2. We provide a script to create partitioned tables and suitable data placement policy in deploy/tidb/genSchema.sh.
3. We provide haproxy template for tidb in deploy/tidb/haproxy.cfg.
```bash
# create schemas and data placement policy with the given racks(number of servers in the cluster), warehouses(number of warehouses), partitions(number of target partitions)
./genSchema.sh RACK_NUM WAREHOUSE_NUM PARTITION_NUM

# install tidb deploy tool tiup
curl --proto '=https' --tlsv1.2 -sSf https://tiup-mirrors.pingcap.com/install.sh | sh
source .bash_profile
tiup cluster

# deploy & start tidb cluster
tiup cluster deploy tidb209 v6.0.0 tidb9node.yaml -p
tiup cluster start tidb209

# install haproxy
sudo yum install -y haproxy

# start haproxy on the client
nohup haproxy -f haproxy.cfg &
```

## Postgresql v14.0
```bash
# install postgresql server
sudo yum install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm
sudo yum install -y postgresql14-server

# make new data directory
mkdir /data/pg14
chown -R postgres:postgres /data/pg14
chmod u=rwx,g=rw /data/pg14 

# change data path, Environment=PGDATA=/data/pg14/
vim /usr/lib/systemd/system/postgresql-14.service

# start postgresql-14 service
sudo /usr/pgsql-14/bin/postgresql-14-setup initdb
sudo systemctl enable postgresql-14
sudo systemctl start postgresql-14

# modify config file according to Postgresql Config File Template (deploy/postgresql/pg_hba.conf, deploy.postgresql/postgresql.conf)
# path: /data/pg14/pg_hba.conf, /data/pg14/postgresql.conf
sudo systemctl restart postgresql-14
```
## CockroachDB v22.1.1
1. We provide 3/6/9/12/15-node config files in deploy/cockroachdb/crdb*node.yaml.
2. We provide a script to create partitioned tables and suitable data placement policy in deploy/cockroachdb/genSchema.sh.

```bash
# install haproxy
sudo yum install -y haproxy

# create schemas and data placement policy with the given racks(number of servers in the cluster), warehouses(number of warehouses), partitions(number of target partitions)
./genSchema.sh RACK_NUM WAREHOUSE_NUM PARTITION_NUM

# deploy cockroachdb with the given config file
./crdbDeploy.sh crdb9node.yaml

# stop cockroachdb cluster 
./crdbStop.sh crdb9node.yaml

# start cockroachdb cluster
./crdbStart.sh crdb9node.yaml

# destroy cockroachdb cluster
./crdbDestroy crdb9node.yaml
```