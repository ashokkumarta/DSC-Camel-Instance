# Dataspace Connector Camel Instance

This Camel application is intended for use with the 
[Dataspace Connector](https://github.com/International-Data-Spaces-Association/DataspaceConnector) and the 
[Configuration Manager](https://github.com/International-Data-Spaces-Association/IDS-ConfigurationManager).

The communication between the Dataspace Connector and data apps can be achieved by using an integration Framework
like Apache Camel. This also provides the possibility to use all kinds of different backends for resources registered
in the connector, as no separate implementation has to be made for each possible protocol. To keep the Dataspace
Connector lightweight and modular, no integration framework will be integrated directly, but rather be executed
standalone in parallel to the connector, as can be done with this Camel application.

## Content

* [Deployment](#deployment)
* [Deploying Camel routes](#deploying-camel-routes)
* [Defining Camel routes](#defining-camel-routes)
* [Using the Dataspace Connector with SSL enabled](#using-the-dataspace-connector-with-ssl-enabled)
* [Using apps](#using-apps)
* [Contributing](#contributing)
* [License](#license)

## Deployment

In the following it is described how to build and run the Camel application. If the deployment is successful, the 
application is available at https://localhost:9090.

### Maven

To build and run the application with Maven, ensure that at least Java 11 is installed. Then, follow these steps:

1. Build the application by executing `mvn clean package` in the application's root directory.
2. Run the application by executing `java -jar target/dsc-camel-instance-{VERSION}.jar`

### Docker

To build and run the application with Docker, follow these steps:

#### Option 1: Docker

1. Build the application by executing `docker build -t <IMAGE_NAME:TAG> .` in the application's root directory.
2. Run the application by executing `docker run --publish 9090:9090 --detach --name dsc-camel <IMAGE_NAME:TAG>`

#### Option 2: Docker Compose

The *docker-compose.yaml* sets up the Camel application and a Dataspace Connector. Optionally, other applications like
e.g. a database or an MQTT broker can be added to the setup to be used in routes.

1. Build the application by executing `docker-compose build` in the application's root directory.
2. Start the setup by executing `docker-compose up`.

## Deploying Camel routes

This application allows for both defining routes statically before the application is started and adding routes 
dynamically at runtime.

### Initial/static route deployment

The easiest way to deploy routes is to statically define them in the Camel Context before the application starts.
Spring will create a default Camel Context on application start. When using that context, all beans and routes you want
to use have to be declared separately/in different locations. By defining a Camel Context in an XML file and importing 
that into the application, the whole context including routes and beans can be defined in one place. The file under 
`src/main/resources/camel-context.xml` can be used for defining the Camel context. Inside the *camelContext* tag, 
any number of routes can be added. To import the file into the application, add the following annotation to the main 
class:

    @ImportResource("classpath:camel-context.xml")

If the Camel context should be loaded from an external file, just change *classpath* to *file* in the import statement.

Some Camel components require additional beans to be defined in the application context for a route to be executed (e.g.
definition of a data source bean when using the SQL component). These beans can also be defined in *camel-context.xml*
and will then automatically be available for the routes.

**Important:** the *bean* tags have to be declared **outside** the *camelContext* tag:

	<bean id="..." class="..."></bean>

	<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
		<route id="...">
			...
		</route>
	</camelContext>

### Dynamic route deployment

While the static deployment of routes is a simple and quick solution and will probably suffice for testing purposes, in
most cases there will be a need to deploy and/or remove routes dynamically at runtime. Therefore, XML files containing
routes and, if necessary, required beans can be sent to the application via HTTP using the following endpoints:

* **POST /api/routes** (multipart/form-data with part *file* for the XML file): add routes
* **DELETE /api/routes/{route-id}**: remove routes
* **POST /api/beans** (multipart/form-data with part *file* for the XML file): add beans
* **DELETE /api/beans/{bean-id}**: remove bean

The files sent to the application should have the following structures for routes and beans respectively:

```
<routes xmlns="http://camel.apache.org/schema/spring">

    <route id="...">
        ...
    </route>
    
</routes>
```

```
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd">

    <bean id="..." class="...">
        ...
    </bean>    
     
</beans> 
```

Inside the *routes* and *beans* tags, any number of routes or beans can be added.

## Defining Camel routes

This application uses the Camel Spring XML DSL. Therefore, all Camel routes are defined in XML format.
Each route has to be enclosed by *route* tags and has to contain a start point (*&lt;from uri="..."&gt;*). It can also
contain any number of endpoints a message should be sent to (*&lt;to uri="..."&gt;*).

    <route id="route-id">

        <from uri="..."/>

        <to uri="..."/>

    </route>

For endpoints, any Camel component can be used (e.g. *http*, *sql*). Each component used in the routes has to be added
to this application's POM as a dependency. For this, the component's Springboot starter can be used. Hereinafter,
example routes are given for using different Camel components as data sources/sinks with the Dataspace Connector. All
Camel components used in the examples have already been added to the POM. 

**The routes described here use version 5 of the Dataspace Connector. They can also be found under 
`src/main/resources/routes/dsc-v5`. The same routes for version 4 of the Dataspace Connector can be found under 
`src/main/resources/routes/dsc-v4`.**

### Provider

The following examples show how to fetch data from different backend systems and publish it to the Dataspace Connector
as a resource's data. In all cases, a resource with the specified ID has to be created in the connector first.

#### HTTP

The Camel HTTP component cannot be used as a consumer, i.e. it cannot be used in a *from* tag and thus cannot be
used as the start of a route. A simple workaround is to use a timer as the route start and then call the HTTP backend.

    <route id="http-to-dsc-example">

        <from uri="timer://foo?delay=10000&amp;period=15000"/>

        <setHeader name="CamelHttpMethod">
            <constant>GET</constant>
        </setHeader>
        <to uri="http://http-demo-backend:8090/demo"/>
        
        <convertBodyTo type="java.lang.String"/>

        <setHeader name="CamelHttpMethod">
            <constant>PUT</constant>
        </setHeader>
        <setHeader name="Authorization">
            <constant>Basic YWRtaW46cGFzc3dvcmQ=</constant>
        </setHeader>
        <to uri="http://dataspace-connector:8080/api/artifacts/927906f2-5ee1-4678-9ace-5f1f2368606c/data"/>

    </route>

This route will start every 15 seconds (with an initial delay of 10 seconds), make an HTTP GET call to the backend and
send the response to the connector as the data of the artifact with ID *927906f2-5ee1-4678-9ace-5f1f2368606c*.

#### SQL

For Spring to be able to load the database driver for the database connection, the dependency of the chosen database
management system (e.g. PostgreSQL, MySQL) has to be added to the POM.

When using the Camel SQL component, the database to use has to be configured in a bean. Therefore, add a data source
bean:

	<bean id="testDataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
		<property name="url" value="jdbc:postgresql://postgres:5432/testdb"/>
		<property name="driverClassName" value="org.postgresql.Driver" />
		<property name="username" value="postgres-user" />
		<property name="password" value="12345"/>
	</bean>

Afterwards, the route definition can be added. The query to use when fetching data from the database has to be specified
in the URI of the *from* tag:

    <route id="sql-to-dsc-example">

        <from uri="sql:select * from test?initialDelay=10000&amp;delay=15000&amp;useIterator=false&amp;dataSource=#testDataSource"/>
        
        <convertBodyTo type="java.lang.String"/>

        <setHeader name="CamelHttpMethod">
            <constant>PUT</constant>
        </setHeader>
        <setHeader name="Authorization">
            <constant>Basic YWRtaW46cGFzc3dvcmQ=</constant>
        </setHeader>
        <to uri="http://dataspace-connector:8080/api/artifacts/927906f2-5ee1-4678-9ace-5f1f2368606c/data"/>

    </route>

This route will start every 15 seconds (with an initial delay of 10 seconds), fetch data from the database using the
specified query and send that to the connector as the data of the artifact with ID 
*927906f2-5ee1-4678-9ace-5f1f2368606c*.

Note that the data source bean's ID is referenced as the value for the query parameter *dataSource* in the *from* tag.
This allows you to specify the correct data source if multiple data source beans are defined. There is also a query
parameter *useIterator* which is set to *false*. This defines that all records returned by the query will be fetched and
sent in **one** message. If the value is set to *true*, **each** record returned by the query will be sent in a
**separate** message.

#### MQTT

As no Springboot starter is available for the MQTT component, the Paho component is used instead. It also offers 
support for MQTT, but is, as opposed to the MQTT component, implemented using the *Eclipse Paho* library. Using the 
Paho component does not require additional beans to be defined, as all information required for the connection can be 
given in the URI of the *from* tag. Therefore, only the route has to added:

    <route id="mqtt-to-dsc-example">

        <from uri="paho:test-topic?brokerUrl=tcp://mosquitto:1883"/>
        
        <convertBodyTo type="java.lang.String"/>

        <setHeader name="CamelHttpMethod">
            <constant>PUT</constant>
        </setHeader>
        <setHeader name="Authorization">
            <constant>Basic YWRtaW46cGFzc3dvcmQ=</constant>
        </setHeader>
        <to uri="http://dataspace-connector:8080/api/artifacts/927906f2-5ee1-4678-9ace-5f1f2368606c/data"/>

    </route>

This route will be triggered whenever a new message is published to the topic with name *test-topic* at the MQTT broker
located at *tcp://mosquitto:1883* and send that message's payload to the connector as the data of the artifact with ID 
*927906f2-5ee1-4678-9ace-5f1f2368606c*.

### Consumer

The following example shows how to fetch a resource's data from the connector and send it to a backend system.

#### File

No additional beans have to be defined to use the file component, so only the route has to be added. As the route starts
with fetching data from the connector using HTTP and the Camel HTTP component cannot be used as a consumer, a timer
is used as the route start.

    <route id="dsc-to-file-example">

        <from uri="timer://foo?delay=10000&amp;period=15000"/>

        <setHeader name="CamelHttpMethod">
            <constant>GET</constant>
        </setHeader>
        <setHeader name="Authorization">
            <constant>Basic YWRtaW46cGFzc3dvcmQ=</constant>
        </setHeader>
        <to uri="http://dataspace-connector:8080/api/artifacts/927906f2-5ee1-4678-9ace-5f1f2368606c/data"/>
        
        <convertBodyTo type="java.lang.String"/>

        <to uri="file:/output?fileName=resourcedata.txt"/>

    </route>

This route will start every 15 seconds (with an initial delay of 10 seconds), fetch the data of the connector's
artifact with ID *927906f2-5ee1-4678-9ace-5f1f2368606c* and write it to a file located at */output/resourcedata.txt*.

## Using the Dataspace Connector with SSL enabled

In all given example routes the Dataspace Connector is addressed using HTTP, not HTTPS. This is due to the fact that
in test environments self-signed certificates are often used. These lead to an error when Camel tries to call the
connector. 

To be able to use the connector with SSL enabled also when using self-signed certificates, an implementation of the 
*org.apache.camel.component.http.HttpClientConfigurer* that works with self-signed certificates has been added to this
project (`src/main/java/de/fraunhofer/isst/dataspaceconnector/camel/util/SelfSignedHttpClientConfigurer`). **Note that
this disables the hostname verification and thus is not secure!** To use this HttpClientConfigurer in routes, declare 
it as a bean and add it as a query parameter at the end of the connector URI:

	uri="https://dataspace-connector:8080/...?httpClientConfigurer=#idOfHttpClientConfigurerBean"/>

**In productive environments SSL should always be enabled and real certificates should be used!**

## Using apps

When using Camel, data apps can be integrated into the routes. In the following, it will be described how to achieve
this. If possible, a data app should provide one HTTP endpoint for both input and output. The data transferred in a
route can therefore be sent to this endpoint using the Camel HTTP component. The app's result will then be synchronously
returned as the response. Besides the actual data, an app may also require information about the usage policy associated
with this data to be able to enforce usage control. To not be bound to a specific payload format, this additional
information is not sent as part of the payload, but rather added in HTTP headers. Thus, it is still sent in the same
request as the corresponding data, but is clearly separated from it. Camel offers the possibility to set Camel headers
for a message, that in case of communicating via HTTP will be mapped to HTTP headers once an endpoint is called. The
Camel headers can therefore be used for setting the additional information.

The additional headers for calling a data app are:
- **TargetDataUri**: URI of the artifact that is being transferred
- **ContractId**: ID of the contract associated with the transferred artifact
- **AppName**: name of the app the message is sent to
- **AppUri**: URI of the app the message is sent to

Based on this, a route that includes a data app looks as follows:

    <route id="backend-to-app-to-dsc-example">
    
        <from uri="timer://foo?delay=10000&amp;period=20000"/>
        <setHeader name="CamelHttpMethod">
            <constant>GET</constant>
        </setHeader>
        <to uri="http://http-demo-backend:8090/demo"/>
        <convertBodyTo type="java.lang.String"/>

        <setHeader name="TargetDataUri">
            <constant>
                http://dataspace-connector:8080/api/artifacts/927906f2-5ee1-4678-9ace-5f1f2368606c
            </constant>
        </setHeader>
        <setHeader name="ContractId">
            <constant>
                http://dataspace-connector:8080/api/agreements/533012aa-25d4-49fe-a3e1-56aa37777b52
            </constant>
        </setHeader>
        <setHeader name="AppName">
            <constant>Demo App</constant>
        </setHeader>
        <setHeader name="AppUri">
            <constant>http://demo-app:5000</constant>
        </setHeader>
        
        <setHeader name="CamelHttpMethod">
            <constant>POST</constant>
        </setHeader>
        <to uri="http://demo-app:5000?socketTimeout=10000"/>

        <setHeader name="CamelHttpMethod">
            <constant>PUT</constant>
        </setHeader>
        <setHeader name="Authorization">
            <constant>Basic YWRtaW46cGFzc3dvcmQ=</constant>
        </setHeader>
        <to uri="http://dataspace-connector:8080/api/artifacts/927906f2-5ee1-4678-9ace-5f1f2368606c/data"/>

    </route>

This example route fetches data from an HTTP backend, sets the usage control relevant headers using the *setHeader* tag
and then sends the data and the headers to a data app. The response returned by the app is sent to the Dataspace
Connector as the data of an existing resource.

## Contributing

You are very welcome to contribute to this project when you find a bug, want to suggest an
improvement, or have an idea for a useful feature. Please find a set of guidelines at the
[CONTRIBUTING.md](CONTRIBUTING.md) and the [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) for details.
