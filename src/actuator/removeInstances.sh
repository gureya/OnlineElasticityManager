#!/bin/bash
machineuser=ubuntu
for i in "$@"
do
ssh  $machineuser@$i "./home/ubuntu/cassandra/bin/cassandra -h $i decommission"
done