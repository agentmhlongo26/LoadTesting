package Simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class BaselineLiveTVAllChannels extends Simulation {
  /** Configure Protocol * */
  val httpConf = http
    .baseUrl("https://ssl.dstv.com")
    .acceptHeader("*/*")
    .contentTypeHeader("application/json")
    //.proxy(Proxy("localhost", 8888).httpsPort(8888))

  /** Ramp Up Runtime Parameters * */
  def userCount: Int = getProperty("USERS", "5").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "1").toInt

  def testDuration: Int = getProperty("DURATION", "30").toInt

  /** Helper Methods * */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data Feeder */
  val feeder = csv("data/objects.csv").circular
  val paramsFeeder = csv("data/params.csv").circular

  /** API Calls * */
  object Browse {
    val getAllChannels = forever() {
      feed(paramsFeeder).
        feed(feeder)
        .exec(http("GET | channels")
          .get("/api/cs-mobile/v7/epg-service/channels/events;genre=ALL;country=ZA;packageId=PREMIUM;count=2;utcOffset=+02:00;platformId=${platformId}")
          .header("Authorization", "${sessionId}")
          .check(status.in(200, 304)))
        .pause(1000 milliseconds)
    }
  }

  /** Scenarios * */
  val getAllChannelsApiCall = scenario("All channels").exec(Browse.getAllChannels)

  /** Load Simulation * */
  setUp(
    getAllChannelsApiCall.inject(rampUsers(userCount) during (rampDuration seconds))
  ).protocols(httpConf)
    .maxDuration(testDuration seconds)
}