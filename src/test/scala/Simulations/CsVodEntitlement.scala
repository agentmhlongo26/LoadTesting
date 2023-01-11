package Simulations

import io.gatling.core.Predef.{Simulation, _}
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import java.text.SimpleDateFormat
import java.util.Calendar
import scala.concurrent.duration.DurationInt

class CsVodEntitlement extends Simulation {

  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

    /** Configure Protocol **/
  val httpConf = http
    .baseUrl("http://cs-vod-entitlement.dstvo.internal")
    //.proxy(Proxy("localhost", 8888).httpsPort(8888))
    .header("Accept", "application/json")

  /** Runtime Parameters **/
  def userCount: Int = getProperty("USERS", "1").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "30").toInt

  /** Helper Methods **/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }


  /** Test Data CSV **/
  val csvFeeder = csv("data/connectId.csv").circular

  /** Scenarios **/


  def VodAuth():ChainBuilder = {
    feed(csvFeeder)
      .exec(http("GET vodAuth")
        .post("/cs-vod-entitlement/v2/vod-authorisation;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;platformId=32faad53-5e7b-4cc0-9f33-000092e85950;connectId=${connectId};userIp=41.0.0.1;deviceType=Web")
        .header("CloudFront-Viewer-Country", "ZA")
        .check(status.is(200))
      )}

  val scn = scenario("Check Vod Authorization ")

    .forever() {
      exec(VodAuth())
        .pause(1)

    }



  /** Load Test Simulation **/
  setUp(
    scn.inject(
      nothingFor(2 seconds),
      rampUsers(userCount) during (rampDuration seconds))
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }

}
