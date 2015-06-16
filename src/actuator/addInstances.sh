#!/bin/bash
machineuser=ubuntu
for i in "$@"
do
ssh  $machineuser@$i "sudo rm -rf /var/lib/cassandra/*"
ssh  $machineuser@$i "./home/ubuntu/cassandra/bin/cassandra"
done