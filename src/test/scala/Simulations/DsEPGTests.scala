package Simulations
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._
import io.gatling.core.Predef.Simulation

class DsEPGTests extends Simulation{
  before {
    println(s"Running test with ${channelUserCount} users")
    println(s"Ramping users over ${channelRampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol **/
  val httpConf = http
    //.baseUrl("http://ds-epg.dstvo.internal")
    .baseUrl("https://public-ingress-prd-230570154.eu-west-1.elb.amazonaws.com")
    //.proxy(Proxy("localhost", 8888).httpsPort(8888))
    .header("Accept", "application/json")

  /** Ramp Up Runtime Parameters **/
  def channelUserCount: Int = getProperty("USERS", "5").toInt

  def channelRampDuration: Int = getProperty("RAMP_DURATION", "5").toInt

  def testDuration: Int = getProperty("DURATION", "60").toInt

  /** bouquet Runtime Parameters */
  def bouquetUserCount: Int = getProperty("BOUQUET_USERS", "2").toInt

  def bouquetRampDuration: Int = getProperty("BOUQUET_RAMP_DURATION", "2").toInt

  /** bouquet collection Runtime Parameters */
  def bouquetCollectionUserCount: Int = getProperty("BOUQUET_COLLECTION_USERS", "2").toInt

  def bcRampDuration: Int = getProperty("BOUQUET_COLLECTION_RAMP_DURATION", "2").toInt

  /** ibs Codes Runtime Parameters */
  def ibsCodesUserCount: Int = getProperty("IBSCODES_USERS", "2").toInt

  def icRampDuration: Int = getProperty("IBSCODES_RAMP_DURATION", "2").toInt

  /** ibs Codes by code Runtime Parameters */
  def ibsCodesByCodeUserCount: Int = getProperty("IBSCODES_BY_CODE_USERS", "2").toInt

  def icbcRampDuration: Int = getProperty("ICBC_RAMP_DURATION", "2").toInt

  /** revision history Runtime Parameters */
  def revisionHistoryUserCount: Int = getProperty("RH_USERS", "2").toInt

  def rhRampDuration: Int = getProperty("RH_RAMP_DURATION", "2").toInt

  /** schedule Runtime Parameters */
  def scheduleUserCount: Int = getProperty("SCHEDULE_USERS", "2").toInt

  def seRampDuration: Int = getProperty("SE_RAMP_DURATION", "2").toInt



  /** Helper Methods **/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data CSV **/
  val feeder = csv("data/objects.csv").random
  val csvFeeder = csv("data/bookmark.csv").random
  val paramsFeeder = csv("data/params.csv").circular
  val connectIdFeeder = csv("data/newConnectID.csv").circular

  def getChannels():ChainBuilder = {
    exec(http("GET Channels")
      .get("/ds-epg/channel;includeFields=shortName,channelTag,channelName,channelNumber,imageInstancesMap;all=true")
      .check(status.is(200))

    )}
  pause(duration = 300 millisecond)

  def getRevisionHistory():ChainBuilder = {
    exec(http("GET | Revision History")
      .get("/ds-epg/channel/21ef27fd-1246-4199-bd0c-0ffb817cc5c8/revisionHistory")
      .check(status.in(200)))
  }
  pause(duration = 300 millisecond)

  //**NOTE** filtered date range to be change current date to prevent the below call returning empty response also keep the time range between 10:15 and 12:15
  // a larger time range returns a larger response payload and take too long.
  def getScheduledEvent():ChainBuilder = {
    exec(http("GET | Scheduled Events")
      .get("/ds-epg/schedule;all=true;filterField=startDateTime;startDateTimeStartFilterValue=2022-02-17T10:15:00;startDateTimeEndFilterValue=2022-02-12T17:15:00;filterOperation=AND/")
      .check(status.in(200)))
  }
  pause(duration = 300 millisecond)

  def getBouquet():ChainBuilder = {
    exec(http("GET | Bouquet")
      .get("/ds-epg/bouquet;includeFields=channels;all=true")
      .check(status.in(200)))
  }

  pause(duration = 300 millisecond)

  def getBouquetCollection():ChainBuilder = {
    exec(http("GET | bouquet Collection Revision History By Id")
      .get("/ds-epg/bouquetCollection/5aaba0b5e4b0d72d068085fd/revisionHistory")
      .check(status.in(200)))
  }
  pause(duration = 300 millisecond)

  def getIbsCodes():ChainBuilder = {
    exec(http("GET | Ibs Code")
      .get("/ds-epg/ibsCode")
      .check(status.in(200)))
  }
  pause(duration = 300 millisecond)

  def getIbsCodeByCode():ChainBuilder = {
    exec(http("GET | Ibs Code but Code")
      .get("/ds-epg/ibsCode;filterField=epgCodes;epgCodesFilterValue=IND")
      .check(status.in(200)))
  }
  pause(duration = 300 millisecond)





  val channel = scenario("Api calls made on the Channels").forever(){exec(getChannels())}//;exec(editorialsForHome());exec(getNavigationMenu());exec(getProfile())
  val revisionHistory = scenario("Api calls made to Revision History").forever(){exec(getRevisionHistory())}
  val scheduledEvent = scenario("Api calls made for Schedule events").forever(){exec(getScheduledEvent())}
  val bouquet = scenario("Api calls made for bouquets").forever(){exec(getBouquet())}
  val bouquetCollection = scenario("Api calls made to Bouquets collections").forever(){exec(getBouquetCollection())}
  val ibsCodes = scenario("Api calls made for Ibs Codes").forever(){exec(getIbsCodes())}
  val ibsCodeByCode = scenario("Api calls made for Ibs Codes By Code").forever(){exec(getIbsCodeByCode())}



  /** Load Test Simulation **/
  setUp(
    /*scn.inject(
      nothingFor(2 seconds),
      rampUsers(userCount) during (rampDuration seconds))*/
    channel.inject(rampUsers(channelUserCount) during (channelRampDuration seconds)),
    revisionHistory.inject(rampUsers(revisionHistoryUserCount) during (rhRampDuration seconds)),
    scheduledEvent.inject(rampUsers(scheduleUserCount) during (seRampDuration seconds)),
    bouquet.inject(rampUsers(bouquetUserCount) during (bouquetRampDuration seconds)),
    bouquetCollection.inject(rampUsers(bouquetCollectionUserCount) during (bcRampDuration seconds)),
    ibsCodes.inject(rampUsers(ibsCodesUserCount) during (icRampDuration seconds)),
    ibsCodeByCode.inject(rampUsers(ibsCodesByCodeUserCount) during (icbcRampDuration seconds)),
    //vodAuthorizationApiCall.inject(rampUsers(customUserCount) during (customRampDuration seconds)),
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }

}
