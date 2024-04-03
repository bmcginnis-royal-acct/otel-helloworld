# Hello-Otel

Hello world open telemetry. Run as stand-alone only,
does not include 
kubernetes/istio. 

# System startup

## Start Jaeger in local docker



This instruction taken from Jaeger: https://www.jaegertracing.io/docs/1.40/getting-started/#all-in-one
```
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 14250:14250 \
  -p 14268:14268 \
  -p 14269:14269 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.40

```

Next, open the jaeger UI on http://localhost:16686.

## Start Prometheus and Grafana

```
// Run in terminal foreground
docker compose up

// ...or... Run in terminal background
docker compose up -d
```

To shut down:
```
docker compose down
```

To check if tools are running in docker:
```
docker ps | grep prometheus
docker ps | grep grafana
```
then check prometheus is ready to use:
http://localhost:9090/status

then check grafana ready to use open:
http://localhost:3000/datasources
then click on the 'prometheus' data source in the
web page, scroll to the bottom of page and click the
'Test' button. It should check ok and turn green.

You can use the tools with their web ui:
* Prometheus - http://localhost:9090
* Grafana - http://localhost:3000

## OpenTelemetry Collector
The open telemetry collector uses the config file
in /tool-config/open-telemetry-collector.yaml

It exposes the endpoint 5555 for otel protocol from your app. 

To start it:
```
docker run \
 -v $(pwd)/tool-config/open-telemetry-collector.yaml \
 -p 5555:5555 \
 otel/opentelemetry-collector:latest
```
When you see the output contain the phrase 
*"Everything is ready. Begin running and processing data."* it's ready to use.

In order to use the otel collector, we have to tell our
service to use our collector as the destination for its traces and metrics. 
You'll use a special startup script to run for otel collector:
```
$ run-with-auto-inst-to-otel-collector.sh
```

## Build target and run on command line

### Downloading otel java agent
If you haven't done so already, download the open
telemetry agent and put it in your system.
Follow instructions from open telemetry:
https://opentelemetry.io/docs/instrumentation/java/automatic/

Copy or link the opentelemetry-javaagent.jar file to 
this project's /otel-dist directory.


### Startup Server

Use the following shell script to rebuild and run
the service with auto-instrmented otel tracing, etc.

**IMPORTANT**: You have to rebuild every time because we
use a javaagent that runs with the target built jar file.
Local changes in your IDE won't be picked up by the agent. 

It will send tracing to your running localhost Jaeger (see instructions above).

From project home dir:
```
$ ./run-with-auto-instrumentation.sh

```
## Details about running with otel auto-instrumentation

For list of otel env variables and properties for configuring exporters:
See: https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md

```
// First, tell opentelemetry library to dump trace and metrics output to logging (console). 
// Otherwise, you'll get an error it will try to send to servers on localhost.
// config later with urls to these monitor tools. 
// For details on settings for jaeger and env vars for it,
// See: https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#jaeger-exporter
//

// Use this for sending traces and metrics to logger. 
export OTEL_TRACES_EXPORTER=logging-otlp
export OTEL_METRICS_EXPORTER=logging-otlp

// -- Jaeger localhost --
// Use these env variables to send tracing to jaeger.
// property: otel.traces.exporter=jaeger
// IF not specified, otel will send to http://localhost:14250
// property: otel.exporter.jaeger.endpoint
//
export OTEL_TRACES_EXPORTER=jaeger
export OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14250



// For logging traces to console only
java -javaagent:/Users/bmcginnis@rccl.com/Brian/devtools/otel/opentelemetry-javaagent.jar \
-jar target/hello-otel-0.0.1-SNAPSHOT.jar

// An example of a more elaborate javaagent command that doesn't use
// any env variables. 
$ java -javaagent:/Users/bmcginnis@rccl.com/Brian/devtools/otel/opentelemetry-javaagent.jar \
                   -Dotel.trace.exporter=jaeger \
                   -Dotel.exporter.jaeger.endpoint=localhost:14250 \
                   -Dotel.resource.attributes=service.name=ServiceZero-OtelDemo \
                   -Dotel.javaagent.debug=false \
                   -Dotel.metrics.exporter=logging-otlp
```

