Breeding Management System API
==============================
BMS API is a RESTful services prototyping project built on top of the [Middleware].

### Pre Requisites ###
* Git
* Maven
* MySQL
* JRE and/or JDK
* BMS databases. [IBPDevUtils] is one option for setting them up.

### Setup ###
* Checkout the project: `git clone https://github.com/naymesh/BMSAPI`.
* Configure `src/main/resources/application.properties` to point to your environment specific values for MySQL central and local crop databases and the port (server.port) you want embedded tomcat to run at.

### Run ###

#### Standalone ####

In standalone mode, application uses the embeded Tomcat from Spring-Boot and automatically deploys to it.

* At the root of the checkout directory run `mvn package`. This will build an executable jar `bms-api-1.0-SNAPSHOT.jar` under the Maven `target` folder. Building a standalone jar is the default packaging mode.
* To run do `java -jar bms-api-1.0-SNAPSHOT.jar`
* Spring context will load including the embedded tomcat where resources are deployed at `http://localhost:18080/`. For example, the study resource exposes a summary service which can be accessed at `http://localhost:18080/study/summary/5715` where 5715 is an identifier of a Rice central study. It should return a response that looks like:
```
{
 "id": 5715,
 "name": "RYT2000DS",
 "title": "Replicated Yield Trial 2000  Dry Season",
 "objective": null,
 "type": "RYT",
 "startDate": "20000101",
 "endDate": "null"
}
```

#### Deployed on external Tomcat ###
To build a war that can be deployed on external Tomcat:
* At the root of the checkout directory run `mvn package -Pwebapp`
* This will create `bms-api-1.0-SNAPSHOT.war` under Maven `target` folder.
* Copy this war (perhaps after renaming to somethig simpler) to the `webapps` directory of your Tomcat installation.
* The example resource above will be available at `http://localhost:18080/<name_of_war>/study/summary/5715` in this case.

### Within Eclipse ###
* Import the BMSAPI Maven project from the checkout.
* Run Main.java as a java application.

[IBPDevUtils]:https://github.com/naymesh/IBPDevUtil
[Middleware]:https://github.com/naymesh/IBPDevUtilIBPMiddleware

