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
* Spring context will load including the embedded tomcat where resources are deployed at `http://localhost:<server.port>/`. 


#### Deployed on external Tomcat ###
To build a war that can be deployed on external Tomcat:
* At the root of the checkout directory run `mvn package -Pwebapp`
* This will create `bms-api-1.0-SNAPSHOT.war` under Maven `target` folder.
* Copy this war (perhaps after renaming to somethig simpler) to the `webapps` directory of your Tomcat installation.

#### Within Eclipse ####
* Import the BMSAPI Maven project from the checkout.
* Run Main.java as a java application.

### Explore the API ###
Explore and try out the live API documentation at `http://<host>:<port>/apidoc/index.html`. 

Public deployment of the api is available at: [api.leadnode.io]. 

To change the central crop datatbase go to ``http://<host>:<port>/web`. Once a crop database is selected on this page, all API calls operate against the selected database.


[IBPDevUtils]:https://github.com/naymesh/IBPDevUtil
[Middleware]:https://github.com/naymesh/IBPDevUtilIBPMiddleware
[api.leadnode.io]:http://api.leafnode.io:18080/apidoc/index.html

