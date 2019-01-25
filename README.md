# Business Register Unit API
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](./LICENSE)

Supports unit search.


### Development Tasks

Run the service in development mode (default Play port of 9000):

    sbt run

Run the service in development mode (custom port of 9123):

    sbt "run 9123"

Run unit tests with coverage:

    sbt clean coverage test coverageReport

Run acceptance tests:

    sbt it:test

Run all tests:

    sbt clean test it:test

Generate static analysis report:

    sbt scapegoat


#### Testing Against a Local Solr Instance

1.  Start Solr

        bin/solr start -cloud -p 8983
        bin/solr start -cloud -p 7574 -z localhost:9983

2.  Create Collection

        bin/solr create -c unit -s 2 -rf 2 -p 8983

3.  Define Schema

        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"unit_id", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"name", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"trading_style", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"legal_status", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"sic", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"turnover", "type":"pint", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"paye_jobs", "type":"pint", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"address1", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"address2", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"address3", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"address4", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"address5", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"postcode", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"unit_type", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema
        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"parent_unit_id", "type":"text_general", "multiValued":false, "stored":true}}' http://localhost:8983/solr/unit/schema

        curl -X POST -H 'Content-type:application/json' --data-binary '{"add-copy-field": {"source":"*","dest":"_text_"}}' http://localhost:8983/solr/unit/schema

4.  Add Sample Documents

        curl -X POST -H 'Content-Type: application/json' 'http://localhost:8983/solr/unit/update/json/docs?commitWithin=1000' --data-binary '
        {
          "unit_id":"065H7Z31732",
          "name":"BIG BOX CEREAL LIMITED",
          "trading_style":"BIG BOX CEREAL",
          "legal_status":"A",
          "sic":"6616",
          "paye_jobs":130,
          "address1":"LANE TOP FARM",
          "address2":"1 BOTTOM LANE",
          "address3":"BLACKSHAW HEAD",
          "address4":"HEBDEN BRIDGE",
          "address5":"WEST YORKSHIRE",
          "postcode":"SS5 4PR",
          "unit_type":"PAYE",
          "parent_unit_id":"1000012345000080"
        }'

    The collection can now be queried via:
    * the Solr Admin Interface at [http://localhost:8983/solr/#/unit/query](http://localhost:8983/solr/#/unit/query)
    * the HTTP endpoint at `curl -i 'http://localhost:8983/solr/unit/select?q=*:*'`

5.  Start this Service

        export BR_UNIT_SOLR_COLLECTION=unit
        export BR_UNIT_SOLR_CLOUD_ZOOKEEPER_HOST_A=localhost:9983

        sbt clean run

6.  Perform a Search via this API

        curl -i 'http://localhost:9000/v1/unit?searchTerm=065H7Z31732'

7.  Shutdown This Service

    Terminate the running command (typically Ctrl-C).

8.  Delete Collection

        bin/solr delete -c unit -p 8983

9.  Shutdown Solr

        bin/solr stop -all


### API Specification
The `api.yaml` file in the root project directory documents the API using the [Open API Specification](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.2.md).
This file is best edited using [Swagger Editor](https://github.com/swagger-api/swagger-editor) and best viewed using [Swagger UI](https://github.com/swagger-api/swagger-ui/).
The Docker approach outlined [here](https://github.com/swagger-api/swagger-editor#docker) seems to work well.


### Tracing
[kamon](http://kamon.io) is used to automatically instrument the application and report trace spans to
[zipkin](https://zipkin.io/).  The AspectJ Weaver is required to make this happen, see [adding-the-aspectj-weaver](http://kamon.io/documentation/1.x/recipes/adding-the-aspectj-weaver/)
for further details.

Kamon takes care of propagating the traceId across threads, and making the relevant traceId available to
logback's Mapped Diagnostic Context (MDC).

Tracing is not enabled during the execution of tests, resulting in log statements that contain a traceId
with the value "undefined".

To undertake manual trace testing, run a local Zipkin 2 server.  One simple way to do this is via Docker:

    docker run --rm -d -p 9411:9411 openzipkin/zipkin:2.11

Then run this service via `sbt run`, and exercise an endpoint.

The resulting trace information should be available in the Zipkin UI at
[http://localhost:9411/zipkin/](http://localhost:9411/zipkin/).


### Service Configuration
As is standard for Play, the runtime configuration file can be found at `src/main/resources/application.conf`.

This file adopts a pattern where each variable has a sensible default for running the application locally,
which may then be overridden by an environment variable (if defined).  For example:

    host = "localhost"
    host = ${?BR_UNIT_TRACING_REPORTER_HOST}

The actual settings used for our formal deployment environments are held outside of Github, and rely on the
the ability to override settings via environment variables in accordance with the '12-factor app' approach.

Note that acceptance tests (and the entire IntegrationTest phase generally) use a dedicated configuration
that is defined at `src/it/resources/it_application.conf`.  This imports the standard configuration, and then
overrides the environment to that expected by locally executing acceptance tests.  This allows us to specify
non-standard ports for example, to avoid conflicts with locally running services.  For this to work, the
build file overrides the `-Dconfig.resource` setting when executing the IntegrationTest phase.
