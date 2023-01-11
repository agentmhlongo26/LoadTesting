package Simulations

import io.gatling.core.Predef.{Simulation, _}
import io.gatling.http.Predef._
import io.gatling.core.Predef._

  import scala.concurrent.duration.DurationInt

  class userInfo extends Simulation {

    before {
      println(s"Running test with ${userCount} users")
      println(s"Ramping users over ${rampDuration} seconds")
      println(s"Total Test duration: ${testDuration} seconds")
    }

    /** Configure Protocol **/
    val httpConf = http
      .baseUrl("http://ssl.dstv.com")
      .header("Accept", "application/json")

    /** Runtime Parameters **/
    def userCount: Int = getProperty("USERS", "2").toInt
    def rampDuration: Int = getProperty("RAMP_DURATION", "5").toInt
    def testDuration: Int = getProperty("DURATION", "5").toInt

    /** Stair Case Runtime Parameters */
    def incrementUserCount: Int = getProperty("INCREMENT_USERS", "2").toInt
    def noOfSteps: Int = getProperty("NO_OF_STEPS", "5").toInt
    def startingUsers: Int = getProperty("STARTING_USERS", "2").toInt
    def levelDuration: Int = getProperty("LEVEL_DURATION", "5").toInt
    def stepRampDuration: Int = getProperty("STEP_RAMP_DURATION", "2").toInt

    /** Helper Methods **/
    private def getProperty(propertyName: String, defaultValue: String) = {
      Option(System.getenv(propertyName))
        .orElse(Option(System.getProperty(propertyName)))
        .getOrElse(defaultValue)
    }

    /** Test Data CSV **/
    val csvFeeder = csv("data/accesstoken.csv").circular

    /** API Calls **/
    def userInfo() = {
      feed(csvFeeder)
      exec(http("GET | user info")
        .get("/api/user/info")
        .header("Authorization", "${accesstoken}")
        .check(status.is(200)))
    }

    def userInfoNoAuth() = {
      feed(csvFeeder)
      exec(http("GET | user info No Auth")
        .get("/api/user/info/noauth")
        //.header("Authorization", "${accesstoken}")
        .check(status.is(200)))
    }


    /** Scenarios **/
    val scn = scenario("userInfo")
      .forever() {
        exec(userInfo())
          .pause(2)
      }

    /** Load Test Simulation **/
    setUp(
      scn.inject(
        incrementConcurrentUsers(incrementUserCount)
          .times(noOfSteps)
          .eachLevelLasting(levelDuration seconds)
          .separatedByRampsLasting(stepRampDuration seconds)
          .startingFrom(startingUsers)
      )
    )
      .protocols(httpConf)
      .maxDuration(testDuration seconds)

    after {
      println("Load Test completed")
    }
}
