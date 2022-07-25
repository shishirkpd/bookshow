##BookShow Application 

###To Run

# bookshow

Prerequisites
This project use maven as build tool. OpenJDK-11 is required to build and run the tool

How to build and run
### run tests 
#### mvn clean test

### build jar 
#### ./mvn clean package

### publish jar
./mvn clean publish

run navigate to the jar location and execute the below command eg: jar file can be found under target
java -jar bookshow-1.0-SNAPSHOT.jar