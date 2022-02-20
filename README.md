# sunbird-cb-ext

This service is created to manage APIs over and above those of Sunbird..And also includes any APIs that are wrappers for Sunbird APIs, work allocation tool and many other features.

## Features

- Portal module (MDO, SPV, CBP, CBC)
- Work allocation tool
- Mandatory content check

## Tech

SB-CB-EXT uses a number of open source projects:

- [APACHE KAFKA 2.12-3.0.0] - used for data pipeline and data integration
- [APACHE CASSANDRA 3.11.6] - No sql database used for scalability and high availability
- [ELASTICSEARCH 6.3.0] - No sql database used for great searching capability
- [SPRING BOOT] - Great framework to work with java.
- [JAVA 11] - used for core development
- [DOCKER DESKTOP latest version] - only required in Windows 

## Installation

Installation steps for Cassandra in windows:

	-Download the latest version of Docker Desktop, as of now this is 4.3.2
	-Create a new folder "cassandra-data" in Local Disk C
	-Open the CMD and point it to C:\
	-Run the following commands one by one
		-docker -v
		-docker ps
		-docker run --name cassandra -p 7000:7000 -v C:/cassandra-data:/var/lib/cassandra -p 9042:9042 -d cassandra:3.11.6
		-docker ps(to check if the container is running or not)
		-docker exec -it cassandra bash
	Your cassandra is now ready to use.

Please find the cassandra.cql file in the resources folder where you can find the queries to be executed in cassandra
	
How to get the Kafka running?
	
	Here are the steps:
	-Download the Kafka and unzip it in Local Disk C
	-Now navigate to C:\kafka_2.11-2.3.0> open cmd here and execute the following command:
		.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
	-open cmd here again and execute the following command
		.\bin\windows\kafka-server-start.bat .\config\server.properties
	-open cmd here again and to create a new topic, navigate to the windows folder first and open three different
	cmd windows and execute the following commands in 3 separate windows:
		C:\kafka_2.11-2.3.0\bin\windows> kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic testtopicname
		C:\kafka_2.11-2.3.0\bin\windows> kafka-console-producer.bat --broker-list localhost:9092 --topic test
		C:\kafka_2.11-2.3.0\bin\windows> kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test --from-beginning
	You can create new topics by replacing the "test" with the new topic names in the above two commands
		
**Kafka Topics Required**

- orgCreation - Used to create SB Org object when new Department is created
- userRoleAuditTopic - Used to update the user_department_role record in Audit table 

**ES Index Details**
- Need to create indexes 
  - workallocationv2 - this name should be same as property value of workallocationv2.index.name
  - workorderv1 - this name should be same as property value of workorder.index.name

- Update the work_allocation mapping using file @ https://github.com/sunbird-cb/sunbird-cb-ext/blob/cbrelease-3.0.1/src/main/resources/elasticsearch/index/workallocationv2.json

- Update the work_order mapping using file @ https://github.com/sunbird-cb/sunbird-cb-ext/blob/cbrelease-3.0.1/src/main/resources/elasticsearch/index/workorderv1.json

**ES Index Alias Details**
- Check any existing index has alias "work_allocation" or "work_order". If so, delete the alias mapping using following CURL in ElasticSerach server.
	- curl --location --request DELETE 'http://localhost:9200/{{existingIndexName}}/_alias/{{aliasName}}'
	- existingIndexName - refers the existing index in ES
	- aliasName - refers either "work_allocation" or "work_order"

- Add alias for newly created Indexes
	- curl --location --request POST 'http://localhost:9200/_aliases' --header 'Content-Type: application/json' --data-raw '{"actions":[{"add":{"index":"workorderv1","alias":"work_order"}}]}'
	- curl --location --request POST 'http://localhost:9200/_aliases' --header 'Content-Type: application/json' --data-raw '{"actions":[{"add":{"index":"workallocationv2","alias":"work_allocation"}}]}'
	

**Cassandra table list**

- mandatory_user_content
- user_assessment_summary
- user_assessment_master
- user_quiz_master
- user_quiz_summary
- work_order
- work_allocation
- org_staff_position
- org_budget_scheme
- org_audit

Note : Make sure the kafka and cassandra is up and running before starting the service

