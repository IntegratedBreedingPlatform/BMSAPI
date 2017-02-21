Breeding Management System API
==============================
BMS API is a set of RESTful web services that allow access to data in the BMS database.

Authentication
==============

All BMS API calls require authentication and valid access token provided. Please refer to the public Wiki documentation: [BMSAPI - Authentication](https://github.com/IntegratedBreedingPlatform/Documentation/wiki/BMSAPI---Authentication) for detauls.


Using the BMSAPI Swagger UI
===========================

BMSAPI ships with a user interface that allows easy exploration and interaction with various API resources/entities and operations that are exposed. It can be accessed at following URL in any BMS v4.x deployment: `http://<server.host>:<server.port>/bmsapi/`. This user interface also acts as live documentation of the API resources, methods, and parameters etc. It is built using [Swagger](http://swagger.io/) which is a standard in REST API live documentation.

Like in the case of direct access (e.g. the cURL examples in previous section) access to BMSAPI methods using this Swagger UI requires the user to first authenticate by logging into the BMS deployed on **the same server** and **in the same browser window/tab**. 

If you access the Swagger UI  without login, an alert message will be shown saying **Authentication has expired. Please login to Workbench again, then refresh this page."**. Upon "OK"ing this alert message you will notice that you can still see the API listings and "Try it out!" button etc. under each resource, but upon invoking any API method by clocking the "Try it out!" button, authentication error response will be returned which looks like:

```
{
  "timestamp": 1459727366021,
  "status": 401,
  "error": "Unauthorized",
  "message": "Access Denied"
}
```

So, go to the usual BMS login page **of the same server deployment** at `http://<server.host>:<server.port>/ibpworkbench/controller/auth/login/` **in the same browser window/tab** and login as a valid BMS user first. Once successfully logged into BMS, reload the Swagger UI home page `http://<server.host>:<server.port>/bmsapi/` in the same browser window (different tab is fine, but must be same browser window). You should then no longer see the alert message regarding authentication. The Swagger UI now detects that you are authenticated on same server as a valid user. Now invoking the API methods using the "Try it out!" button, you should expect to see appropriate data response.

**EXAMPLE**

Leafnode development team's nightly deploy of the BMSAPI can be seen at: http://api.leafnode.io:10081/bmsapi/. As explained above, you will see  **Authentication has expired. Please login to Workbench again, then refresh this page."** alert message because you are not yet logged in. You can see the API resources and method listings etc. but trying to invoke any of the methods using the "Try it out!" button, you will see HTTP 401, Unauthorized, Access Denied response as shown above.

To successfully invoke the operations as an authenticated user, you need to first login as that user at http://api.leafnode.io:10081/ibpworkbench/controller/auth/login, then reload http://api.leafnode.io:10081/bmsapi/ in the same browser window. You should now be able to see actual data responses based on what the logged in user has access to.

**Please note** that http://api.leafnode.io:10081/bmsapi/ was shown here purely as an example to demonstrate the use of BMSAPI Swagger user interface. It is not intended to be used for client demos or actual development against it. Because it is a development team's nightly deploy, it will be restarted/redeployed frequently without any notice. You are welcome to play around with it while it is up :)

 
For Code Contributing Developers
================================
Setup, build from source, install and deploy.

## Pre Requisites ##
* Git
* Maven
* MySQL
* JDK
* An existing BMS installation with at least one program in it. Or a BMS development environment can be setup using instructions [here](https://github.com/IntegratedBreedingPlatform/DBScripts/tree/master/setuputils).

### Setup ###
* Get the source code:
  * BMSAPI: `git clone https://github.com/IntegratedBreedingPlatform/BMSAPI.git`.
  * Middleware: `git clone https://github.com/IntegratedBreedingPlatform/Middleware.git` which is a required dependency of BMSAPI.
  * BMSConfig : `git clone https://github.com/IntegratedBreedingPlatform/BMSConfig.git` - this is where your environment configuration will be read from during build.
* Create and configure your environment specific folder under [BMSConfig](https://github.com/IntegratedBreedingPlatform/BMSConfig) project and set values as per your environment such as for MySQL database parameters, tomcat server port etc. For more details see existing user's config folders and the readme the on [BMSConfig](https://github.com/IntegratedBreedingPlatform/BMSConfig) project.
* The main properties that BMSAPI rely on can be seen in `src/main/resources/application.properties` and in Middleware project at `src/main/resources/database.properties`. The values are substituted by Maven build from your environment specific configuration folder in BMSConfig.
* First run `mvn clean install` on Middleware.
* Then run `mvn clean install` on BMSAPI.

#### Building and running from source ####
In standalone mode, application uses the embeded Tomcat from Spring-Boot and automatically deploys to it.
* At the root of the checkout directory run `mvn clean install -DenvConfig={yourConfigFolderName}`. 
* This will create `bmsapi.war` under Maven `target` folder.
* Note that this is an executable war file and can be run using `java -jar bmsapi.war` command which launches BMS API standlone with embedded tomcat provided by Sprint-Boot.
* Alternatively, copy bmsapi.war file to the `webapps` directory of your Tomcat installation, in this case the embedded tomcat will automatically be disabled and BMS API will be deployed as a web application within external Tomcat under context path equal to the name of the war file (without the .war extension) as usual.

#### Running from within Eclipse ####
* Import the BMSAPI Maven project from the checkout.
* Run `mvn clean install` via command line on BMSAPI project so that `application.properties` are set and configured in build folder from your BMSConfig folder.
* Run `org.ibp.Main` as a java application.

#### General Notes ####
The default database connection parameters BMS API uses are the same as the default BMS MySQL database parameters (localhost, port 43306 with user name root and no password). If you are running BMS API alongside your BMS installation, make sure that BMS is started first (mainly so that the BMS MySQL database is up). 

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
then run with the usual `java -jar bmsapi.war` and BMS API will pick up the `application.properties` file from local directory and override the defaults.



