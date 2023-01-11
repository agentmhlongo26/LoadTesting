package Simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class CsMobileNavigationBugFix extends Simulation {
  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol **/
  val httpConf = http
    .baseUrl("http://cs-mobile-profile-canary.dstvo.internal")

  /** Runtime Parameters **/
  def userCount: Int = getProperty("USERS", "5").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "1").toInt

  def testDuration: Int = getProperty("DURATION", "30").toInt

  /** Helper Methods **/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data CSV **/
  val csvFeeder = csv("data/bugFix.csv").circular

  /** API Calls **/
  def getRecentlyWatchedChannels() = {
    feed(csvFeeder).
      exec(http("GET | cs-mobile/application/navigationmenu")
        .get("/cs-mobile/v5/application/navigationmenu;platformId=32faad53-5e7b-4cc0-9f33-000092e85950")
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  /** Scenarios **/
  val scn = scenario("CsMobile Navigation Menu")
    .forever() {
      exec(getRecentlyWatchedChannels())
    }

  /** Load Test Simulation **/
  setUp(
    scn.inject(
      nothingFor(1 seconds),
      rampUsers(userCount) during (rampDuration seconds))
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }
}
