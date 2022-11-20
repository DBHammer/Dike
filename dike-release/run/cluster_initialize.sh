#!/bin/bash
# ----
# Script to configure system limits.conf/sysctl.conf for different database,
#           achieve software-level clock synchronization services through NTP,
#           copy system resource collector script to servers and
#           install necessary tools in servers(e.g. chaosblade).
#
# Copyright (C) 2022, Huidong Zhang, Luyi Qu 
# ----

function copySshKey() {
    # copy local ssh keys to remote servers
    sshpass -p ${passwd} ssh-copy-id -o StrictHostKeyChecking=no -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} < /dev/null
    if [ $? -ne 0 ] ; then
        echo "Error: fail to copy ssh key to ${user}@${ipaddr}" >&2
        exit 2
    fi
}

# TODO make user parameterized (not just limited to root)
function installChaosblade() {
    # install chaosblade
    scp -r -o ConnectTimeout=${sshTimeout} /root/chaosblade ${user}@${ipaddr}:/root
    if [ $? -ne 0 ] ; then
        echo "Error: fail to install chaosblade in ${user}@${ipaddr}" >&2
        exit 3
    fi
}

function configureSystem() {
    # configure system limits.conf/sysctl.conf
    if [ ${TYPE} = "oceanbase" ] ; then
    # for oceanbase
    ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "
    cat <<-EOF > /etc/security/limits.conf
    root soft nofile 655350
    root hard nofile 655350
    root soft stack 20480
    root hard stack 20480
    root soft nproc 655360
    root hard nproc 655360
    root soft core unlimited
    root hard core unlimited
EOF"
    if [ $? -ne 0 ] ; then
        echo "Error: fail to configure limits.conf for ${user}@${ipaddr}" >&2
        exit 4
    fi
    ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "
    cat <<-EOF > /etc/sysctl.conf && sysctl -p
    fs.aio-max-nr = 1048576
    net.core.somaxconn = 2048
    net.core.netdev_max_backlog = 10000
    net.core.rmem_default = 16777216
    net.core.wmem_default = 16777216
    net.core.rmem_max = 16777216
    net.core.wmem_max = 16777216
    net.ipv4.ip_local_port_range = 3500 65535 
    net.ipv4.ip_forward=0
    net.ipv4.conf.default.rp_filter=1
    net.ipv4.conf.default.accept_source_route = 0
    net.ipv4.tcp_syncookies = 0
    net.ipv4.tcp_rmem = 4096 87380 16777216
    net.ipv4.tcp_wmem = 4096 65536 16777216
    net.ipv4.tcp_max_syn_backlog = 16384
    net.ipv4.tcp_fin_timeout = 15
    net.ipv4.tcp_max_syn_backlog = 16384
    net.ipv4.tcp_tw_reuse = 1
    net.ipv4.tcp_slow_start_after_idle=0
    vm.swappiness = 0
    vm.min_free_kbytes = 2097152
EOF"
    if [ $? -ne 0 ] ; then
        echo "Error: fail to configure sysctl.conf for ${user}@${ipaddr}" >&2
        exit 5
    fi
elif [ ${TYPE} = "tidb" ] ; then
    # for tidb
    ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "
    cat <<-EOF > /etc/security/limits.conf
    root soft nofile 1000000
    root hard nofile 1000000
    root soft stack 32768
    root hard stack 32768
EOF"
    if [ $? -ne 0 ] ; then
        echo "Error: fail to configure limits.conf for ${user}@${ipaddr}" >&2
        exit 4
    fi
    ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "
    cat <<-EOF > /etc/sysctl.conf && sysctl -p
    fs.file-max = 1000000
    net.core.somaxconn = 32768
    net.ipv4.tcp_syncookies = 0
    vm.overcommit_memory = 1
EOF"
    if [ $? -ne 0 ] ; then
        echo "Error: fail to configure sysctl.conf for ${user}@${ipaddr}" >&2
        exit 5
    fi
elif [ ${TYPE} = "cockroachdb" ] ; then
    # for cockroachdb
    ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "
    cat <<-EOF > /etc/security/limits.conf
    root soft nofile 500000
    root hard nofile 500000
EOF"
    if [ $? -ne 0 ] ; then
        echo "Error: fail to configure limits.conf for ${user}@${ipaddr}" >&2
        exit 4
    fi
fi
}

function ntpSynchronize() {
    # deploy ntp server/client
    if [ ${hostname} = "server1" ] ; then
    ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "
    cat << EOF >> /etc/ntp.conf
    restrict 10.24.5.0 mask 255.255.255.0
EOF"
    if [ $? -ne 0 ] ; then
        echo "Error: fail to configure ntp.conf for ntp server ${user}@${ipaddr}" >&2
        exit 6
    fi
    rootserver=${ipaddr}
elif [ ${hostname} = "client0" ] ; then
    client=${ipaddr}
else
    ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "
    cat << EOF >> /etc/ntp.conf && systemctl restart ntpd
    server ${rootserver}
EOF"
    if [ $? -ne 0 ] ; then
        echo "Error: fail to configure ntp.conf for ntp client ${user}@${ipaddr}" >&2
        exit 7
    fi
    fi
}

function copyOsCollector() {
    # copy os_collector_linux.py
    if [ ${hostname} != "0" ] ; then
        ssh -n -o ConnectTimeout=${sshTimeout} ${user}@${ipaddr} "mkdir -p /root/benchmarkStartSimple/run/misc/" &&
        scp -o ConnectTimeout=${sshTimeout} root@${client}:/root/benchmarkStartSimple/run/misc/os_collector_linux.py ${user}@${ipaddr}:/root/benchmarkStartSimple/run/misc/os_collector_linux.py
        if [ $? -ne 0 ] ; then
            echo "Error: fail to copy resource collector script to server ${user}@${ipaddr}" >&2
            exit 8
        fi
    fi
}

TYPE=$1
case ${TYPE} in
    oceanbase|tidb|cockroachdb)
	;;
    "")	echo "Error: missing database type, Usage: $(basename $0) DATABASE_TYPE" >&2
	exit 1
	;;
    *)	echo "Error: unsupported database type ${TYPE}, Options: [oceanbase|tidb|cockroachdb]" >&2
	exit 1
	;;
esac
wget https://github.com/chaosblade-io/chaosblade/releases/download/v1.6.1/chaosblade-1.6.1-linux-amd64.tar.gz && tar -xzf chaosblade-1.6.1-linux-amd64.tar.gz
mv chaosblade-1.6.1 /root/chaosblade

client=""
rootserver=""
sshTimeout=10
cat hostname.txt | while read hostname user ipaddr passwd
do
echo "Configuring ${hostname} ${ipaddr} ..."
copySshKey
installChaosblade
configureSystem
ntpSynchronize
copyOsCollector
done
