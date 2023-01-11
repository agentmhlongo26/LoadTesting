package Simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BaselineVodSections extends Simulation {
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
    val getVodSections = forever() {
      feed(paramsFeeder).
        feed(feeder)
        .exec(http("GET | Vod No-auth Sections")
          .get("/api/cs-mobile/v7/application/vod/no-auth/sections;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;country=ZA;subscriptionPackageId=PREMIUM;platformId=${platformId}")
          .header("Authorization", "${sessionId}")
          .check(status.in(200, 304)))
        .pause(1000 milliseconds)
    }
  }

  /** Scenarios * */
  val getVodSectionsApiCall = scenario("Vod no-auth Sections").exec(Browse.getVodSections)

  /** Load Simulation * */
  setUp(
    getVodSectionsApiCall.inject(rampUsers(userCount) during (rampDuration seconds))
  ).protocols(httpConf)
    .maxDuration(testDuration seconds)
}