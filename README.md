## Ordering Application

A spring boot based application for managing orders, products and ingredient stocks.

The application is hosted on Google Cloud Platform (GCP) App Engine service and data is in Firestore NoSQL database.

<a href="https://inspired-bazaar-431614-v1.nw.r.appspot.com/api/swagger-ui/index.html#" target="_blank">Swagger
Documentation</a>

### Before Running the Application:

1- Make sure Java Development Kit (JDK) version **17** is installed and added in **JAVA_HOME** in your environment
variables.

2- Add **credentials.json** file in the main folder (which contains the README file) to be able to communicate with
Firestore database.

3- Edit the **email.to** property value inside **application.properties** with desired email to send ingredient stock
notification.

### How to run the application:

You can run **OrderingApplication** class using your IDE or using maven by running **runApplication.bat** file

### How to run unit test:

You can run **OrderServiceTest** class using your IDE or using maven by running **runUnitTest.bat** file

