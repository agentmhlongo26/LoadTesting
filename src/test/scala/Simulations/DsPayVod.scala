package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import scala.concurrent.duration._

class DsPayVod extends Simulation {

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
    //.baseUrl("http://ds-pay-vod.dstvo.internal:8080")
    .baseUrl("https://public-ingress-prd-230570154.eu-west-1.elb.amazonaws.com")
    .header("Accept", "application/json")

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
  //val paramsFeeder = csv("").random

  /** Object definition * */
  def getVideoAssets(): ChainBuilder = {
    exec(http("GET | All videoAssets")
      .get("/ds-pay-vod/videoAsset")
      .check(status.is(200)))
  }

  def getVideoMeta(): ChainBuilder = {
    exec(http("GET | All videoMeta")
      .get("/ds-pay-vod/videoMeta;filterField=categorisations.product;categorisations.productFilterValue=1b09957b-27aa-493b-a7c9-53b3cec92d63")
      .check(status.is(200)))
  }

  def getVideoPackage(): ChainBuilder = {
    exec(http("GET | All videoPackages")
      .get("/ds-pay-vod/videoPackage")
      .check(status.is(200)))
  }

  def getEditorialList(): ChainBuilder = {
    exec(http("GET | All editorialLists")
      .get("/ds-pay-vod/editorialList")
      .check(status.is(200)))
  }

  def getPrograms(): ChainBuilder = {
    exec(http("GET | All programs")
      .get("/ds-pay-vod/program")
      .check(status.is(200)))
  }

  def getSchedules(): ChainBuilder = {
    exec(http("GET | All schedules")
      .get("/ds-pay-vod/schedule")
      .check(status.is(200)))
  }

  def getPlaylist(): ChainBuilder = {
    exec(http("GET | All playlists")
      .get("/ds-pay-vod/playlist")
      .check(status.is(200)))
  }

  /** Scenario definition * */
  val videoAssets = scenario("Get videoAssets").forever() {
    exec(getVideoAssets()).pause(1000 milliseconds)
  }
  val videoMetas = scenario("GET videoMeta").forever() {
    exec(getVideoMeta()).pause(1000 milliseconds)
  }
  val videoPackages = scenario("GET videoPackage").forever() {
    exec(getVideoPackage()).pause(1000 milliseconds)
  }
  val editorialLists = scenario("GET editorialList").forever() {
    exec(getEditorialList()).pause(1000 milliseconds)
  }
  val programs = scenario("GET program").forever() {
    exec(getPrograms()).pause(1000 milliseconds)
  }
  val schedules = scenario("GET schedule").forever() {
    exec(getSchedules()).pause(1000 milliseconds)
  }
  val playlists = scenario("GET playList").forever() {
    exec(getPlaylist()).pause(1000 milliseconds)
  }

  /** Load Test Simulation * */
  setUp(
    videoAssets.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    videoMetas.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    videoPackages.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    editorialLists.inject(rampUsers(maxUserCount) during (maxUserRampDuration)),
    programs.inject(rampUsers(maxUserCount) during (maxUserRampDuration)),
    schedules.inject(rampUsers(maxUserCount) during (maxUserRampDuration)),
    playlists.inject(rampUsers(maxUserCount) during (maxUserRampDuration))
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }
}
