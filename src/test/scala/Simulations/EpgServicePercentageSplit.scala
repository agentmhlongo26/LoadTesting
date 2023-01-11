package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
//import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

class EpgServicePercentageSplit extends Simulation{

  /** Configure Protocol **/
  val httpConf = http
    .baseUrl("https://epg-service.dstv.com")
    //.baseUrl("http://epg-service.dstvo.internal")
    //.proxy(Proxy("localhost", 8888).httpsPort(8888))
    .header("Accept", "application/json")

  /** Ramp Up Runtime Parameters * */
  def userCount: Int = getProperty("USERS", "2").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "4").toInt

  def testDuration: Int = getProperty("DURATION", "60").toInt

  /** Stair Case Runtime Parameters */
  def incrementFutureEvents: Int = getProperty("INCREMENT_FUTURE_EVENTS", "7").toInt

  def incrementChannelEvents: Int = getProperty("INCREMENT_CHANNELS_EVENTS", "8").toInt

  def incrementGetChannel: Int = getProperty("INCREMENT_GET_CHANNELS", "4").toInt

  def incrementGetPackages: Int = getProperty("INCREMENT_GET_PACKAGES", "2").toInt

  def noOfSteps: Int = getProperty("NO_OF_STEPS", "2").toInt

  def startingFutureEvents: Int = getProperty("STARTING_FUTURE_EVENTS", "5").toInt

  def startingChannelEvents: Int = getProperty("STARTING_CHANNELS_EVENTS", "3").toInt

  def startingGetChannel: Int = getProperty("STARTING_GET_CHANNELS", "2").toInt

  def startingGetPackages: Int = getProperty("STARTING_GET_PACKAGE", "1").toInt

  def levelDuration: Int = getProperty("LEVEL_DURATION", "30").toInt

  def stepRampDuration: Int = getProperty("STEP_RAMP_DURATION", "0").toInt

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


  object  ChannelsEvents {

    val channelsEvents = forever(){
      feed(csvEventByNumber)
        .exec(http("GET Channels List With Events")
          .get("/v1/channels/events?count=2&country=${country}&liveStreamsOnly=false&packageId=${packageId}&product=DSTV_NOW&utcOffset=%2B02%3A00")
          .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
          .check(status.is(200)))
        .pause(300 milliseconds)

    }

  }

  object futureEvents {
    val futureEvents = forever() {
      feed(csvEventByNumber)
        .exec(http("GET Future Events")
          .get("/v1/events/futureEvents?correlationId=0x218961&country=${country}&fromEpisode=1&packageId=${packageId}&products=DSTV_NOW")
          .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
          .check(status.is(200)))
        .pause(300 milliseconds)
    }
  }

    object channels {
      val Channels = forever(){
        feed(csvFeeder)
        feed(csvLive)
        feed(csvPackageId)
          .exec(http("GET Genre For Package False")
            .get("/v1/channels/genres?country=" + "${country}" + "&liveStreamsOnly=true" + "&packageId=" + "${packageId}")
            .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
            .check(status.is(200)))
          .pause(300 milliseconds)

        feed(csvLive)
          .exec(http("GET Channels List")
            .get("/v1/channels?country=${country}&liveStreamsOnly=${liveStream}&packageId=${packageId}")
            .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
            .check(status.is(200)))
          .pause(300 milliseconds)

      }
    }

    object packages{
      val Packages = forever(){
        feed(csvFeeder)
          .exec(http("GET Package by country")
            .get("/v1/packages?country=" + "${country}")
            .header("Authorization", "Basic YXdzYXBpZ2F0ZXdheTpnb29sZWVuZ2U2YWh6b28yRWl3ZQ==")
            .check(status.is(200)))
          .pause(300 milliseconds)
      }
    }
  /** Scenarios * */
  val channelEvents = scenario("Api calls made on for Channel events").exec(ChannelsEvents.channelsEvents)
  val futureevents = scenario("Api calls made on for future events").exec(futureEvents.futureEvents)
  val getChannels = scenario("Api calls made on for streaming channels").exec(channels.Channels)
  val getPackage = scenario("Api calls to get the packages").exec(packages.Packages)

  /** Load Simulation * */
  setUp(
    channelEvents.inject(rampUsers(startingChannelEvents) during (rampDuration seconds)),
    futureevents.inject(rampUsers(startingFutureEvents) during (rampDuration seconds)),
    getChannels.inject(rampUsers(startingGetChannel) during (rampDuration seconds)),
    getPackage.inject(rampUsers(startingGetPackages) during (rampDuration seconds)),
    /*channelEvents.inject(
      incrementConcurrentUsers(incrementChannelEvents)
        .times(noOfSteps)
        .eachLevelLasting(levelDuration seconds)
        .separatedByRampsLasting(stepRampDuration seconds)
        .startingFrom(startingChannelEvents)
    ),
    futureevents.inject(
      incrementConcurrentUsers(incrementFutureEvents)
        .times(noOfSteps)
        .eachLevelLasting(levelDuration seconds)
        .separatedByRampsLasting(stepRampDuration seconds)
        .startingFrom(startingFutureEvents)
    ),
    getChannels.inject(
      incrementConcurrentUsers(incrementGetChannel)
        .times(noOfSteps)
        .eachLevelLasting(levelDuration seconds)
        .separatedByRampsLasting(stepRampDuration seconds)
        .startingFrom(startingGetChannel)
    ),
    getPackage.inject(
      incrementConcurrentUsers(incrementGetPackages)
        .times(noOfSteps)
        .eachLevelLasting(levelDuration seconds)
        .separatedByRampsLasting(stepRampDuration seconds)
        .startingFrom(startingGetPackages)
    )*/

  ).protocols(httpConf)
    .maxDuration(testDuration seconds)




}
