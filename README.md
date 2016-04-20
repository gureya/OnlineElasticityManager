# Online Elasticity Manager (OnlineElastMan)
A self-trained Proactive Elasticity Manager for cloud-based storage systems:

The pay-as-you-go pricing model and the illusion of unlimited resources makes cloud computing a conducive environment for provision of elastic services where different resources are dynamically requested and released in response to changes in their demand. The benefit of elastic resource allocation to cloud systems is to minimize resource provisioning costs while meeting service level objectives (SLOs). 

With the emergence of elastic services, and more particularly elastic key-value stores, that can scale horizontally by adding/removing servers, organizations perceive potential in being able to reduce cost and complexity of large scale Web 2.0 applications. A well-designed elasticity controller helps reducing the cost of hosting services using dynamic resource provisioning and, in the meantime, does not compromise service quality. An elasticity controller often needs to be trained either online or offline in order to make it intelligent enough to make decisions on spawning or removing extra instances when workload increase or decrease. 

However, there are two main issues on the process of control model training. A significant amount of recent works train the models offline and apply them to an online system. This approach may lead the elasticity controller to make inaccurate decisions since not all parameters can be considered when building the model offline. The complete training of the model consumes large efforts, including modifying system setups and changing system configurations. Worse, some models can even include several dimensions of system parameters. 

To overcome these limitations, we present the design and evaluation of a self-trained proactive elasticity manager for cloud-based elastic key-value stores. Our elasticity controller uses online profiling and support vector machines (SVM) to provide a black-box performance model of an application’s SLO violation for a given resource demand. The model is dynamically updated to adapt to operating environment changes such as workload pattern variations, data rebalance, changes in data size, etc. 

We have implemented and evaluated our controller using the Apache cassandra key-value store in an OpenStack Cloud environment. Our experiments with artificial workload traces shows that our controller guarantees a high level of SLO commitments while keeping the overall resource utilization optimal.

# Running:
A Runnable Jar is already available on the Deployment folder or you can also build the project using Maven;
- $ java -jar ElasticityManager/Deployment/SelfElastMan-0.0.1-SNAPSHOT.jar

Conceptually, the Data collector, Workload prediction, Online training and Controller operate concurrently and communicate by message passing. Based on the workload prediction result and updated system model, the controller invokes the cloud storage API to add or remove servers.

Note: The DataCollector uses port 9898 to connect to a Cassandra node to gather read and write metrics periodically

From the data collector, the workload is fed to two modules: workload prediction and online training.

The following parameters were considered when building the online performance model and have to be provided in the config file (config.properties file):
- Data grid scale
- Read and Write lantency queue
- Confidence level

The controller also keeps a list of all available instances (should be provided initially in the List_of_CassandraNodes.txt file) with their state (active or inac- tive). This list is updated upon decommissioning or commissioning operations.

Below is a list of parameters that needs to be specified for the running of OnlineElastMan

#List of Online Elasticity Manager config properties...Default

- Periodic timer(in seconds) to pull statistics from the Cassandra Node
timerWindow=10

- Dimensions metrics: the Maximum Read and Write Throughput(ops/sec), DataSize(KB)
Your MaxDimensions should be equal or greater than your scale 
maxReadTP=1010
maxWriteTP=1010
maxDataSize=10

- The granularity of your dimensions
scale=50

- Queue length for each data points 
queueLength=10

- Confidence Level in the number of sla violations. E.g. 10% of Sla violations
confLevel=0.1
- path to your matlab scripts
matlabPath=/Users/GUREYA/Documents/workspace/ElasticityManager/src/predictor
- path to your actuator scripts installations
actuatorScriptsPath=/Users/GUREYA/Documents/workspace/ElasticityManager/src/actuator
- current DataSize. Consider getting this from the cloud side(in KB)
currentDataSize=5
- Expected target throughput per server
targetThroughput=1000
- Expected performance and response time reads and writes 99th percentile(us)
readResponseTime=5000
- MINIMUM NUMBER OF SERVERS EXPECTED FOR THE KEY-VALUE STORE
minServers=5
- MAXIMUM NUMBER OF SERVERS EXPECTED FOR THE KEY-VALUE STORE
maxServers=10

