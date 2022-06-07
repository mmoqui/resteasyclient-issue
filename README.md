# resteasyclient-issue

A reproducer of an issue found in Resteasy client 4.7.6 within Wildfly 26.

The issue is that Resteasy client doesn't use anymore Jackson to deserialize JSON entities in HTTP responses. Because the integration tests, and hence Resteasy client, are used in the same deployment than the REST-based resource targeted by the test, and because Jackson is used by default by JAX-RS impl in Wildfly to serialize/deserialize JSON entities, we expect the same with Resteasy client.

The issue is detected in the integration `SubscriptionResourceIT` test. The test works fine in Wildfly <= 24.0.1 (that uses Resteasy 3.15.1) but fails in Wildfly 26.1.1 (that uses Resteasy 4.7.6) for the reason above.

To reproduce the pb, please download and install Wildfly 26.1.1. To run the integration tests, first start wildfly:

  ```
  $ ${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml --debug 5005
  ```

Then run the integration tests:

  ```
  $ mvn clean install -Dcontext=ci
  ```
  
To run explicitly a given test, for instance `SubscriptionResourceIT` in which the pb was found:

  ```
  $ mvn clean install -Dcontext=ci -Dit.test=SubscriptionResourceIT
  ```
  
