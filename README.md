Breeding Management System API
==============================
BMS API is a RESTful services prototyping project built on top of the [Middleware](https://github.com/naymesh/IBPDevUtilIBPMiddleware) - the data access layer of BMS.

### Pre Requisites ###
* Git
* Maven
* MySQL
* JDK
* An existing BMS installation with at least one program in it. Or a BMS development environment can be setup using instructions [here](https://github.com/digitalabs/IBDBScripts/tree/master/setuputils).

### Setup ###
* Checkout the project: `https://github.com/IntegratedBreedingPlatform/BMSAPI.git`.
* Create and configure `pipeline/config/{yourConfigFolderName}/application.properties` to point to your (BMS) environment specific values for MySQL central and local crop databases and the port (server.port) you want embedded tomcat to run at.

### Run ###

#### Building and running from source ####

In standalone mode, application uses the embeded Tomcat from Spring-Boot and automatically deploys to it.

* At the root of the checkout directory run `mvn clean install -DenvConfig={yourConfigFolderName}`
* This will create `bmsapi.war` under Maven `target` folder.
* Note that this is an executable war file and can be run using `java -jar bmsapi.war` command which launches BMS API standlone with embedded tomcat provided by Sprint-Boot.
* Alternatively, copy bmsapi.war file to the `webapps` directory of your Tomcat installation, in this case the embedded tomcat will automatically be disabled and BMS API will be deployed as a web application within external Tomcat under context path equal to the name of the war file (without the .war extension) as usual.

#### Within Eclipse ####
* Import the BMSAPI Maven project from the checkout.
* Configure `src/main/resources/application.properties` values as per your environment.
* Run Main.java as a java application.

#### Get pre-built war file and run ####

* Get the latest snapshot of built .war file [from our Nexus repository](http://apps.leafnode.io:8081/nexus/content/repositories/snapshots/org/generationcp/bmsapi/)
* Run this with `java -jar  bmsapi.war`

#### General Notes ###
The default database connection parameters BMS API uses are the same as the default BMS MySQL database parameters (localhost, port 13306 with user name root and no password). If you are running BMS API alongside your BMS installation, make sure that BMS is started first (mainly so that the BMS MySQL database is up). 

If you have not installed BMS with default database settings, create `application.properties` file in same directory as the BMS API executable war file and update db.* property values as per your environment:

```
server.port=19080
server.contextPath=/bmsapi

spring.thymeleaf.cache=false

db.host=<YourBMSMySQLHost>
db.port=<YourBMSMySQLPort>
db.username=<YourBMSMySQLUserName>
db.password=<YourBMSMySQLPassword>

db.workbench.name=workbench
```

then run with the usual `java -jar bmsapi.war` and BMS API will pick up the application.properties file from local directory and override the defaults.

### Explore the API ###
Explore and try out the live API documentation (built with [Swagger](https://helloreverb.com/developers/swagger)) at the home page `http://<server.host>:<server.port>/<server.contextPath>`.

Public deployment of the BMS API is available at: [api.leadnode.io](http://api.leafnode.io:19080/bmsapi). 


