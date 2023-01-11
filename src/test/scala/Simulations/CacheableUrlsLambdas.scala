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

class CacheableUrlsLambdas extends Simulation {

  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol **/

  val httpConf = http
    .baseUrl("https://6p4tkpt9g6.execute-api.eu-west-1.amazonaws.com")

  /** Runtime Parameters **/
  def userCount: Int = getProperty("USERS", "20").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "1").toInt

  def testDuration: Int = getProperty("DURATION", "900").toInt

  /** Helper Methods **/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data CSV **/
  val csvFeeder = csv("CachebleData/sessionId.csv").circular
  //val csvPackages = csv("CachebleData/package.csv").circular

  /** API Calls **/
 /* def getEpgSectionsNoAuth() = {
    feed(csvPackages).
      exec(http("GET | Epg Sections No Auth")
        .get("/api/cs-mobile/" + "${version}" + "/epg-service/sectionsNoAuth;country=" + "${country}" + ";packageId=" + "${packageId}")
        .check(status.is(200)))
  }*/

  def getEpgSectionsByPlatformCountry() = {
    feed(csvFeeder).
      exec(http("GET | Epg Sections by country, subscription, Platform")
        .get("/api/cs-mobile/epg/v6/sections;platformId=32faad53-5e7b-4cc0-9f33-000092e85950")
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getEpgSectionsByPlatformCountryV7() = {
    feed(csvFeeder).
      exec(http("GET | Epg Sections by country, subscription, Platform V7")
        .get("/api/cs-mobile/epg/v7/sections;platformId=32faad53-5e7b-4cc0-9f33-000092e85950")
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getVodSections() = {
    feed(csvFeeder).
      exec(http("GET | Vod Sections")
        .get("/api/cs-mobile/v6/application/vod/sections;platformId=32faad53-5e7b-4cc0-9f33-000092e85950;subscriptionPackageId=PREMIUM;country=ZA")
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getVodSectionsV7() = {
    feed(csvFeeder).
      exec(http("GET | Vod Sections V7")
        .get("/api/cs-mobile/v7/application/vod/sections;platformId=32faad53-5e7b-4cc0-9f33-000092e85950;subscriptionPackageId=PREMIUM;country=ZA")
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getNavigationMenu() = {
    feed(csvFeeder).
      exec(http("GET | Navigation Menu")
        .get("https://pzc04rbefd.execute-api.eu-west-1.amazonaws.com/api/cs-mobile/v7/application/navigationmenu;platformId=32faad53-5e7b-4cc0-9f33-000092e85950")
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }
  def getNavigationMenuV6() = {
    feed(csvFeeder).
      exec(http("GET | Navigation Menu V6")
        .get("https://pzc04rbefd.execute-api.eu-west-1.amazonaws.com/api/cs-mobile/v6/application/navigationmenu;platformId=32faad53-5e7b-4cc0-9f33-000092e85950")
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }


  /** Scenarios **/
  val scn = scenario("Cacheable Urls Lambda")
    .forever() {
     /* exec(getEpgSectionsNoAuth())
        .pause(1)*/
        exec(getEpgSectionsByPlatformCountry())
        .pause(1)
        .exec(getVodSections())
        .pause(1)
        .exec(getNavigationMenu())
        .pause(1)
        .exec(getEpgSectionsByPlatformCountryV7())
        .pause(1)
        .exec(getVodSectionsV7())
        .pause(1)
        .exec(getNavigationMenuV6())
        .pause(1)
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
