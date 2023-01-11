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

class GetLegacyCatalogues extends Simulation {
  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol **/
  val httpConf = http
    .baseUrl("http://canary-catalogue.dstvo.internal:8080")
  .proxy(Proxy("localhost", 8888).httpsPort(8888))

  /** Runtime Parameters **/
  def userCount: Int = getProperty("USERS", "5").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "5").toInt

  def testDuration: Int = getProperty("DURATION", "90").toInt

  /** Helper Methods **/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data Feeder */
  val Packagefeeder = csv("CachebleData/package.csv").circular
  val Countryfeeder = csv("EpgCsv/CountryList.csv").circular
  val platformFeeder = csv("data/params.csv").circular
  val GenrefFeeder = csv("data/bookmark.csv").circular
  val ProgramIDFeeder = csv("data/videoParam.csv").circular

  /** API Calls * */


  def EnrichedCatalogueAPI():ChainBuilder = {
      feed(Packagefeeder).
      feed(Countryfeeder).
        feed(platformFeeder)
        .exec(http("GET Enriched/content-catalogue")
          .get("/catalogue-generator/enriched-videos?countryId=${country}&packageId=${packageId}&platformId=${platformId}")
          .check(status.in(200)))
  }

  /*def EnrichedCataloguePlatformID(): ChainBuilder = {
    feed(Packagefeeder).
      feed(Countryfeeder).
    feed(platformFeeder)
        .exec(http("GET Enrich/content-catalogue/PlatformID")
          .get("/catalogue-generator/enriched-videos/{videoId/programId/genRef}?countryId=${country}&packageId=${packageId}&platformId=${platformId}")
          .check(status.in(200)))

  }*/
  def EnrichedCataloguePlatformIDVideoID(): ChainBuilder = {
    feed(Packagefeeder).
      feed(Countryfeeder).
      feed(platformFeeder)
      .exec(http("GET Enrich/content-catalogue/PlatformIDVideoID")
        .get("/catalogue-generator/enriched-videos/?videoId=IS20_100243294&countryId=${country}&packageId=${packageId}&platformId=${platformId}")
        .check(status.in(200)))
  }
  def EnrichedCatalogueAPIVideoID():ChainBuilder = {
    //val getEnrichedCatalogues = {
    feed(Packagefeeder).
      feed(Countryfeeder).
      feed(platformFeeder).
      feed(ProgramIDFeeder)
        .exec(http("GET Enrich/content-catalogue/videoID")
          .get("/api/cs-mobile/now-content/v7/catalogue-generator/enriched-videos?countryId=${country}&packageId=${packageId}&platformId=${platformId}&${videoparam}")
          .check(status.in(200)))
    //}
  }

  def EnrichedCatalogueProgramID() : ChainBuilder = {
    //val getEnrichedCatalogues = {
    feed(Packagefeeder).
      feed(Countryfeeder).
    feed(platformFeeder)
        .exec(http("GET Enrich/content-catalogue/ProgramID")
          .get("/api/cs-mobile/now-content/v7/catalogue-generator/catalogues?countryId=${country}&packageId=${packageId}&platformId=${platformId}&programId=${programId}")
          .check(status.in(200)))
    //}
  }

  def EnrichedCatalogueAPIgenRef():ChainBuilder = {
    feed(Packagefeeder).
      feed(Countryfeeder)
      feed(platformFeeder)
        .exec(http("GET Enrich/content-catalogue/genRef")
          .get("/api/cs-mobile/now-content/v7/catalogue-generator/enriched-videos?countryId=${country}&packageId=${packageId}&platformId=${platformId}&genRef=${genref}")
          .check(status.in(200)))
  }

  def LegacyCataloguePlatformID(): ChainBuilder = {
    feed(Packagefeeder).
      feed(Countryfeeder).
      feed(platformFeeder)
        .exec(http("GET Legacy/content-catalogue/PlatformID")
          .get("/api/cs-mobile/now-content/v7/catalogue-generator/catalogues?countryId=${country}&packageId=${packageId}&platformId=${platformId}")
          .check(status.in(200)))
    //}
  }

  def LegacyCataloguePlatformId(): ChainBuilder = {
    feed(Packagefeeder).
      feed(Countryfeeder).
      feed(platformFeeder)
        .exec(http("GET Legacy/content-catalogue/PlatformId")
          .get("/api/cs-mobile/now-content/v7/catalogue-generator/catalogues/{videoId/programId/genRef}?countryId=${country}&packageId=${packageId}&platformId=${platformId}")
          .check(status.in(200)))
    //}
  }

  def LegacyCatalogueVideoId (): ChainBuilder = {
    feed(Packagefeeder).
      feed(Countryfeeder).
      feed(platformFeeder)
        .exec(http("GET Legacy/content-catalogue/videoId")
          .get("/api/cs-mobile/now-content/v7/catalogue-generator/catalogues?countryId=${country}&packageId=${packageId}&platformId=${platformId}&videoId=${videoId}")
          .check(status.in(200)))
    }
 //}

  def LegacyCatalogueProgramId (): ChainBuilder = {
    feed(Packagefeeder).
      feed(Countryfeeder).
      feed(platformFeeder)
        .exec(http("GET Legacy/content-catalogue/ProgramId")
          .get("/api/cs-mobile/now-content/v7/catalogue-generator/catalogues?countryId=${country}&packageId=${packageId}&platformId=${platformId}&${videoparam}")
          .check(status.in(200)))
    //}
  }

  def LegacyCatalogueGenRef (): ChainBuilder = {
    feed(Packagefeeder).
      feed(Countryfeeder).
      feed(platformFeeder)
        .exec(http("GET Legacy/content-catalogue/GenRef")
          .get("/api/cs-mobile/now-content/v7/catalogue-generator/catalogues?countryId=${country}&packageId=${packageId}&platformId=${platformId}&genRef=${genref}")
          .check(status.in(200)))
    //}
  }

  val scn = scenario("Check catalogue Movies ")

    .forever(){

      //exec(EnrichedCatalogueAPI())
        //.pause(1)
        //.exec(EnrichedCataloguePlatformIDVideoID())
        //.pause(1)
        exec(EnrichedCatalogueAPIVideoID())
        .pause(1)
        //.exec(EnrichedCatalogueProgramID())
        //.pause(1)
        //.exec(EnrichedCatalogueAPIgenRef())
        //.pause(1)
        //.exec(LegacyCataloguePlatformID())
        //.pause(1)
        //.exec(LegacyCataloguePlatformId())
        //.pause(1)
        //.exec(LegacyCatalogueVideoId())
        //.pause(1)
        //.exec(LegacyCatalogueProgramId())
        //.pause(1)
        //.exec(LegacyCatalogueGenRef())
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