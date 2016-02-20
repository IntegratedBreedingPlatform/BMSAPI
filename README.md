Breeding Management System API
==============================
BMS API is a set of RESTful web services to interact with the data in BMS database.

### Authentication ###
BMS API services expose operations on data created by registered BMS users as they carry out breeding programs and studies for various crops. Hence access to the BMS API services require authentication as the same registered user. BMS API uses a light-weight variant of the popular OAuth protocol known as X-Auth. In exchange of valid users credentials (Workbench user name and password) BMS API issues a fixed time window (configurable per deployment) ticket/token which is then required to be provided for **each and every**  BMS API service invocation as part of `X-Auth-Token` request header. Example below illustrates the scenario of a command line client (curl) accessing the API:

**Request a listing of programs without authentication:**

We are using curl to demonstrate the request/response behaviour. Any equivalant HTTP client (e.g. [Postman](http://www.getpostman.com/)) which allows user to specify HTTP request headers can be used.

Request: 
```
curl http://<host>:<port>/bmsapi/program/list
```
Response: 
```
Code: 401
Response Body:
{
  "timestamp": 1447884520756,
  "status": 401,
  "error": "Unauthorized",
  "message": "Access Denied"
}
```
This is because there is no authentication header provided as part of the request.

**Request a listing of programs with authentication:**

First, authenticate with credentials of a registered Workbench user:

Request :
```
curl -X POST -H "Content-Type: multipart/form-data;" -F "username=naymesh" -F "password=naymeshspassword" 'http://<host>:<port>/bmsapi/authenticate'
```
Response (if credentials are correct):
```
Code: 200
Response Body:
{
  "token": "naymesh:1447886088052:fdd12b1069a9f28ddee2f8d42d30dde5",
  "expires": 1447886088052
}
```

Now make the program listing API request with the authentication header using the token provided in response to successful authentication as in example above:

Request:

```
curl -X GET -H "X-Auth-Token: naymesh:1447886088052:fdd12b1069a9f28ddee2f8d42d30dde5" 'http://<host>:<port>/bmsapi/program/list'
```

Response:
```
Code: 200
Response Body:
[
    {
        "id": "1",
        "uniqueID": "fb0783d2-dc82-4db6-a36e-7554d3740092",
        "name": "Naymesh's Program",
        "createdBy": "naymesh",
        "members": [
            "naymesh"
        ],
        "crop": "maize",
        "startDate": "2015-11-11"
    },
    {
        "id": "2",
        "uniqueID": "57b8f271-56db-448e-ad8d-528ac4d80f04",
        "name": "Akhil's Program",
        "createdBy": "akhil",
        "members": [
            "akhil",
            "naymesh"
        ],
        "crop": "maize",
        "startDate": "2015-12-12"
    }
]
```
## Authorization ##
Based on the details of the user making requests to BMSAPI, the data returned is restricted and filtered in the same way as the data is filtered/restricted when user interacts with the same data via the BMS application user interface. For example, users only see the data for the programs/studies they have created or the programs/studies that they are part of. As shown in example above, the listing returned two programs one which the authenticated user (naymesh) has created and one where the user is a member.

# BMSAPI Build and Install/Deploy #

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

### Run ###
#### Building and running from source ####
In standalone mode, application uses the embeded Tomcat from Spring-Boot and automatically deploys to it.
* At the root of the checkout directory run `mvn clean install -DenvConfig={yourConfigFolderName}`. 
* This will create `bmsapi.war` under Maven `target` folder.
* Note that this is an executable war file and can be run using `java -jar bmsapi.war` command which launches BMS API standlone with embedded tomcat provided by Sprint-Boot.
* Alternatively, copy bmsapi.war file to the `webapps` directory of your Tomcat installation, in this case the embedded tomcat will automatically be disabled and BMS API will be deployed as a web application within external Tomcat under context path equal to the name of the war file (without the .war extension) as usual.

#### Within Eclipse ####
* Import the BMSAPI Maven project from the checkout.
* Run `mvn clean install` via command line on BMSAPI project so that `application.properties` are set and configured in build folder from your BMSConfig folder.
* Run `org.ibp.Main` as a java application.

#### General Notes ###
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

### Explore the API ###
Explore and try out the live API documentation (built with [Swagger](https://helloreverb.com/developers/swagger)) at the home page `http://<server.host>:<server.port>/<server.contextPath>`.

Public deployment of the BMS API is available at: [api.leadnode.io](http://api.leafnode.io:10081/bmsapi/). Access to invoke the API operations via Swagger UI requires the user to first authenticate by logging into the Workbench in same browser window.


