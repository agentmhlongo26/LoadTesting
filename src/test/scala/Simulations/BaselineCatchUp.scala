package Simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class BaselineCatchUp extends Simulation {
  /** Configure Protocol * */
  val httpConf = http
    .baseUrl("https://ssl.dstv.com")
    .acceptHeader("*/*")
    .contentTypeHeader("application/json")
  //.proxy(Proxy("localhost", 8888).httpsPort(8888))

  /** Ramp Up Runtime Parameters * */
  def userCountEditorials: Int = getProperty("EDITORIAL_USERS", "2").toInt

  def userCountCatalogueBy: Int = getProperty("CATALOGUE_BY_USERS", "2").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "4").toInt

  def testDuration: Int = getProperty("DURATION", "60").toInt

  /** Helper Methods * */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data Feeder */
  val feeder = csv("data/objects.csv").random
  val paramsFeeder = csv("data/params.csv").circular

  /** API Calls * */
  object Browse {

    val getEditorialsForCatchup = forever() {
      feed(paramsFeeder).
        feed(feeder)
        .exec(http("GET | Editorials for Catchup")
          .get("/api/cs-mobile/editorial/${version}/getEditorialsForCatchup;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;packageId=3e6e5480-8b8a-4fd5-9721-470c895f91e2;platformId=${platformId}")
          .header("Authorization", "${sessionId}")
          .check(status.in(200, 304)))
        .pause(1000 milliseconds)
    }

    val catalogueBy = forever() {
      feed(paramsFeeder).
        feed(feeder)
        .exec(http("GET | catalogueByPackageAndCountry")
          .get("/api/cs-mobile/now-content/v7/catalogueByPackageAndCountry;productId=c53b19ce-62c0-441e-ad29-ecba2dcdb199;platformId=${platformId};subscriptionPackage=PREMIUM;country=ZA;tags=${tags};page=0;pageSize=36")
          .check(status.in(200, 304)))
        .pause(1000 milliseconds)
    }
  }

  /** Scenarios * */
  val catalogueByApiCalls = scenario("catalogueByPackageAndCountry").exec(Browse.catalogueBy)
  val editorialsForCatchup = scenario("getEditorialsForCatchup").exec(Browse.getEditorialsForCatchup)

  /** Load Simulation * */
  setUp(
    catalogueByApiCalls.inject(rampUsers(userCountCatalogueBy) during (rampDuration seconds)),
    editorialsForCatchup.inject(rampUsers(userCountEditorials) during (rampDuration seconds)),
  ).protocols(httpConf)
    .maxDuration(testDuration seconds)
}