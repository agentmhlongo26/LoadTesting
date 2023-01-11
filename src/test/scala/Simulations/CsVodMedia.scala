package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import scala.concurrent.duration._

class CsVodMedia extends Simulation {

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
    //.baseUrl("http://cs-vod-media.dstvo.internal")
    .baseUrl("https://public-ingress-prd-230570154.eu-west-1.elb.amazonaws.com")
    .header("Accept", "application/json")

  /** Runtime Parameters * */
  def userCount: Int = getProperty("USERS", "5").toInt

  def maxUserCount: Int = getProperty("MAX_USERS", "2").toInt

  def maxUserRampDuration: Int = getProperty("MAX_USER_RAMP_DURATION", "2").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "120").toInt

  /** Helper Methods * */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data CSV * */
  val paramsFeeder = csv("data/LeanCatalogue.csv").random

  /** Object definition * */
  def getCatalogueCountryPackage(): ChainBuilder = {
    feed(paramsFeeder)
      .exec(http("GET | Catalogue Items for Package and Country")
        .get("/cs-vod-media/v2/catalogue;productId=${productId};platformId=${platformId};countryCode=${countryCode};subscriptionPackageId=${packageId};videoAssetsFilter=${videoAssetsFilter};useMultiDRMAssets=${useMultiDRMAssets};includeFutureDatedItems=${includeFutureDatedItems}")
        .check(status.is(200)))
  }

  def getVideoItems(): ChainBuilder = {
    feed(paramsFeeder)
      .exec(http("GET | Catalogue Items for Package and Country")
        .get("/cs-vod-media/v2/video;productId=${productId};platformId=${platformId};countryCode=${countryCode};subscriptionPackageId=${packageId};videoAssetsFilter=${videoAssetsFilter};useMultiDRMAssets=${useMultiDRMAssets};includeFutureDatedItems=${includeFutureDatedItems}")
        .check(status.is(200)))
  }

  def getLastChanceItems(): ChainBuilder = {
    feed(paramsFeeder)
      .exec(http("GET | Last Chance items")
        .get("/cs-vod-media/v2/catalogue/lastChance;productId=${productId};platformId=${platformId};countryCode=${countryCode};subscriptionPackageId=${packageId};sortOrder=${sortOrder}")
        .check(status.is(200)))
  }

  def getCatchupNavigation(): ChainBuilder = {
    feed(paramsFeeder)
      .exec(http("GET | Catch-up Navigation")
        .get("/cs-vod-media/v2/navigation/catchUpNavigation;product=${productId};platformId=${platformId};language=EN;country=${countryCode};subscriptionPackage=${packageId}")
        .check(status.is(200)))
  }

  def getBillboardCollections(): ChainBuilder = {
    feed(paramsFeeder)
      .exec(http("GET | Billboard Collections")
        .get("/cs-vod-media/v2/billboard;productId=${productId};platformId=${platformId};countryCode=${countryCode};subscriptionPackage=${packageId}")
        .check(status.is(200)))
  }

  def gerEditorialLists(): ChainBuilder = {
    feed(paramsFeeder)
      .exec(http("GET | Editorial Lists")
      .get("/cs-vod-media/v2/editorialList;productId=${productId};platformId=${platformId};countryCode=${countryCode};subscriptionPackageId=${packageId}")
        .check(status.is(200)))
  }

  /** Scenario definition * */
  /*val catalogue = scenario("Get Catalogue").forever() {
    exec(getCatalogueCountryPackage()).pause(1000 milliseconds)
  }*/
  val videoItems = scenario("GET Video Items").forever() {
    exec(getLastChanceItems()).pause(1000 milliseconds)
  }
  val lastChange = scenario("GET Last Chance").forever() {
    exec(getLastChanceItems()).pause(1000 milliseconds)
  }
  val catchupNavigation = scenario("GET Catchup Navigation").forever() {
    exec(getCatchupNavigation()).pause(1000 milliseconds)
  }
  val billboards = scenario("GET Billboards").forever() {
    exec(getBillboardCollections()).pause(1000 milliseconds)
  }
  val editorialLists = scenario("GET Editorial Lists").forever() {
    exec(gerEditorialLists()).pause(1000 milliseconds)
  }

  /** Load Test Simulation * */
  setUp(
    //catalogue.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    videoItems.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    lastChange.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    catchupNavigation.inject(rampUsers(maxUserCount) during (maxUserRampDuration)),
    billboards.inject(rampUsers(maxUserCount) during (maxUserRampDuration)),
    editorialLists.inject(rampUsers(maxUserCount) during (maxUserRampDuration))
  )
  .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }
}
