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
* Checkout the project: `git checkout https://github.com/naymesh/BMSAPI`
* Configure `src/main/resources/application.properties` to point to your environment specific values
* At the root of the checkout directory run `mvn package`. This will build an executable jar 'bms-api-1.0-SNAPSHOT.jar' under the `target` folder.
* To run do `java -jar bms-api-1.0-SNAPSHOT.jar`
* Spring context will load including the embedded tomcat where resources are deployed at `http://localhost:18080/` (port is configurable in application.properties). For example, the study resource exposes a summary service which can be accessed at `http://localhost:18080/study/summary/5715` where 5715 is an identifier of a Rice central study. It should return a response that looks like:
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

[IBPDevUtils]:https://github.com/naymesh/IBPDevUtil
[Middleware]:https://github.com/naymesh/IBPDevUtilIBPMiddleware

