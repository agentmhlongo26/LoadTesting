package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import scala.util.Random

import scala.concurrent.duration.DurationInt

class ConcurrencySessionManager extends Simulation{

  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol **/
  val httpConf = http
    //.baseUrl("https://009esau2t4.execute-api.af-south-1.amazonaws.com/api")
    .baseUrl("https://ssl.dstv.com/")
    //.proxy(Proxy("localhost", 8888).httpsPort(8888))
    .header("Accept", "application/json")

  /** Runtime Parameters **/
  def userCount: Int = getProperty("USERS", "15").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "120").toInt

  //var newEventId: String = "$.id"

  /** Helper Methods **/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data CSV **/
  val csvFeeder = csv("data/sessionId.csv").circular
  val feeder = Iterator.continually(Map("deviceId" -> (Random.alphanumeric.take(10).mkString + "-test-device")))

  def getSessionDevice1():ChainBuilder = {

    val path: String = "/api/cs-mobile/user-manager/v6/getConcurrentSession;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;platformId=32faad53-5e7b-4cc0-9f33-000092e85950;deviceId=" + "${deviceId}" + ";sessionType=stream;platformType=web"
    feed(feeder).
    feed(csvFeeder)
      .exec(http("Session Device 1")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.in(200, 403))
      )}

  def getSessionDevice2():ChainBuilder = {
    val path: String = "/api/cs-mobile/user-manager/v6/getConcurrentSession;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;platformId=32faad53-5e7b-4cc0-9f33-000092e85950;deviceId=" + "${deviceId}" + ";sessionType=stream;platformType=web"
    feed(feeder).
    feed(csvFeeder)
      .exec(http("Session Device 2")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.in(200, 403))
      )}

  //def getSessionDevice3():ChainBuilder = {
    //val path: String = "/api/cs-mobile/user-manager/v6/getConcurrentSession;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;platformId=32faad53-5e7b-4cc0-9f33-000092e85950;deviceId=" + "${deviceId}" + ";sessionType=stream;platformType=web"
    //feed(feeder).
    //feed(csvFeeder)
      //.exec(http("Session Device 3")
        //.get(path)
       // .header("Authorization", "${sessionId}")
       // .check(status.in(200, 403))
     // )}


  val scn = scenario("Check Concurrency ")

    .forever() {
      exec(getSessionDevice1)
        .pause(30)
        .exec(getSessionDevice2)
        .pause(30)


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
