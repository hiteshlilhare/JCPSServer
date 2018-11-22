# JCPSServer
It is Java Card Application Store portal which will execute as server on port number 8010.

### Dependency
* JDK8.0
* Maven 3.0
* MySQL 8.0 for database
* Create scheme named 'jcpss' in MySQL database
* Update database username and password in connect() method of MySQLDAOImpl.java class inside package com.github.hiteshlilhare.jcpss.db

### How to Start Server
* Clone the repository
* Clean project
```shell
mvn clean
```
* Compile project
```shell
mvn compile
```
* Strat project
```shell
mvn spring-boot:run
```