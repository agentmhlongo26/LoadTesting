# Connected Video: Load & Performance Tests using Gatling

**Technology Stack**
- Scala
- Akka
- Netty

**Usage**
Option 1: Execute tests using the *Engine* or *MyGatlingRunner* class

Option 2: Execute tests using the command line
```
mvn gatling:test -Dgatling.simulationClass=templates.GatlingTemplate -DUSERS=10 -DRAMP_DURATION=5 -DDURATION=30
```

**Reporting**
An HTML report for the test execution can be found in the below folder location:
- ../target/gatling/<test-name>-.../index.html