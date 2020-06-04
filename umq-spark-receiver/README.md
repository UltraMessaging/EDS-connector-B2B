UMQSparkReceiver
===============

This is a spark streaming implementation for Ultra Messaging. It is an eclipse Maven project and can be built either from within Eclipse or using maven 3.x. To build under maven, the UM 6.9 java library should be installed to your repository.

* __Install command line__
  * `mvn install:install-file -Dfile=UMS_6.9_jdk1.5.0_12.jar  -DgroupId=com.latencybusters -DartifactId=lbm -Dversion=6.9 -Dpackaging=jar`
* __Build command line__
  * `mvn clean package`
  
* The file __target/umq-spark-(version).jar__ is generated
  
To change the UM version used, change the filename and version in the above command line, and modify pom.xml to reference the new version. The version in the following block should reflect this:
```xml
<dependency>
	<groupId>com.latencybusters</groupId>
	<artifactId>lbm</artifactId>
	<version>6.9</version>
</dependency>
```

------------------------

### The following classes are included in the jar

| Class Name | Purpose | Notes |
| :------------------------ | :------------------------ | :--- |
| `com.infa.presales.spark.UmqSparkReceiver` | Spark streaming receiver implementation | A spark job should call the constructor with an array of command line options. There is also a main class which will behave similar to umqrcv |
| `com.infa.presales.spark.UmqWordCount` | Spark job main class | Creates an instance of UmqSparkReceiver and uses it to create a spark streaming context |
| `com.infa.presales.umqsrc` | Modified umqsrc | Added __--stdin__ to read text from stdin |
| `com.infa.presales.spark.TestSparkReceiver` | self-contained spark TCP input example | Adapted from Apache examples |

### This has been tested on the Cloudera 5.8 Quickstart VM.
---------------------


### These supporting scripts and configuration files are in __src/resources__:

| Filename | Purpose | Notes |
|------------|:--------------|:----------|
| `rcv.cfg` | Receiver configuration | Used by runrcv.sh |
| `runrcv.sh` | Run as a command line | Similar to umqrcv application |
| `runsrc.sh` | Run embedded umqsrc | Added options to read text from stdin |
| `sparkrcv.sh` | Run as a spark streaming receiver | Submits __UmqWordCount__ as a local spark job |
| `src.cfg` | umqsrc configuration | Edit to fit your environment |
| `vdsrcv.cfg` | Receiver configuration | Use VDS default resolver daemon |
| `vdsrcv.sh` | Spark Receiver configuration | Uses VDS receiver configuration |
