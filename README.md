# ElasticityManager
A self-trained Proactive Elasticity Manager for cloud-based storage systems
	- Automatically scales in/out an elastic distributed Key-Value store running on cloud environments using online profiling and
	a Support Vector Machine based performance model.
	- Tested on Apache Cassandra storage system

Running ----> A Runnable Jar is already available on the Deployment folder or you can also build the project using Maven;
$ java -jar ElasticityManager/Deployment/SelfElastMan-0.0.1-SNAPSHOT.jar

Note: The DataCollector uses port 9898 to connect to a Cassandra node to gather read and write metrics periodically
