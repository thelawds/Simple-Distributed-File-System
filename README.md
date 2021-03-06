# Simple-Distributed-File-System
Innopolis University Assignment II. Distributer File System

# Documentation how to launch and use your system

## Using Docker
Firstly we need to pull the repo from GitHub.

Secondly - pull the images "thelawds/storage_node" and "thelawds/naming_node" from the DockerHub.

```docker
docker pull thelawds/storage_node
docker pull thelawds/naming_node
```

Initialize the docker swarm and then use ```docker stack deploy -c docker-compose.yml SDFS```.

Client can be executed only from scratch!

## From scratch

Build the sources using your IDE or [Gradle](https://gradle.org/). You might want to change some parameters in:
```
  - Naming/src/main/resources/application.yaml
  - Storage/src/main/resources/application.yaml
```

For more information visit [this](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-yaml) page

Then you can run the builded applications using ```java -jar /Naming/build/libs/naming-0.0.1-SNAPSHOT.jar```, ```java -jar /Storage/build/libs/storage-0.0.1-SNAPSHOT.jar``` and ```java -jar /Client/build/libs/client-0.0.1-SNAPSHOT.jar```.

# Architectural diagrams
1) When Client want to do some operation in the system it connects to the Naming Server and request the IP adress of the Storage Node. Naming server randomly choose the Storage through all available Servers.
2) Client and Server exchange HTTP messages between each other to make the operation.
3) When Storage Server receives all the data it sends the acknowledgment to the Naming Server
4) and requests the IP adress of one more Storage Server with enough amount of memory to save the replica of the data in it.
5) After that the first Server sends the same client request, with minor changes in flags, to the second server.
6) When the second Storage Server receives all data, it sends an ack to the Naming server and it, in it's turn, makes a record in the database.

![Structure of our project](pic/OverallStructure.png "This is the structure of our project")

## Naming Node
In our project Naming Server tracks the file system directory tree using the Postgres database. When a client wishes to perform an operation on a file, it first contacts the Naming Server to obtain the Storage Server IP. Naming Node generates the random number to choose one of the available Storage Nodes. Also every 60 seconds Name Node chacks the storages health status to register their presence. If nodes are failed, then Naming Server drops them from the database. Within this checking operation Name Server also replicate the content which was saved only in one storage.

![Naming Packages](pic/NamingPackages.png "Package of java code for the Naming Server")

## Storage Nodes
Storage servers provide clients with access to it's local file system and several operations with files, such as initialization, file creation, file reading, file writing, file deletion, directory opening and derectory reading. Also Storage Server can interact with the Naming Server to transparently perform replication of files.

![Storage Packages](pic/StoragePackages.png "Package of java code for the Storage Servers")

## Database
This is the Postgres database which consists of three tables: "file_information", "storage_node" and "file_information_to_storage_node". Let's talk about each separately.

- File_information table help us to save the info about each file, such as it's id, path, last_update, size, executability, readability and writeability. 
- Storage_node table save all storage nodes info: name, address, state and the amount of free space. 
- The last one is a many to many relationship which map files' information to the Storage Nodes.

# Description of communication protocols

We have decided to use [Synchronous HTTP Communications](https://docs.microsoft.com/en-us/dotnet/architecture/microservices/architect-microservice-container-applications/communication-in-microservice-architecture) in pair with [REST architectural pattern](https://en.wikipedia.org/wiki/Representational_state_transfer).

# Contribution of each team member
Murashko Alecsey - SE_01:

- Writing Storage Node
- Writing Naming Node
- Documentation

Anna Gorb - SE_02:

- Docker Files
- Writing Client
- Documentation

# Good Choices and reasons for them:

## HTTP REST Communication
We have chosen REST architectural pattern over HTTP, because it is very simple to implement and there are plenty of frameworks available. Unfortunately, usage of http creates some overhead on file transmission.

## One Point of Failure
There is only one point of failure in the system - naming node. It is very easy to implement and practice of  GFS and HDFS has shown that this approach is reliable enough.

## Naming server checks the availability of all the nodes in the system

Such a decision is easy to implement and have an advantage that SDFS user can moderate the rate with which Naming server asks Storage servers about their state, which helps to moderate the Naming node's performance. 
