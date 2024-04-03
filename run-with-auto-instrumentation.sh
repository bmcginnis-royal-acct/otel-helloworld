#!/bin/bash

export OTEL_METRICS_EXPORTER=logging-otlp
export OTEL_TRACES_EXPORTER=jaeger
export OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14250
export OTEL_RESOURCE_ATTRIBUTES=service.name=hello-otel,service.version=v1.0
export INVENTORY_BASE_URL=http://localhost:8080/inventory

echo "====================="
echo "Rebuild & Run stand-alone with auto-instrumented OpenTelemetry sent to localhost Jaeger..."
echo "OTEL_METRICS_EXPORTER :         $OTEL_METRICS_EXPORTER"
echo "OTEL_TRACES_EXPORTER :          $OTEL_TRACES_EXPORTER"
echo "OTEL_RESOURCE_ATTRIBUTES :      $OTEL_RESOURCE_ATTRIBUTES"
echo "OTEL_EXPORTER_JAEGER_ENDPOINT : $OTEL_EXPORTER_JAEGER_ENDPOINT"
echo "INVENTORY_BASE_URL :            $INVENTORY_BASE_URL"
echo "====================="
echo ""

mvn clean package -Dmaven.test.skip=true


java -javaagent:./otel-dist/opentelemetry-javaagent.jar \
-jar target/hello-otel-0.0.1-SNAPSHOT.jar