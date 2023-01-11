package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps

class UniversalPlus extends Simulation {

  before {
    println(s"Running test with ${userCount} users")
    println(s"Running test with ${maxUserCount} max users")
    println(s"Ramping to max users over ${maxUserRampDuration} seconds")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol * */
  val httpConf = http

    /** Prod configuration * */

    .baseUrl("https://alpha-ssl.dstv.com")
    .header("Accept", "application/json")
  //.proxy(Proxy("localhost", 8888).httpsPort(8888))

  /** Runtime Parameters * */
  def userCount: Int = getProperty("USERS", "5").toInt

  def maxUserCount: Int = getProperty("MAX_USERS", "2").toInt

  def maxUserRampDuration: Int = getProperty("MAX_USER_RAMP_DURATION", "2").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "60").toInt

  /** Helper Methods * */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data CSV * */
  val countryAndPackage_Data = csv("data/universalplus.csv").circular

  /** Object definition * */
  def getChannelGroupByCountyAndPackage(): ChainBuilder = {
    feed(countryAndPackage_Data).
    exec(http("GET | All channelGroupByCountyAndPackage")
      .get("/api/lists/channel_group_sections?country_code=" + "${country}" + "&subscription_package=" + "${package}")
      .check(status.is(200)))
  }

  /** Object definition * */
  def getCatalogueByPackageCountry(): ChainBuilder = {
    feed(countryAndPackage_Data).
    exec(http("GET | All catalogueByPackageCountry")
      .get("/api/cs-mobile/now-content/v7/catalogueByPackageAndCountry;productId=" + "${productId};platformId=" + "${platformId};subscriptionPackage=" + "${package};country=" + "${country};channelId=HDT,VAM,M1D,MAP,MCH,MZE" + ";page=" + "${page};pageSize=" + "${pageSize}")
      .check(status.is(200)))
  }

  /** Scenario definition * */
  val channelGroupByCountyAndPackage = scenario("Get channelGroupByCountyAndPackage").forever() {
    exec(getChannelGroupByCountyAndPackage()).pause(1000 milliseconds)
  }

  /** Scenario definition * */
  val catalogueByPackageCountry = scenario("Get catalogueByPackageCountry").forever() {
    exec(getCatalogueByPackageCountry()).pause(1000 milliseconds)
  }

  /** Load Test Simulation * */
  setUp(
    channelGroupByCountyAndPackage.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    catalogueByPackageCountry.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds))
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }
}

