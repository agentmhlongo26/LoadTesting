package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import scala.concurrent.duration.DurationInt

class CsMobileToEpgService extends Simulation{

  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol **/
  val httpConf = http
    .baseUrl("https://ssl.dstv.com")
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

  val csvFeeder = csv("data/objects.csv").circular
  val connectIdFeeder = csv("data/newConnectID.csv").circular

  def getjwt():ChainBuilder = {

    //val path: String = "/connect/connect-authtoken/v2/accesstoken/"
    feed(connectIdFeeder)
      .exec(http("GET jwt Token")
        .post("/connect/connect-authtoken/v2/accesstoken/")
        //.post("http://connect-authtoken.dstvo.internal/connect/connect-authtoken/v2/accesstoken")
        .header("Content-Type", "application/json")
        .header("Authorization", "Basic ZHN0dm5vd3dlYnJlYWN0OmRzdHZub3d3ZWJyZWFjdA==")
        .body(StringBody(
          """{
                "sub":  "${connectId}",
                "aud":  "dc09de02-de71-4181-9006-2754dc5d3ed3"
          }"""
        )).check(status.is(200))
        .check(jsonPath("$.accessToken").saveAs("jwt"))
      )}


  def getEpgEvents():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/epgEvents"
    feed(csvFeeder)
      .exec(http("GET EPG Events")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
      )}

  def getEpgEventsByCount():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/epgEventsByCount;count=1"
    feed(csvFeeder)
      .exec(http("GET EPG Events By Count")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
        .check(jsonPath("$.*[0].id").saveAs("eventId"))
      )}
  println("${eventId}")

  def getEpgEventById():ChainBuilder = {

    val path: String ="/api/cs-mobile/v7/epg-service/event;eventId=DW496085552"
    feed(csvFeeder)
      .exec(http("GET EPG Events By Event Id")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
        .check(jsonPath("$.mainContentID").saveAs("correlationId"))
      )}

  def getEpgFutureEvents():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/futureEvents;correlationId=${correlationId}"
    feed(csvFeeder)
      .exec(http("GET EPG Future events")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
        //.check(jsonPath("$.mainContentID").saveAs("correlationId"))
      )}

  def getEpgPackages():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/packages"
    feed(csvFeeder)
      .exec(http("GET EPG Packages")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))

      )}

  def getEpgGenre():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/genres;liveStreamsOnly=true"
    feed(csvFeeder)
      .exec(http("GET EPG Genre Live Stream True")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
      )}

  def getEpgGenreFalse():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/genres;liveStreamsOnly=false"
    feed(csvFeeder)
      .exec(http("GET EPG Genre Live Stream False")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
      )}

  def getEpgChannels():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/channels"
    feed(csvFeeder)
      .exec(http("GET EPG Channels")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
        .check(jsonPath("$.items[0].id").saveAs("channelId"))
        .check(jsonPath("$.items[0].notificationId").saveAs("notificationId"))
      )}

  def getEpgChannelsByTag():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/channels/${channelId};notificationId=${notificationId}"
    feed(csvFeeder)
      .exec(http("GET EPG Channels By Tag")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
        .check(jsonPath("$.id").saveAs("channelId"))
      )}

  def getEpgChannelEventsByCount():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/channels/${channelId}/byCount;count=1"
    feed(csvFeeder)
      .exec(http("GET EPG Channel events by count")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
      )}

  def getEpgChannelEventsByDay():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/channels/CHD/byDay;daysBefore=0;daysAfter=0"
    feed(csvFeeder)
      .exec(http("GET EPG Channel events by day")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.is(200))
        //.check(jsonPath("$.[0].mainContentID").saveAs("mainContentId"))
      )}

  def getEpgChannelsWithEvents():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/channels/events;genre=Sport;country=ZA;packageId=PREMIUM;count=0;utcOffset=+02:00"
    feed(csvFeeder)
      .exec(http("GET EPG Channels With Events")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.in(200, 304))

      )}

  def getEpgSections():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/sectionsNoAuth;country=ZA;packageId=PREMIUM;utcOffset=%2B02%3A00"
    feed(csvFeeder)
      .exec(http("GET EPG sections")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.in(200, 304))

      )}

  def getEpgRecentltWatched():ChainBuilder = {
    val path: String = "/api/cs-mobile/v7/epg-service/recentlyWatchedChannels"
    feed(csvFeeder)
      .exec(http("GET EPG Recently Watched")
        .get(path)
        .header("Authorization", "${jwt}")
        .check(status.in(200, 204))

      )}




  val scn = scenario("Check cs-Mobile to Epg ")

    .forever() {
      exec(getjwt)
        //.pause(1)
        //.exec(getEpgEventsByCount)
        //.pause(1)
        .exec(getEpgEventById)
        .pause(1)
        .exec(getEpgFutureEvents)
        .pause(1)
        .exec(getEpgPackages)
        .pause(1)
        .exec(getEpgGenre)
        .pause(1)
        .exec(getEpgGenreFalse)
        .pause(1)
        .exec(getEpgChannels)
        .pause(1)
        .exec(getEpgChannelsByTag)
        .pause(1)
        .exec(getEpgChannelEventsByCount)
        .pause(1)
        .exec(getEpgChannelEventsByDay)
        .pause(1)
        .exec(getEpgChannelsWithEvents)
        .pause(1)
        .exec(getEpgSections)
        .pause(1)
        .exec(getEpgRecentltWatched)
        .pause(1)

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