Run as java opts:

```
JAVA_OPTS=${JAVA_OPTS} \
-Dotel.exporter=jaeger \
-Doter.jaeger.endpoint=blah \
-javaagent:${APP_HOME}/resources/myjars/opentelemetry-javaagent.jar"
```

# Dual Server Configuration
To fully demo distributed tracing, run this app as two separate instances
and vary them slightly via configuration and what traffic you send to each of them. 

The cartService and InventoryService reside in same Service code. 
Doing a demo of Jaeger isn't ideal because it's hard to see the difference in traces
due to the app being the same. 

In this configuration, we run the Service in 2 separate windows.
It is important that all CartService calls go to Server 1 and all Inventory Service
calls go to Server 2.

Server 1 = port set to 8080 (default). INVENTORY-BASE-URL=http://localhost:8090/inventory}

Server 2 = SERVER_PORT=8090; 

The distributed trace use case is lazy and contrived, but it works:
```
Server 1 = Logical Cart Service
Server 2 = Logical Inventory Service

Postman GET Cart http://localhost:8080/cart/1 => Cart Service
Cart Service POST cart  http://localhost:8090/inventory/cartset => Inventory Service

Cart Service fetches the cart then calls Inventory Service to create a cart with item inventory levels.
```

## Server 1 aka Cart Service

```
export OTEL_METRICS_EXPORTER=logging-otlp
export OTEL_TRACES_EXPORTER=jaeger
export OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14250
export INVENTORY_BASE_URL=http://localhost:8090/inventory

java -javaagent:/Users/bmcginnis@rccl.com/Brian/devtools/otel/opentelemetry-javaagent.jar \
-jar target/hello-otel-0.0.1-SNAPSHOT.jar
```
## Server 2 aka Inventory Service
```
export SERVER_PORT=8090
export OTEL_METRICS_EXPORTER=logging-otlp
export OTEL_TRACES_EXPORTER=jaeger
export OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14250


java -javaagent:/Users/bmcginnis@rccl.com/Brian/devtools/otel/opentelemetry-javaagent.jar \
-jar target/hello-otel-0.0.1-SNAPSHOT.jar
```

## View Cart / Inventory traffic with Jaeger

Run the following:
```
$ curl --request GET 'http://localhost:8080/cart/1'
$ curl --request GET 'http://localhost:8080/cart/2'
$ curl --request GET 'http://localhost:8080/cart/3'
```

Now view the Jaeger web UI and you should see some traces.

# The journey

I started from spring initializr, see /doc for the
starting point artifacts. This is spring boot 3.0, 
webflux project. 

The idea is to build a simple as possible, stand-alone
demo of using open telemetry, then build upon learnings.

## Modify pom.xml

# Smoke tests

## Manual Smoke Test
```
// Happy path - get cart by id, 2nd retry
curl --request GET 'http://localhost:8080/cart/1' --header 'retry: 2'

// Happy path - get all carts 
curl --request GET 'http://localhost:8080/cart'

// Unhappy path - Cart Not Found
curl --request GET 'http://localhost:8080/cart/666'

```


# Resources
Based on articles:

Best overview article of otel:
https://www.aspecto.io/blog/what-is-opentelemetry-the-infinitive-guide/

Good diagrams of otel tracing: https://blog.frankel.ch/end-to-end-tracing-opentelemetry/

Best pictures of otel and jaeger. How to run jaeger locally w/ docker too. 
- https://medium.com/jaegertracing/jaeger-tracing-a-friendly-guide-for-beginners-7b53a4a568ca

Initial article - https://allopensourcetech.com/integrating-spring-boot-with-opentelemetry/
Reactive prog spring 3 refresher - https://dzone.com/articles/webflux-reactive-programming-with-spring-part-3