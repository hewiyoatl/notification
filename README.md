### Dependencies
- Java 6 or 7
- SBT 0.12.3 or higher
- Running MySQL installation with our default config.
- (Optional) Ruby 1.9.3+ and rake

### Quick Start
```
# Compile everything
sbt compile

# Generate IntelliJ project
sbt gen-idea

# Create or migrate database
sbt 'project sms-service-database' run

# Run all unit and integration tests
sbt test

# Start the server process (default port 7070, admin port 9990)
sbt 'project sms-service-server' run
```

### Run commands
```
# database migrations
sbt 'project sms-service-database' run

# sms service (default port 7070, admin port 9990)
sbt 'project sms-service-server' run

# sms service on different port (e.g. 7071, admin port 9991)
sbt 'project sms-service-server' "run -com.twitter.finatra.config.port=localhost:7071 -com.twitter.finatra.config.adminPort=localhost:9991"
```

- There is a default 8080 server to handle Twilio callbacks. Config TODO.

### Test Commands
```
# Run all unit and integration tests 
sbt test

# database integration tests
sbt 'project sms-service-database-tests' test

# service-providers integration tests
sbt 'project sms-service-providers-tests' test
```

### Batch delivery commands (be careful with these!)
The command line batch sender takes an input file with phone numbers on a single line, or release an assigned phone number in Twilio.
```
# Usage:
<twilio account sid> <twilio auth token> batchsend <twilio from number> <message> <path to csv file>
sbt "project sms-service-providers"
 \ "run ACe23e59afd9d8fb945e6d048af1dd41f3 c2184023f7d0d5e524773506eda220ad batchsend 219-778-5911 \"here is a test http://www.loyal3.com\" foo.csv"
 
<twilio account sid> <twilio auth token> release <longcode>
sbt "project sms-service-providers"
 \ "run ACe23e59afd9d8fb945e6d048af1dd41f3 c2184023f7d0d5e524773506eda220ad release <longcode>"

```

### Packaging commands
```
# See rake targets
rake -T
```

### Module Descriptions

- sms-config: configuration stuff
- sms-core: core model classes and interfaces for SMS client and service
- sms-package: Used when building deployment package
- sms-service: REST service layer to providers and databases
- sms-service-database: Database access layer and migrations
- sms-service-database-tests: integration tests for database
- sms-service-providers: providers for sms services
- sms-service-providers-tests: integration tests for providers
- sms-service-server: Main class for starting the server
- tasks: Ruby build helpers
- test-tools: common utilities for testing
- tools: custom tools needed for build (e.g. SBT version to run on TeamCity)
 
- REST samples [HERE](REST_SAMPLES.md) 

- CocoaRest client samples in sample_rest.dat
