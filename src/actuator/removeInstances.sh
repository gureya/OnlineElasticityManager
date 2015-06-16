#!/bin/bash
machineuser=ubuntu
for i in "$@"
do
ssh  $machineuser@$i "./home/ubuntu/cassandra/bin/nodetool -h $i decommission"
done