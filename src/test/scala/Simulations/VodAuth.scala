package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import java.util.Calendar
import java.text.SimpleDateFormat

import com.fasterxml.jackson.databind.JsonNode
import io.gatling.core.check.MultipleFindCheckBuilder
import io.gatling.core.check.jsonpath.{JsonPathCheckType, JsonPathOfType}

import scala.concurrent.duration.DurationInt

class VodAuth extends Simulation {

  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  object MyClass {
    def main(args: Array[String]) {
      val form = new SimpleDateFormat("yyyy / MM / dd");
      val c = Calendar.getInstance();

      println("Present Date : " + c.getTime());

      val formattedDate = form.format(c.getTime());
      println("Date formatted : "+formattedDate);
    }
  }

  /** Configure Protocol **/
  val httpConf = http
    .baseUrl("https://ssl.dstv.com/api")
    //.proxy(Proxy("localhost", 8888).httpsPort(8888))
    .header("Accept", "application/json")

  /** Runtime Parameters **/
  def userCount: Int = getProperty("USERS", "15").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "120").toInt

  /** Helper Methods **/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }


  /** Test Data CSV **/
  val csvFeeder = csv("sessionId.csv").circular


  /** Scenarios **/


  def VodAuth():ChainBuilder = {
    val path: String = "/cs-mobile/user-manager/v5/vod-authorisation;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;platformId=32faad53-5e7b-4cc0-9f33-000092e85950;deviceType=Web"

    feed(csvFeeder)
      .exec(http("GET vodAuth")
        .post(path)
        .header("Authorization", "${sessionId}")
        .header("X-Forwarded-For", "41.0.0.0")
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
