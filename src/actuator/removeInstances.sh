#!/bin/bash
machineuser=ubuntu
for i in "$@"
do
ssh  $machineuser@$i "/home/ubuntu/cassandra/bin/nodetool -h $i decommission"
ssh $machineuser@$i "sudo rm -rf /var/lib/cassandra/*"
pid=$(ssh $machineuser@$i "ps aux | grep '[c]assandra'" | awk '{print $2}')
ssh $machineuser@$i "kill -9 $pid"
done