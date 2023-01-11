package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class EpgService extends Simulation {

  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol **/
  val httpConf = http
    //.baseUrl("https://epg-service.dstv.com")
    .baseUrl("https://api.legacy.connectedvideo.tv")
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
  val csvFeeder = csv("EpgCsv/CountryList.csv").circular
  val csvLive = csv("EpgCsv/Livestream.csv").circular
  val csvPackageId = csv("EpgCsv/PackageId.csv").circular
  val csvChannelTag = csv("EpgCsv/ChannelTag.csv").circular
  val csvEventsByCountry = csv("EpgCsv/eventsByCountry.csv").circular
  val csvEventByNumber = csv("EpgCsv/eventsByNumberOfEvents.csv").circular
  val csvChannelByNumberOfEvents = csv("EpgCsv/ChannelEventsByCount.csv").circular


  def getPackageByCountry():ChainBuilder = {
    val path: String = "/v1/packages/?country=" + "${country}"
    feed(csvFeeder)
      .exec(http("GET Package by country")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
      )}

  def getGenreForPackageTrue():ChainBuilder = {
    val path: String = "/v1/channels/genres?country=" + "${country}" + "&liveStreamsOnly=true" + "&packageId=" + "${packageId}"
    feed(csvFeeder)
    feed(csvLive)
    feed(csvPackageId)
      .exec(http("GET Genre For Package True")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
      )}

  def getGenreForPackageFalse():ChainBuilder = {
    val path: String = "/v1/channels/genres?country=" + "${country}" + "&liveStreamsOnly=false" + "&packageId=" + "${packageId}"
    feed(csvFeeder)
    feed(csvLive)
    feed(csvPackageId)
      .exec(http("GET Genre For Package False")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
      )}

  def getChannelsByNumOfEvents():ChainBuilder = {
    val path: String = "/v1/channels/${channel}/events/byNumberOfEvents/?count=${count}&product=DSTV_NOW"

    feed(csvChannelTag)
      .exec(http("GET channels by number of events")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
        .check(jsonPath("$.events[0].id").saveAs("eventId"))
      )}

  def getEventsByEventId():ChainBuilder = {
    //val path: String = "/v1/event/C3389653537"
    feed(csvChannelTag)
      .exec(http("GET Events By Event Id")
        .get("/v1/event/${eventId}")
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))

      )}

  def getEventsByCountry():ChainBuilder = {
    val path: String = "/v1/events?country=${country}&daysAfter=${daysAfter}&daysBefore=${daysBefore}&product=DSTV_NOW&utcOffset=${utcOffset}"
    feed(csvEventsByCountry)
      .exec(http("GET Events By Country")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
      )}

  def getEventsByNumberOfEvents():ChainBuilder = {
    val path: String = "/v1/events/byNumberOfEvents?count=${count}&country=${country}&packageId=${packageId}&product=DSTV_NOW"

    feed(csvEventByNumber)

      .exec(http("GET Events By number of Events")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
        .check(jsonPath("$.SKY.events[0].correlationId").saveAs("correlationId"))
      )}

  def getFutureEvents():ChainBuilder = {
    val path: String = "/v1/events/futureEvents?correlationId=${correlationId}&country=${country}&fromEpisode=1&packageId=${packageId}&products=DSTV_NOW"
    feed(csvEventByNumber)
      .exec(http("GET Future Events")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
      )}


  def getChannelsList():ChainBuilder = {
    val path: String = "/v1/channels/?country=${country}&liveStreamsOnly=${liveStream}&packageId=${packageId}"
    feed(csvLive)
      .exec(http("GET Channels List")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
      )}

  def getChannelEventsForTagByDay():ChainBuilder = {
    val path: String = "/v1/channels/${channelTag}/events?daysAfter=${daysAfter}&daysBefore=${daysBefore}&product=DSTV_NOW&utcOffset=${utcOffset}"
    feed(csvEventsByCountry)
      .exec(http("GET Channel events for Tag by Day ")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
      )}

  def getChannelsByNumberOfEvents():ChainBuilder = {
    val path: String = "/v1/channels/${channelTag}/events/byNumberOfEvents?count=${count}&product=DSTV_NOW"
    feed(csvChannelByNumberOfEvents)
      .exec(http("GET Channel by number of events")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
      )}

  def getRecurringEventsByChannel():ChainBuilder = {
    val path: String = "/v1/channels/E10/events/byNumberOfEvents?count=4&product=DSTV_NOW"
    feed(csvChannelByNumberOfEvents)
      .exec(http("GET Recurring Events By Channel")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
        //.check(jsonPath("$.events[0].correlationId").saveAs("correlationId"))
      )}

  def getChannelsByFutureEvents():ChainBuilder = {
    val path: String = "/v1/channels/E10/events/futureEvents?correlationId=${correlationId}&fromEpisode=0&product=DSTV_NOW"
    feed(csvChannelByNumberOfEvents)
      .exec(http("GET Channels By Future Events")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))
        .check(jsonPath("$.events[0].correlationId").saveAs("correlationId"))
      )}

  def getChannelListWithEvents():ChainBuilder = {
    val path: String = "/v1/channels/events?count=2&country=${country}&liveStreamsOnly=false&packageId=${packageId}&product=DSTV_NOW&utcOffset=%2B02%3A00"
    feed(csvEventByNumber)
      .exec(http("GET Channels List With Events")
        .get(path)
        .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
        .check(status.is(200))

      )}



  val scn = scenario("Check Epg Service ")

    .forever() {
      exec(getPackageByCountry())
        .pause(1)
        .exec(getGenreForPackageTrue)
        .pause(1)
        .exec(getGenreForPackageFalse)
        .pause(1)
        .exec(getChannelsByNumOfEvents)
        .pause(1)
        .exec(getEventsByEventId)
        .pause(1)
        //.exec(getEventsByCountry)
        //.pause(1)
        //.exec(getEventsByNumberOfEvents)
        //.pause(1)
        .exec(getFutureEvents)
        .pause(1)
        .exec(getChannelsList)
        .pause(1)
        .exec(getChannelEventsForTagByDay)
        .pause(1)
        .exec(getChannelsByNumberOfEvents)
        .pause(1)
        .exec(getRecurringEventsByChannel)
        .pause(1)
        //.exec(getChannelsByFutureEvents)
        //.pause(1)

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
